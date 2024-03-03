package domain.value_object

import java.util.UUID

trait EntityId extends Any {
  def id: UUID

  override def toString: String = id.toString
}

object EntityId {
  val NullId = new UUID(0x0ffffffffffffffffL, 0x0ffffffffffffffffL)
}
