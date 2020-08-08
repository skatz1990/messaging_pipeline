package me.skatz.kafka

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory
import me.skatz.utils.{Configuration, JsonHelper, KafkaUtils, MessageGenerator}
import org.apache.kafka.clients.producer.ProducerRecord

import scala.concurrent.Future

object KafkaProducer extends App {
  implicit val system: ActorSystem = ActorSystem("Producer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val producerSettings = KafkaUtils.configureProducerSettings()

  val producerSink: Future[Done] =
    Source(1 to MessageGenerator.numOfTweets)
      .map(_ => new ProducerRecord[String, String](
        Configuration.ingestEnrichTopic,
        JsonHelper.parseObject(MessageGenerator.generateTweetMsg())))
      .runWith(Producer.plainSink(producerSettings))

  println("************ Message produced ************")
}
