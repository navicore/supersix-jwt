package onextent.supersix.jwt.actors.key

import java.util.UUID

import akka.actor.{Actor, Props}
import akka.persistence.PersistentActor
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import onextent.supersix.jwt.Conf
import onextent.supersix.jwt.models.{CreateKey, Key, UpdateKey}

object KeyActor {
  def props(clientId: UUID)(implicit timeout: Timeout) =
    Props(new KeyActor(clientId))
}

class KeyActor(clientId: UUID) extends Actor with LazyLogging with Conf with PersistentActor {

  logger.debug(s"key actor for '$clientId' created")

  var state: Option[Key] = None

  override def persistenceId: String =
    conf.getString("main.keyPersistenceId") + "_" + clientId.toString.replace('-', '_')

  override def receiveRecover: Receive = {

    // BEGIN DB RECOVERY

    case create: CreateKey => state = Some(create.key)
    case update: UpdateKey => state = Some(update.key)

    // END DB RECOVERY
  }

  override def receiveCommand: Receive = {

    case msg: CreateKey =>
      state = Some(msg.key)
      persistAsync(msg) { _ =>
        sender() ! msg.key
      }

    case msg: UpdateKey =>
      state = Some(msg.key)
      persistAsync(msg) { _ =>
        sender() ! msg.key
      }

  }

}
