package onextent.supersix.jwt.actors.key

import akka.actor._
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import onextent.supersix.jwt.RsaKeys.KeyInfo

object RsaService {
  def props()(implicit timeout: Timeout) = Props(new RsaService())
  def name = "RsaService"
}

// todo: actor persistence
class RsaService()(implicit timeout: Timeout) extends Actor with LazyLogging {

  logger.debug(s"actor ${context.self.path} created")

  override def receive: PartialFunction[Any, Unit] = {

    case "get" =>
      sender() ! KeyInfo("", "", "")

  }

}
