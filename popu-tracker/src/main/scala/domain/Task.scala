package domain

import domain.value_object.{AccountId, TaskId}
import spray.json.RootJsonFormat
import spray.json.DefaultJsonProtocol._

import java.time.Instant
import domain.value_object.AccountId.accIdJsonFormat
import infrastructure.rest._

case class Task(
               entityId: TaskId,
               title: String,
               createdAt: Instant,
               assignedTo: AccountId,
               isDone: Boolean
               )

object Task {
  implicit val taskJsonFormat: RootJsonFormat[Task] = jsonFormat5(Task.apply)
}
