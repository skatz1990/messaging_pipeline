package me.skatz.kafka

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.kafka.Subscriptions
import akka.kafka.javadsl.Consumer
import akka.stream.ActorMaterializer
import akka.stream.alpakka.elasticsearch.WriteMessage
import akka.stream.alpakka.elasticsearch.scaladsl.ElasticsearchSink
import akka.stream.scaladsl.Flow
import me.skatz.database.TweeterMessage
import me.skatz.utils.{AvroMessageSerializer, Configuration, KafkaUtils}
import org.apache.http.HttpHost
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.elasticsearch.client.RestClient
import spray.json.{DefaultJsonProtocol, JsonFormat}

object ElasticSearchProc extends App with DefaultJsonProtocol {
  implicit val system: ActorSystem = ActorSystem("ElasticSearchProc")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val client: RestClient = RestClient.builder(new HttpHost(Configuration.esUrl, Configuration.esPort.toInt)).build()
  implicit val format: JsonFormat[TweeterMessage] = jsonFormat4(TweeterMessage)
  implicit val log: LoggingAdapter = Logging.getLogger(ActorSystem.create,this)

  val consumerSettings = KafkaUtils.configureConsumerSettings(new ByteArrayDeserializer, new ByteArrayDeserializer)
  log.info("ElasticSearchProc started")

  Consumer
    .plainSource(consumerSettings, Subscriptions.topics(Configuration.enrichEsprocTopic))
    .via(Flow[ConsumerRecord[Array[Byte], Array[Byte]]].map { kafkaMessage =>
      val msg = AvroMessageSerializer.genericByteArrayToMessage(kafkaMessage.value)
      WriteMessage.createIndexMessage(msg.get)
    })
    .runWith(ElasticsearchSink.create[TweeterMessage](indexName = "kafka", typeName = "type name"), system)
}
