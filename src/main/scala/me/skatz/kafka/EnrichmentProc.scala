package me.skatz.kafka

import akka.actor.ActorSystem
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{CommitterSettings, ProducerMessage, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import me.skatz.utils.{AvroMessageSerializer, Configuration, KafkaUtils}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringDeserializer, StringSerializer}
import spray.json.DefaultJsonProtocol

object EnrichmentProc extends App with DefaultJsonProtocol {
  implicit val system: ActorSystem = ActorSystem("EnrichmentProc")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val consumerSettings = KafkaUtils.configureConsumerSettings(new StringDeserializer, new StringDeserializer)
  val producerSettings = KafkaUtils.configureProducerSettings(new StringSerializer, new ByteArraySerializer)

  val committerSettings = CommitterSettings(system).withMaxBatch(1L).withParallelism(1)

  Consumer
    .committableSource(consumerSettings, Subscriptions.topics(Configuration.ingestEnrichTopic))
    .map { msg =>
      val byteArray = AvroMessageSerializer.jsonToGenericByteArray(msg.record.value())
      ProducerMessage.multi(
        List[ProducerRecord[String, Array[Byte]]](
          new ProducerRecord[String, Array[Byte]](Configuration.enrichEsprocTopic, byteArray),
          new ProducerRecord[String, Array[Byte]](Configuration.enrichCassTopic, byteArray)
        ),
        msg.committableOffset
      )
    }
    .toMat(Producer.committableSink(producerSettings, committerSettings))(Keep.both)
    .run()
}
