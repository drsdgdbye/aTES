package domain.value_object

import infrastructure.rest._
import spray.json.RootJsonFormat
import spray.json.DefaultJsonProtocol._
import java.util.UUID

case class AccountId(id: UUID) extends EntityId

object AccountId extends EntityIdCompanion[AccountId] {
  implicit val accIdJsonFormat: RootJsonFormat[AccountId] = jsonFormat1(AccountId.apply)
}
