package onextent.supersix.jwt.http

import akka.actor.ActorRef
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.ask
import com.typesafe.scalalogging.LazyLogging
import onextent.supersix.jwt.http.functions.HttpSupport
import onextent.supersix.jwt.models._
import onextent.supersix.jwt.models.functions._
import spray.json._

object KeyRoute
    extends JsonSupport
    with LazyLogging
    with Directives
    with HttpSupport {

  private def create(service: ActorRef): Route =
    path(urlpath / "key") {
      post {
        decodeRequest {
          entity(as[KeyRequest]) { keyReq =>
            val f = service ask CreateKey(MkKey(keyReq))
            onSuccess(f) { (r: Any) =>
              {
                r match {
                  case key: Key =>
                    complete(
                      HttpEntity(ContentTypes.`application/json`,
                                 key.toJson.prettyPrint))
                  case KeyAlreadyExists(clientId) =>
                    complete(StatusCodes.Conflict, s"$clientId already exists")
                  case _ =>
                    complete(StatusCodes.NotFound)
                }
              }
            }
          }
        }
      }
    }

  private def issue(service: ActorRef): Route =
    path(urlpath / "jwt") {
      post {
        decodeRequest {
          entity(as[JwtRequest]) { jwtReq =>
            val f = service ask jwtReq
            onSuccess(f) { (r: Any) =>
            {
              r match {
                case jwt: Jwt =>
                  complete(
                    HttpEntity(ContentTypes.`application/json`,
                      jwt.toJson.prettyPrint))
                case KeyNotFound(clientId) =>
                  complete(StatusCodes.Conflict, s"$clientId already exists")
                case _ =>
                  complete(StatusCodes.NotFound)
              }
            }
            }
          }
        }
      }
    }

  // todo: verify

  // todo: approve

  def apply(service: ActorRef): Route =
    create(service) ~
    issue(service)

}
