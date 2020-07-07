package me.skatz.kafka

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Transactional
import akka.kafka.{ConsumerSettings, ProducerMessage, ProducerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import com.google.gson.Gson
import com.typesafe.config.{Config, ConfigFactory}
import me.skatz.database.TweeterMessage
import me.skatz.utils.Configuration
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer, StringSerializer}
import spray.json.DefaultJsonProtocol

object EnrichmentProc extends App with DefaultJsonProtocol {
  implicit val system: ActorSystem = ActorSystem("EnrichmentProc")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  // Consume message
  val consumerConfig: Config = ConfigFactory.load.getConfig("akka.kafka.consumer")
  val consumerSettings: ConsumerSettings[Array[Byte], String] = ConsumerSettings(consumerConfig, new ByteArrayDeserializer, new StringDeserializer)
    .withBootstrapServers(Configuration.bootstrapServer)
    .withGroupId(Configuration.groupId)
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  // Produce
  val producerConfig = ConfigFactory.load.getConfig("akka.kafka.producer")
  val producerSettings =
    ProducerSettings(producerConfig, new StringSerializer, new StringSerializer)
      .withBootstrapServers(Configuration.bootstrapServer)

  val intermediateFlow = Flow[ConsumerRecord[Array[Byte], String]].map { kafkaMessage =>
    val gson = new Gson

    // Deserialize JSON
    val message = gson.fromJson(kafkaMessage.value(), classOf[TweeterMessage])
    println(message)
  }

  Transactional
    .source(consumerSettings, Subscriptions.topics(Configuration.ingestEnrichTopic))
    //.via(businessFlow)
    .map { msg =>
      ProducerMessage.multi(
        List[ProducerRecord[String, String]](
          new ProducerRecord[String, String](
            Configuration.enrichEsprocTopic,
            msg.record.value
          ),
          new ProducerRecord[String, String](
            Configuration.enrichCassTopic,
            msg.record.value
          )
        ),
        msg.partitionOffset
      )
    }
    .to(Transactional.sink(producerSettings, "transactional-id"))
    .run()

  // def businessFlow[T]: Flow[T, T, NotUsed] = Flow[T]
  //  Transactional
  //    .source(consumerSettings, Subscriptions.topics("sourceTopic"))
  //    .via(
  //      Flow[ConsumerMessage.TransactionalMessage[String, DestinationMessage]]
  //        .map { msg =>
  //          ProducerMessage.multi(
  //            List[ProducerRecord[String, DestinationMessage]](
  //              new ProducerRecord(
  //                "destinationTopic",
  //                msg.record.key,
  //                DestinationMessage("foo")
  //              )
  //            ),
  //            msg.partitionOffset
  //          )
  //        }
  //    )
  //    .via(Transactional.flow(producerSettings, transactionalId))

  //  val control =
  //    Transactional
  //      .source(consumerSettings, Subscriptions.topics(Configuration.ingestEnrichTopic))
  //      // .via(intermediateFlow)
  //      .map { msg =>
  //        ProducerMessage.single(new ProducerRecord(Configuration.enrichCassTopic, msg.record.key, msg.record.value), msg.partitionOffset)
  //      }
  //      .toMat(Transactional.sink(producerSettings, "123"))(DrainingControl.apply)
  //      .run()
}

