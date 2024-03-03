package infrastructure.rest

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior, PostStop}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route

import scala.concurrent.Future
import scala.util.{Failure, Success}

object RestEndpoint {
  sealed trait Message

  private final case class Started(binding: ServerBinding) extends Message
  private final case class StartFailed(cause: Throwable) extends Message
  case object Stop extends Message

  def apply(
    host: String,
    port: Int,
    routes: Route
  ): Behavior[Message] =
    Behaviors.setup[Message]{ implicit context =>
      implicit val system: ActorSystem[_] = context.system

      val bindingFuture: Future[Http.ServerBinding] =
        Http().newServerAt(host, port)
          .bind(routes)

      context.pipeToSelf(bindingFuture) {
        case Success(binding) => Started(binding)
        case Failure(exception) => StartFailed(exception)
      }

      def running(binding: ServerBinding): Behavior[Message] =
        Behaviors.receiveMessagePartial[Message] {
          case Stop =>
            context.log.info("Stopping REST Endpoint at http://{}", binding.localAddress)
            Behaviors.stopped
        }.receiveSignal {
          case (_, PostStop) =>
            binding.unbind()
            Behaviors.same
        }

      def starting(wasStopped: Boolean): Behaviors.Receive[Message] =
        Behaviors.receiveMessage[Message] {
          case StartFailed(cause) =>
            throw new RuntimeException("Exception at startup REST Endpoint", cause)
          case Started(binding) =>
            context.log.info("REST Endpoint started at http://{}", binding.localAddress)
            if (wasStopped) {
              context.self ! Stop
            }
            running(binding)
          case Stop =>
            starting(wasStopped = true)
        }

      starting(wasStopped = false)
    }
  }

