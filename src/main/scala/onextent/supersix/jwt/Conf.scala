package onextent.supersix.jwt

import akka.actor.ActorSystem
import akka.pattern.AskTimeoutException
import akka.serialization.SerializationExtension
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContextExecutor

object Conf extends Conf with LazyLogging {

  implicit val actorSystem: ActorSystem = ActorSystem(appName, conf)
  SerializationExtension(actorSystem)

  implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher

  val decider: Supervision.Decider = {

    case _: AskTimeoutException =>
      // might want to try harder, retry w/backoff if the actor is really supposed to be there
      logger.warn(s"decider discarding message to resume processing")
      Supervision.Resume

    case e: java.text.ParseException =>
      logger.warn(
        s"decider discarding unparseable message to resume processing: $e")
      Supervision.Resume

    case e: Throwable =>
      logger.error(s"decider can not decide: $e", e)
      Supervision.Stop

    case e =>
      logger.error(s"decider can not decide: $e")
      Supervision.Stop

  }

  implicit val materializer: ActorMaterializer = ActorMaterializer(
    ActorMaterializerSettings(actorSystem).withSupervisionStrategy(decider))

}

trait Conf {

  val origConf: Config = ConfigFactory.load()
  val overrides: Config = ConfigFactory.parseString(s"""
      # override seed node 0
      akka.cluster.seed-nodes.0="${origConf.getString("main.akkaSeed0")}"
      akka.cluster.seed-nodes.1="${origConf.getString("main.akkaSeed1")}"
      """)

  val conf: Config = overrides.withFallback(ConfigFactory.load())

  val appName: String = conf.getString("main.appName")
  val akkaSeed0: String = conf.getString("main.akkaSeed0")
  val akkaSeed1: String = conf.getString("main.akkaSeed1")

  val keyServiceShards: Int = conf.getInt("main.deviceServiceShards")

}
