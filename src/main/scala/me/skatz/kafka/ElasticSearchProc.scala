package me.skatz.kafka

import akka.actor.ActorSystem
import akka.kafka.Subscriptions
import akka.kafka.javadsl.Consumer
import akka.stream.ActorMaterializer
import akka.stream.alpakka.elasticsearch.WriteMessage
import akka.stream.alpakka.elasticsearch.scaladsl.ElasticsearchSink
import akka.stream.scaladsl.Flow
import com.google.gson.Gson
import me.skatz.database.TweeterMessage
import me.skatz.utils.{Configuration, KafkaUtils}
import org.apache.http.HttpHost
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.elasticsearch.client.RestClient
import spray.json.{DefaultJsonProtocol, JsonFormat}

object ElasticSearchProc extends App with DefaultJsonProtocol {
  implicit val system: ActorSystem = ActorSystem("ElasticSearchProc")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val client: RestClient = RestClient.builder(new HttpHost(Configuration.esUrl, Configuration.esPort.toInt)).build()
  implicit val format: JsonFormat[TweeterMessage] = jsonFormat4(TweeterMessage)

  val consumerSettings = KafkaUtils.configureConsumerSettings()

  val kafkaSource = Consumer.plainSource(consumerSettings, Subscriptions.topics(Configuration.enrichEsprocTopic))

  val intermediateFlow = Flow[ConsumerRecord[Array[Byte], String]].map { kafkaMessage =>
    val gson = new Gson

    // Deserialize JSON
    val message = gson.fromJson(kafkaMessage.value(), classOf[TweeterMessage])
    WriteMessage.createIndexMessage(message)
  }

  val esSink = ElasticsearchSink.create[TweeterMessage](indexName = "kafka", typeName = "type name")
  kafkaSource.via(intermediateFlow).runWith(esSink, system)
}
