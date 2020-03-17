package me.skatz.kafka

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory
import me.skatz.database.Message
import me.skatz.utils.{Configuration, JsonHelper}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.Future

object KafkaProducer extends App {

  implicit val system: ActorSystem = ActorSystem("Producer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val config = ConfigFactory.load.getConfig("akka.kafka.producer")
  val producerSettings =
    ProducerSettings(config, new StringSerializer, new StringSerializer)
      .withBootstrapServers(Configuration.bootstrapServer)

  val producerSink: Future[Done] =
    Source(1 to 10)
      .map(_.toString)
      .map(value => new ProducerRecord[String, String](Configuration.topicName, JsonHelper.parseObject(Message(value.toInt, s"data: ${value}"))))
      .runWith(Producer.plainSink(producerSettings))

  println("************ Message produced ************")
}