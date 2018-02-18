package onextent.supersix.jwt

import akka.actor.ActorRef
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import com.typesafe.scalalogging.LazyLogging
import onextent.supersix.jwt.actors.key.ShardedKeyService
import onextent.supersix.jwt.http.functions.HttpSupport
import Conf._
import onextent.supersix.jwt.http.KeyRoute

object Main extends App with LazyLogging with HttpSupport with Directives {

  val keyService: ActorRef =
    actorSystem.actorOf(ShardedKeyService.props(),
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

}
