package infrastructure.db.tracker

import domain.Task
import domain.value_object.AccountId

import scala.concurrent.Future

// todo impl
trait TaskRepository {
  def insert(task: Task): Future[Int]

  def assign: Future[Int]

  def findAllFor(accountId: AccountId): Future[Seq[Task]]
}
