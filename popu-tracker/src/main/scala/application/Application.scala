package application

import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.{ActorRef, ActorSystem, Props, SpawnProtocol}
import akka.util.Timeout
import application.supervisior.PopuTrackerSupervisor
import application.supervisior.SystemSupervisor._
import com.typesafe.config.{Config, ConfigFactory}
import slick.basic.DatabaseConfig
import slick.jdbc.PostgresProfile

import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContextExecutor

object Application extends App {
  implicit val system: ActorSystem[SpawnProtocol.Command] =
    ActorSystem[SpawnProtocol.Command](SpawnProtocol(), "supervisor")

  implicit val exec: ExecutionContextExecutor = system.executionContext
  implicit val timeout: Timeout = Timeout(10, TimeUnit.SECONDS)

  val config: Config = ConfigFactory.load()
  val dbConfig: DatabaseConfig[PostgresProfile] = DatabaseConfig.forConfig("postgres", config)

  private val supervisor = system ? { ref: ActorRef[ActorRef[Message]] =>
    SpawnProtocol.Spawn(PopuTrackerSupervisor(config, dbConfig), "popu-tracker-supervisor", Props.empty, ref)
  }

  supervisor.flatMap(_ ? [Message] { replyTo => Init(replyTo) })
}
