package infrastructure.db.ecst

import akka.Done

import scala.concurrent.{ExecutionContext, Future}

abstract class IOffsetProcessing {

  protected def getPartitionsNumber: Int = 3

  def storePartitionWithOffset(topic: String, partition: Int, offset: Long)(implicit ec: ExecutionContext): Future[Done]

  def loadPartitionWithOffsetMap(topic: String)(implicit ec: ExecutionContext): Future[Map[Int, Long]]

  protected def initPartitionData(topic: String)(implicit ec: ExecutionContext): Future[Map[Int, Long]]

  final def getPartitionWithOffsetMap(topic: String)(implicit ec: ExecutionContext): Future[Map[Int, Long]] =
    loadPartitionWithOffsetMap(topic).flatMap {
      case map if map.isEmpty => initPartitionData(topic)
      case map                => Future.successful(map)
    }
}