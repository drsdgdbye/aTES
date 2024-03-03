package application.ecst

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import com.typesafe.config.Config
import infrastructure.db.ecst.AccountSlickStore
import infrastructure.kafka.AccountEcstConsumer
import org.slf4j.LoggerFactory

import scala.concurrent.Future

object EcstConsumers {
  def startAll(
                accountStore: AccountSlickStore
              )(implicit sys: ActorSystem[_]): Future[Seq[Consumer.DrainingControl[Done]]] = {
    val logger = LoggerFactory.getLogger("EcstConsumers")
    val config = sys.settings.config.getConfig("akka.kafka")
    val topicConfig = config.getConfig("topics")
    val consumerConfig: Config = config.getConfig("consumer")

    val accountTopic = topicConfig.getString("account")

    AccountEcstConsumer(accountTopic, consumerConfig, accountStore)
  }
}
