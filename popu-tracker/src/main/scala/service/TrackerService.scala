package service

import akka.Done
import domain.Task
import domain.value_object.AccountId

import scala.concurrent.Future

trait TrackerService {
  def create(task: Task): Future[Done]

  def assign: Future[Done]

  def list(implicit accId: AccountId): Future[Seq[Task]]
}
