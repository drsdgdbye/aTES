package application.supervisior

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import application.ecst.EcstConsumers
import application.migration.PostgresMigration
import application.supervisior.SystemSupervisor.{InitSucceed, Message}
import com.typesafe.config.Config
import infrastructure.rest.utils.JWTUtils
import infrastructure.rest.{RestEndpoint, Routes}
import slick.basic.DatabaseConfig
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.hikaricp.HikariCPJdbcDataSource

import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContextExecutor

object PopuTrackerSupervisor extends SystemSupervisor {
  override protected def init(
                               config: Config,
                               dbConfig: DatabaseConfig[PostgresProfile]
                             )(replyTo: ActorRef[Message]): Behavior[Message] = {
    Behaviors.setup[Message] { implicit context =>
      implicit val ec: ExecutionContextExecutor = context.executionContext
      implicit val system: ActorSystem[_] = context.system
      implicit val dc: DatabaseConfig[PostgresProfile] = dbConfig
      implicit val jwtUtils: JWTUtils = new JWTUtils(config.getConfig("jwt"))
      implicit val timeout: Timeout = Timeout(10, TimeUnit.SECONDS)

     if (config.getBoolean("migrations-enabled")) {
        PostgresMigration.migrate(dbConfig.db.source.asInstanceOf[HikariCPJdbcDataSource].ds)
      }

      val offsets = TableQuery[OffsetDataTable]
      val logs = TableQuery[DbEntityLogDataTable]

      lazy val accStore = AccountSlickStore(dbConfig.db, offsets, logs)

      val isKafkaEnabled = config.getConfig("kafka").getBoolean("enable")

      if (isKafkaEnabled) {
        EcstConsumers.startAll(accStore)
        EcstProducers.startAll
      }

      val restConfig = config.getConfig("akka.http.server")
      val host = restConfig.getString("host")
      val port = restConfig.getInt("port")

      val routes: Route = Routes()

      context.spawn(
        RestEndpoint(
          host,
          port,
          routes
        ), "rest-endpoint")

      context.log.info("PopuTracker is started")

      replyTo ! InitSucceed
      Behaviors.same
    }
  }
}
