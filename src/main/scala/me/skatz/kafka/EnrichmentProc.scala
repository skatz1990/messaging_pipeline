package me.skatz.kafka

import akka.actor.ActorSystem
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ConsumerSettings, ProducerMessage, ProducerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import com.typesafe.config.{Config, ConfigFactory}
import me.skatz.utils.Configuration
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer, StringSerializer}
import spray.json.DefaultJsonProtocol

object EnrichmentProc extends App with DefaultJsonProtocol {
  implicit val system: ActorSystem = ActorSystem("EnrichmentProc")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val consumerConfig: Config = ConfigFactory.load.getConfig("akka.kafka.consumer")
  val consumerSettings = ConsumerSettings(consumerConfig, new ByteArrayDeserializer, new StringDeserializer)
    .withBootstrapServers(Configuration.bootstrapServer)
    .withGroupId(Configuration.groupId)
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  val producerConfig = ConfigFactory.load.getConfig("akka.kafka.producer")
  val producerSettings = ProducerSettings(producerConfig, new StringSerializer, new StringSerializer)
    .withBootstrapServers(Configuration.bootstrapServer)

  Consumer
    .committableSource(consumerSettings, Subscriptions.topics(Configuration.ingestEnrichTopic))
    .map { msg =>
      ProducerMessage.multi(
        List[ProducerRecord[String, String]](
          new ProducerRecord[String, String](Configuration.enrichEsprocTopic, msg.record.value),
          new ProducerRecord[String, String](Configuration.enrichCassTopic, msg.record.value)
        ),
        msg.committableOffset
      )
    }
    .toMat(Producer.committableSink(producerSettings))(Keep.both)
    .run()
}
