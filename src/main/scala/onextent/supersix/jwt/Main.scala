package onextent.supersix.jwt

import akka.actor.ActorRef
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import com.typesafe.scalalogging.LazyLogging
import onextent.supersix.jwt.Conf._
import onextent.supersix.jwt.actors.key.{RsaService, ShardedKeyService}
import onextent.supersix.jwt.http.KeyRoute
import onextent.supersix.jwt.http.functions.HttpSupport
import akka.pattern.ask
import onextent.supersix.jwt.RsaKeys.KeyInfo

object Main extends App with LazyLogging with HttpSupport with Directives {

  val rsaService: ActorRef =
    actorSystem.actorOf(RsaService.props(), RsaService.name)

  // get rsa keys to give to actors to sign JWTs
  val f = rsaService ask "get"
  f.map {
    case keyInfo: KeyInfo =>
      logger.info(s"got key info: $keyInfo")
      val keyService: ActorRef =
        actorSystem.actorOf(ShardedKeyService.props(keyInfo),
          ShardedKeyService.name)
      val route =
        HealthCheck ~
          logRequest(urlpath) {
            handleErrors {
              cors(corsSettings) {
                KeyRoute(keyService)
              }
            }
          }
      Http().bindAndHandle(route, "0.0.0.0", apiPort)
    case _ => logger.error("bad key info")
  }

}
