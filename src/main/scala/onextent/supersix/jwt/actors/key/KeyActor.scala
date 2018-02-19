package onextent.supersix.jwt.actors.key

import java.util.UUID

import akka.actor.{Actor, Props}
import akka.persistence.{PersistentActor, RecoveryCompleted}
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import onextent.supersix.jwt.Conf
import onextent.supersix.jwt.RsaKeys.KeyInfo
import onextent.supersix.jwt.models._

object KeyActor {
  def props(clientId: UUID, keyInfo: KeyInfo)(implicit timeout: Timeout) =
    Props(new KeyActor(clientId, keyInfo))
}

class KeyActor(clientId: UUID, keyInfo: KeyInfo) extends Actor with LazyLogging with Conf with PersistentActor {

  logger.debug(s"key actor for '$clientId' created")

  var stateKey: Option[Key] = None
  var stateSecrets: Option[KeySecrets] = None

  override def persistenceId: String =
    conf.getString("main.keyPersistenceId") + "_" + clientId.toString.replace('-', '_')

  override def receiveRecover: Receive = {

    // BEGIN DB RECOVERY

    case msg: CreateKey =>
      logger.debug(s"recovering with create key $clientId")
      stateKey = Some(msg.key)
      stateSecrets = Some(msg.secrets)

    case msg: UpdateKey => stateKey = Some(msg.key)

    case _: RecoveryCompleted => logger.debug(s"recovery for $clientId completed: $stateKey")

    case msg: Any => logger.warn(s"I don't know what to do with $msg")

    // END DB RECOVERY
  }

  override def receiveCommand: Receive = {

    case msg: CreateKey =>
      stateKey = Some(msg.key)
      stateSecrets = Some(msg.secrets)
      persistAsync(msg) { _ =>
        sender() ! stateKey
      }

    case msg: UpdateKey =>
      stateKey = Some(msg.key)
      persistAsync(msg) { _ =>
        sender() ! stateKey
      }

    case msg: JwtRequest =>
      stateKey match {
        case Some(key) =>
          if (key.clientSecret == msg.clientSecret ) {
            sender() ! "see me"
          } else {
            sender() ! "incorrect secret"
          }
        case _ =>
          sender() ! "incorrect key state"
      }

  }

}
