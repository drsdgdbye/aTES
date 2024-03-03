package infrastructure.kafka

import akka.Done
import akka.actor.typed.ActorSystem
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.stream.scaladsl
import akka.stream.scaladsl.Sink
import domain.value_object.{BaseEntity, EntityId}
import infrastructure.db.ecst.EcstOffsetStore
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition

import java.util.UUID
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.io.Source

/** Интерфейс подписчика на топик в Kafka для получения данных из другого сервиса */
trait SubscriberApi {

  /** Получить сообщения из топика */
  protected[ecst] def receiveWithDeserializing[Id <: EntityId, Entity <: BaseEntity[Id]](
                                                                                          topicName: String,
                                                                                          offsetStore: EcstOffsetStore[Id, Entity],
                                                                                          consumerSettings: ConsumerSettings[UUID, String],
                                                                                          requiredProtocolVersion: Int)(implicit
                                                                                                                        actorSystem: ActorSystem[_]
                                                                                        ): Future[DrainingControl[Done]] = {
    implicit val ec: ExecutionContextExecutor = actorSystem.executionContext
    offsetStore.getPartitionWithOffsetMap(topicName).map { partitionWithOffsetMap =>
      createConsumer(topicName, consumerSettings, partitionWithOffsetMap)
        .mapAsync(1) { record =>
          EntityRecord.from(record, requiredProtocolVersion) match {
            case Some(entity) =>
              offsetStore
                .businessLogicAndStoreOffset(record.key(), entity, topicName, record.partition(), record.offset())
                ._1
            case None =>
              offsetStore.storePartitionWithOffset(topicName, record.partition(), record.offset())
          }
        }
        .toMat(Sink.ignore)(DrainingControl.apply)
        .run()
    }
  }

  /** Консьюмер сообщений из топика с учетом переданных данных о партициях и офсетах */
  private def createConsumer(
                              topicName: String,
                              consumerSettings: ConsumerSettings[UUID, String],
                              partitionWithOffsetMap: Map[Int, Long]
                            ): scaladsl.Source[ConsumerRecord[UUID, String], Consumer.Control] =
    Consumer
      .plainSource(
        consumerSettings,
        Subscriptions.assignmentWithOffset(
          partitionWithOffsetMap.map { case (partition, offset) =>
            new TopicPartition(topicName, partition) -> offset
          }
        )
      )
}