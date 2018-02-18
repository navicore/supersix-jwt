package onextent.supersix.jwt.actors.key

import java.util.UUID

import akka.actor._
import akka.cluster.sharding.ShardRegion
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import onextent.supersix.jwt.Conf
import onextent.supersix.jwt.models._

object KeyService extends Conf {

  def props()(implicit timeout: Timeout) =
    Props(new KeyService())
  def shardName = "deviceService"

  def SHARDS: Int = keyServiceShards

  val extractEntityId: ShardRegion.ExtractEntityId = {
    case m: CreateKey => (m.key.clientId.toString, m)
    case m: UpdateKey => (m.key.clientId.toString, m)
    case m: DeleteKey => (m.clientId.toString, m)
    case m: JwtRequest => (m.clientId.toString, m)
  }

  def calcShardName(uuid: UUID): String = {
    s"${Math.abs(uuid.getLeastSignificantBits) % SHARDS}"
  }

  val extractShardId: ShardRegion.ExtractShardId = {
    case m: CreateKey => calcShardName(m.key.clientId)
    case m: UpdateKey => calcShardName(m.key.clientId)
    case m: DeleteKey => calcShardName(m.clientId)
    case m: JwtRequest => calcShardName(m.clientId)
  }

}

class KeyService()(implicit timeout: Timeout) extends Actor with LazyLogging {

  logger.debug(s"actor ${context.self.path} created")

  def create(actorId: String, key: Key): Unit = {
    // create a new actor for client ID and init by sending it its first msg
    context.actorOf(KeyActor.props(key.clientId), actorId) ! CreateKey(key)
    sender() ! key
  }

  override def receive: PartialFunction[Any, Unit] = {

    case CreateKey(key) =>
      context
        .child(key.clientId.toString)
        .fold(create(key.clientId.toString, key))(_ => {
          sender() ! KeyAlreadyExists(key.clientId)
        })

    case msg: UpdateKey =>
      def notFound(): Unit = sender() ! KeyNotFound(msg.key.clientId)
      context
        .child(msg.key.clientId.toString)
        .fold(notFound())(_ forward msg)

    case jwtReq: JwtRequest =>
      def notFound(): Unit = sender() ! KeyNotFound(jwtReq.clientId)
      context
        .child(jwtReq.clientId.toString)
        .fold(notFound())(_ forward jwtReq)

  }

}
