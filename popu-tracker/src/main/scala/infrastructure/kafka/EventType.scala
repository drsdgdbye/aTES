package infrastructure.kafka

sealed trait EventType

case object Create extends EventType {
  override def toString(): String = EventType.Create
}

case object Update extends EventType {
  override def toString(): String = EventType.Update
}

case object Delete extends EventType {
  override def toString(): String = EventType.Delete
}

object EventType {
  val Create: String = "create"
  val Update: String = "update"
  val Delete: String = "delete"
}