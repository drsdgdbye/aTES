package infrastructure.db.ecst

import akka.Done

import scala.concurrent.{ExecutionContext, Future}

abstract class OffsetStore[K, V, R, Passthrough](offsetProcessing: IOffsetProcessing)(implicit ec: ExecutionContext) {

  protected def businessLogic(key: K, value: V): (Future[R], Passthrough)

  def businessLogicAndStoreOffset(
                                   key: K,
                                   value: V,
                                   topic: String,
                                   partition: Int,
                                   offset: Long): (Future[R], Passthrough) = {
    val (future, passthrough) = businessLogic(key, value)
    (for {
      result <- future
      _ <- offsetProcessing.storePartitionWithOffset(topic, partition, offset)
    } yield result) -> passthrough
  }

  final def storePartitionWithOffset(topic: String, partition: Int, offset: Long): Future[Done] =
    offsetProcessing.storePartitionWithOffset(topic, partition, offset)

  final def loadPartitionWithOffsetMap(topic: String): Future[Map[Int, Long]] =
    offsetProcessing.loadPartitionWithOffsetMap(topic)

  final def getPartitionWithOffsetMap(topic: String): Future[Map[Int, Long]] =
    offsetProcessing.getPartitionWithOffsetMap(topic)
}
