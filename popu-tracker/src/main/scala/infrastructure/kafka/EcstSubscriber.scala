package infrastructure.kafka

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ConsumerSettings
import akka.kafka.scaladsl.Consumer.DrainingControl
import com.typesafe.config.Config
import domain.value_object.{BaseEntity, EntityId}
import infrastructure.db.ecst.EcstOffsetStore
import org.apache.kafka.common.serialization.{StringDeserializer, UUIDDeserializer}

import scala.concurrent.Future

object EcstSubscriber {
  /** Получить сообщения из топика */
  def receiveMessages[
    Id <: EntityId,
    Entity <: BaseEntity[Id],
  ](
     topicName: String,
     offsetStore: EcstOffsetStore[Id, Entity],
     consumerConf: Config,
     requiredProtocolVersion: Int
   )(implicit
     actorSystem: ActorSystem): Future[DrainingControl[Done]] =
    receive(
      topicName,
      offsetStore,
      ConsumerSettings(consumerConf, UUIDDeserializer, StringDeserializer),
      requiredProtocolVersion
    )
}
