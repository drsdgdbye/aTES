package infrastructure.db.ecst

import akka.{Done, NotUsed}
import domain.value_object.{BaseEntity, EntityId}

import java.util.UUID
import scala.concurrent.ExecutionContext

abstract class EcstOffsetStore[Id <: EntityId, Entity <: BaseEntity[Id]](offsetProcessing: IOffsetProcessing)(implicit
                                                                                                              ec: ExecutionContext)
  extends OffsetStore[UUID, IEntityRecord[Id, Entity], Done, NotUsed](offsetProcessing)