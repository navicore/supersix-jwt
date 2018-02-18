package onextent.supersix.jwt.actors.key

import akka.actor.{Actor, ActorRef, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import onextent.supersix.jwt.http.functions.HttpSupport

object ShardedKeyService {
  def props() = Props(new ShardedKeyService())
  def name = "shardedKeyService"
}

class ShardedKeyService() extends Actor with HttpSupport {

  ClusterSharding(context.system).start(
    KeyService.shardName,
    KeyService.props(),
    ClusterShardingSettings(context.system),
    KeyService.extractEntityId,
    KeyService.extractShardId
  )

  def shardedKeyService: ActorRef = {
    ClusterSharding(context.system).shardRegion(KeyService.shardName)
  }

  override def receive: Receive = {
    case m =>
      shardedKeyService forward m
  }

}
