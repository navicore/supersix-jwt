package onextent.supersix.jwt.actors.key

import akka.actor.{Actor, ActorRef, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import onextent.supersix.jwt.RsaKeys.KeyInfo
import onextent.supersix.jwt.http.functions.HttpSupport

object ShardedKeyService {
  def props(keyInfo: KeyInfo) = Props(new ShardedKeyService(keyInfo))
  def name = "shardedKeyService"
}

class ShardedKeyService(keyInfo: KeyInfo) extends Actor with HttpSupport {

  ClusterSharding(context.system).start(
    KeyService.shardName,
    KeyService.props(keyInfo),
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
