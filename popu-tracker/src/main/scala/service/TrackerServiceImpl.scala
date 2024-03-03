package service
import akka.Done
import domain.Task
import domain.value_object.AccountId
import infrastructure.db.tracker.TaskRepository

import scala.concurrent.{ExecutionContext, Future}

class TrackerServiceImpl(taskRepository: TaskRepository)(implicit ec: ExecutionContext) extends TrackerService {

  override def create(task: Task): Future[Done] = taskRepository.insert(task).map(_ => Done)

  override def assign: Future[Done] = taskRepository.assign.map(_ => Done)

  override def list(implicit accId: AccountId): Future[Seq[Task]] = taskRepository.findAllFor(accId)
}

object TrackerServiceImpl {
  def apply(trackerRepository: TaskRepository)(implicit ec: ExecutionContext) = new TrackerServiceImpl(trackerRepository)
}
