package infrastructure.kafka

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer.DrainingControl
import domain.value_object.{Account, AccountId}
import infrastructure.db.ecst.EcstOffsetStore
import org.apache.kafka.clients.consumer.ConsumerConfig

import scala.concurrent.Future

class AccountEcstConsumer(
                           topic: String,
                           consumerConfig: ConsumerConfig,
                           store: EcstOffsetStore[AccountId, Account]
                         )(implicit sys: ActorSystem) {
  def read(): Future[DrainingControl[Done]] =
    EcstSubscriber.receiveMessages(
      topic,
      store,
      consumerConfig,
      1
    )
}

object AccountEcstConsumer {
  def apply(topic: String, consumerConfig: ConsumerConfig, store: EcstOffsetStore[AccountId, Account])(implicit sys: ActorSystem) =
    new AccountEcstConsumer(topic, consumerConfig, store)
}
