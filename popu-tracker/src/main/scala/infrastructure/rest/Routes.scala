package infrastructure.rest

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import domain.Task
import domain.value_object.AccountId
import infrastructure.rest.utils.JWTUtils
import org.postgresql.util.PSQLException
import service.TrackerService
import spray.json.DefaultJsonProtocol._
import spray.json.DeserializationException

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

class Routes(trackerService: TrackerService
            )(implicit system: ActorSystem[_], timeout: Timeout, jwtUtils: JWTUtils) {
  implicit val ec: ExecutionContextExecutor = system.executionContext

  private val corsSettings: CorsSettings = CorsSettings.defaultSettings.withAllowedMethods(Seq(GET, PATCH, POST, OPTIONS, DELETE))

  private def createTask: Route = (post & entity(as[Task])) { newTask =>
    complete(StatusCodes.Created, trackerService.create(newTask))
  }

  private def assignTasks: Route = (path("assign")) {
    pathEndOrSingleSlash {
      complete(StatusCodes.OK, trackerService.assign)
    }
  }

  private def list(implicit accountId: AccountId): Route = pathEndOrSingleSlash {
    complete(StatusCodes.OK, trackerService.list)
  }

  private def trackerRoutes(implicit accountId: AccountId): Route = createTask ~ get { assignTasks ~ list }

  val routes: Route = cors(corsSettings) {
    Route.seal(
      pathPrefix("api" / "popu-tracker" / "v1") {
        cookie("token") { cookie =>
          jwtUtils.verifyJWT(cookie.value) match {
            case Success(value) =>
              implicit val accId: AccountId = AccountId.fromString(value.getClaim("accountId").asString())
              trackerRoutes
            case Failure(_) =>
              complete(StatusCodes.Unauthorized, "Token validation failed")
          }
        }
      }
    )
  }

  implicit def exceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case ex: DeserializationException =>
        complete(StatusCodes.BadRequest, ex.msg)
      case ex: IllegalArgumentException =>
        complete(StatusCodes.BadRequest, ex.getMessage)
      case ex: PSQLException =>
        complete(StatusCodes.BadRequest, ex.getMessage)
    }
}

object Routes {
  def apply(trackerService: TrackerService)(
    implicit system: ActorSystem[_], timeout: Timeout, jwtUtils: JWTUtils) = new Routes(trackerService)
}

