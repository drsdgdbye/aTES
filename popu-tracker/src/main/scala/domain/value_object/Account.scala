package domain.value_object

case class Account(
                  entityId: AccountId,
                  entityVersion: EntityVersion,
                  fullName: String,
                  role: String
                  ) extends BaseEntity[AccountId]
