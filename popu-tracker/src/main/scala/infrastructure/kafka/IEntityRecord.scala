package infrastructure.kafka

import domain.value_object.{BaseEntity, EntityId}
import infrastructure.kafka.EntityRecord.{EventTypeProperty, FieldMaskProperty, ProtocolVersionProperty}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader

import java.util.UUID

sealed trait IEntityRecord[Id <: EntityId, Entity <: BaseEntity[Id]] {

  /** Тип события */
  def eventType: EventType

  /** Заголовки записи сообщения в Kafka */
  def getHeaders: List[RecordHeader] = List(new RecordHeader(EventTypeProperty, eventType.toString.getBytes()))

  /** Получить запись сообщения в Kafka */
  def getRecord(topic: String): ProducerRecord[UUID, String]

  /** Идентификатор сущности */
  def getEntityId: Id

  /** Основные параметры записи */
  protected def mainParams: List[(String, String)] = List("headers" -> getHeaders.toString())

  /** Текстовое представление записи */
  def show: String = s"${getClass.getName}${mainParams.map { case (k, v) => s"$k=$v" }.mkString("(", ", ", ")")}"
}

/** Наличие в записи версии протокола */
trait IProtocolVersion {
  def protocolVersion: Int

  protected def versionHeader: RecordHeader =
    new RecordHeader(ProtocolVersionProperty, protocolVersion.toString.getBytes())
}

/**
 * Запись в Kafka о создании сущности
 *
 * @param entity          созданная сущность
 * @param protocolVersion версия протокола
 * @tparam Id     тип идентификатора сущности, распространяемой через Kafka
 * @tparam Entity тип сущности, распространяемой через Kafka
 */
final case class EntityCreatingRecord[Id <: EntityId, Entity <: BaseEntity[Id]](entity: String, protocolVersion: Int)
  extends IEntityRecord[Id, Entity]
    with IProtocolVersion {

  val eventType: EventType = Create

  override def getHeaders: List[RecordHeader] = versionHeader :: super.getHeaders

  def getEntity: String = entity.toString

  def getRecord(topic: String): ProducerRecord[UUID, String] = {
    val record = new ProducerRecord(topic, getEntityId.id, getEntity)
    getHeaders.foreach(record.headers().add)
    record
  }

  def getEntityId: Id = entity.entityId

  override protected def mainParams: List[(String, String)] =
    ("entity", entity.toString) :: ("protocolVersion", protocolVersion.toString) :: super.mainParams
}

/**
 * Запись в Kafka об обновлении сущности
 *
 * @param entityId        идентификатор обновляемой сущности
 * @param entity          обновленная сущность
 * @param protocolVersion версия протокола
 * @tparam Id     тип идентификатора сущности, распространяемой через Kafka
 * @tparam Entity тип сущности, распространяемой через Kafka
 */
final case class EntityUpdatingRecord[Id <: EntityId, Entity <: BaseEntity[Id]](
                                                                                 entityId: Id,
                                                                                 entity: Entity,
                                                                                 protocolVersion: Int)
  extends IEntityRecord[Id, Entity]
    with IProtocolVersion {

  val eventType: EventType = Update

  override def getHeaders: List[RecordHeader] = versionHeader :: super.getHeaders

  def getEntity: String = entity.toString

  def getRecord(topic: String): ProducerRecord[UUID, String] = {
    val record = new ProducerRecord(topic, getEntityId.id, getEntity)
    getHeaders.foreach(record.headers().add)
    record
  }

  def getEntityId: Id = entity.entityId

  override protected def mainParams: List[(String, String)] =
    ("entity", entity.toString) :: ("protocolVersion", protocolVersion.toString) :: super.mainParams
}

/**
 * Запись в Kafka об удалении сущности
 *
 * @param entityId идентификатор удаляемой сущности
 * @tparam Id     тип идентификатора сущности, распространяемой через Kafka
 * @tparam Entity тип сущности, распространяемой через Kafka
 */
final case class EntityDeletingRecord[Id <: EntityId, Entity <: BaseEntity[Id]](entityId: Id)
  extends IEntityRecord[Id, Entity] {

  val eventType: EventType = Delete

  def getRecord(topic: String): ProducerRecord[UUID, String] = {
    val record = new ProducerRecord(topic, entityId.id, null.asInstanceOf[String])
    getHeaders.foreach(record.headers().add)
    record
  }

  def getEntityId: Id = entityId

  override protected def mainParams: List[(String, String)] = ("entityId", entityId.toString) :: super.mainParams
}

object EntityRecord {
  val EventTypeProperty: String = "eventType"

  val ProtocolVersionProperty: String = "protocolVersion"

  val FieldMaskProperty: String = "fieldMask"

  def getProtocolVersion(record: ConsumerRecord[UUID, String]): Option[Int] =
    record.headers().toArray.collectFirst {
      case header if header.key() == ProtocolVersionProperty => new String(header.value()).toInt
    }

  def from[Id <: EntityId, Entity <: BaseEntity[Id]](
                                                      record: ConsumerRecord[UUID, String],
                                                      requiredProtocolVersion: Int
                                                    ): Option[IEntityRecord[Id, Entity]] = {
    val headers = record.headers().toArray

    def protocolVersion: Int = getProtocolVersion(record).getOrElse(
      throw new UnsupportedOperationException("Полученное сообщение не содержит информацию о версии протокола")
    )

    headers.find(_.key() == EventTypeProperty).map(h => new String(h.value())) match {
      case Some(EventType.Create) =>
        val version = protocolVersion
        if (version == requiredProtocolVersion) {
          val entity = record.value()
          Some(EntityCreatingRecord[Id, Entity](entity, version))
        } else
          None
      case Some(EventType.Update) =>
        val version = protocolVersion
        if (version == requiredProtocolVersion) {
          val entity = record.value()
          Some(EntityUpdatingRecord[Id, Entity](record.key(), entity, version))
        } else
          None
      case Some(EventType.Delete) =>
        Some(EntityDeletingRecord[Id, Entity](record.key()))
      case _ =>
        throw new UnsupportedOperationException("Полученное сообщение не содержит информацию о типе события")
    }
  }
}
