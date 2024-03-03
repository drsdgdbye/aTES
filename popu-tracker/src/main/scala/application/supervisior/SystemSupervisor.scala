package application.supervisior

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import application.supervisior.SystemSupervisor._
import com.typesafe.config.Config
import slick.basic.DatabaseConfig
import slick.jdbc.PostgresProfile

trait SystemSupervisor {
  protected def init(
                      config: Config,
                      dbConfig: DatabaseConfig[PostgresProfile]
                    )(replyTo: ActorRef[Message]): Behavior[Message]

  protected def stop()(replyTo: ActorRef[Message]): Behavior[Message] = {
    replyTo ! Stopped
    Behaviors.stopped
  }

  def apply(
             config: Config,
             dbConfig: DatabaseConfig[PostgresProfile]
           ): Behavior[Message] = {
    Behaviors.receiveMessage {
      case Init(replyTo) => init(config, dbConfig)(replyTo)
      case Stop(replyTo) => stop()(replyTo)
    }
  }
}

object SystemSupervisor {
  sealed trait Message

  case class Init(replyTo: ActorRef[Message]) extends Message

  case class Stop(replyTo: ActorRef[Message]) extends Message

  case object InitSucceed extends Message

  case object Stopped extends Message
}
