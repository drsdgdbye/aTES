package domain.value_object

trait BaseEntity[Id <: EntityId] {

  def entityId: Id

  def entityVersion: EntityVersion
}
