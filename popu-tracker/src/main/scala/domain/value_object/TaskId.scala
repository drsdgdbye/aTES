package domain.value_object

import spray.json.DefaultJsonProtocol.jsonFormat1
import spray.json.RootJsonFormat
import infrastructure.rest._
import java.util.UUID

case class TaskId(id: UUID) extends EntityId

object TaskId extends EntityIdCompanion[TaskId] {
  implicit val taskIdJsonFormat: RootJsonFormat[TaskId] = jsonFormat1(TaskId.apply)
}
