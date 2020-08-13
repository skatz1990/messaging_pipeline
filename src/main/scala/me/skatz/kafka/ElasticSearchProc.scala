package me.skatz.kafka

import akka.actor.ActorSystem
import akka.kafka.javadsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.alpakka.elasticsearch.WriteMessage
import akka.stream.alpakka.elasticsearch.scaladsl.ElasticsearchSink
import akka.stream.scaladsl.Flow
import com.sksamuel.avro4s.AvroSchema
import com.typesafe.config.{Config, ConfigFactory}
import me.skatz.database.TweeterMessage
import me.skatz.utils.Configuration
import org.apache.avro.generic.{GenericDatumReader, GenericRecord}
import org.apache.avro.io.DecoderFactory
import org.apache.http.HttpHost
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.elasticsearch.client.RestClient
import spray.json.{DefaultJsonProtocol, JsonFormat}

object ElasticSearchProc extends App with DefaultJsonProtocol {
  implicit val system: ActorSystem = ActorSystem("ElasticSearchProc")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val client: RestClient = RestClient.builder(new HttpHost(Configuration.esUrl, Configuration.esPort.toInt)).build()
  implicit val format: JsonFormat[TweeterMessage] = jsonFormat4(TweeterMessage)

  // val consumerSettings = KafkaUtils.configureConsumerSettings()
  val consumerConfig: Config = ConfigFactory.load.getConfig("akka.kafka.consumer")
  val consumerSettings = ConsumerSettings(consumerConfig, new ByteArrayDeserializer, new ByteArrayDeserializer)
    .withBootstrapServers(Configuration.bootstrapServer)
    .withGroupId(Configuration.groupId)
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  val kafkaSource = Consumer.plainSource(consumerSettings, Subscriptions.topics(Configuration.enrichEsprocTopic))

  val intermediateFlow = Flow[ConsumerRecord[Array[Byte], Array[Byte]]].map { kafkaMessage =>
    val msg = getMessage(kafkaMessage.value)

    WriteMessage.createIndexMessage(msg.get)
  }

  val esSink = ElasticsearchSink.create[TweeterMessage](indexName = "kafka", typeName = "type name")
  kafkaSource.via(intermediateFlow).runWith(esSink, system)

  private def getMessage(message: Array[Byte]) = {
    val s = (message.map(_.toChar)).mkString

    val schema = AvroSchema[TweeterMessage]

    val reader: GenericDatumReader[GenericRecord] = new GenericDatumReader[GenericRecord](schema)
    val decoder = DecoderFactory.get().binaryDecoder(message, null)
    val generic: GenericRecord = reader.read(null, decoder)

    val tweeterMessage = TweeterMessage(
      generic.get("firstName").toString,
      generic.get("lastName").toString,
      generic.get("tweet").toString,
      generic.get("date").toString)

    Some(tweeterMessage)
  }
}
