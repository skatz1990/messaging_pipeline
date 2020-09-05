package me.skatz.producer

import akka.Done
import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import me.skatz.producer.utils.MessageGenerator
import me.skatz.shared.{Configuration, JsonHelper, KafkaUtils}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.Future

object KafkaProducer extends App {
  implicit val system: ActorSystem = ActorSystem("Producer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val log: LoggingAdapter = Logging.getLogger(ActorSystem.create, this)

  val producerSettings = KafkaUtils.configureProducerSettings(new StringSerializer, new StringSerializer)

  log.info("KafkaProducer started")

  val producerSink: Future[Done] =
    Source(1 to MessageGenerator.numOfTweets)
      .map(_ => new ProducerRecord[String, String](
        Configuration.ingestEnrichTopic,
        JsonHelper.parseObject(MessageGenerator.generateTweetMsg()))
      )
      .runWith(Producer.plainSink(producerSettings))
}
