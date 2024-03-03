package domain.value_object

import java.util.UUID

trait EntityIdCompanion[ID <: EntityId] {
  implicit def companion: EntityIdCompanion[ID] = this

  val NullObject: ID = apply(EntityId.NullId)

  def fromString(str: String): ID = apply(UUID.fromString(str))

  def apply(id: UUID): ID
}
