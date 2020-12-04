package me.messaging.esProc

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.kafka.Subscriptions
import akka.kafka.javadsl.Consumer
import akka.stream.alpakka.elasticsearch.WriteMessage
import akka.stream.alpakka.elasticsearch.scaladsl.ElasticsearchSink
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph}
import akka.stream.{ActorMaterializer, ClosedShape}
import me.messaging.cassandraProc.database.TweeterMessage
import me.messaging.shared.{AvroMessageSerializer, Configuration, KafkaUtils}
import org.apache.http.HttpHost
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.elasticsearch.client.RestClient
import spray.json.{DefaultJsonProtocol, JsonFormat}
import GraphDSL.Implicits._
import me.messaging.esProc.metrics.Metrics
import me.messaging.shared.metrics.MetricUtils

object ElasticSearchProc extends App with DefaultJsonProtocol {
  implicit val system: ActorSystem = ActorSystem("ElasticSearchProc")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val client: RestClient = RestClient.builder(new HttpHost(Configuration.esUrl, Configuration.esPort.toInt)).build()
  implicit val format: JsonFormat[TweeterMessage] = jsonFormat4(TweeterMessage)
  implicit val log: LoggingAdapter = Logging.getLogger(ActorSystem.create, this)

  val consumerSettings = KafkaUtils.configureConsumerSettings(new ByteArrayDeserializer, new ByteArrayDeserializer)
  log.info("ElasticSearchProc started")

  RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
    val jointSource = Consumer.plainSource(consumerSettings, Subscriptions.topics(Configuration.enrichEsprocTopic))

    val pipelineMap = Flow[ConsumerRecord[Array[Byte], Array[Byte]]].map { kafkaMessage =>
      val msg = AvroMessageSerializer.tweeterByteArrayToMessage(kafkaMessage.value)
      WriteMessage.createIndexMessage(msg.get)
    }
    val metricMap = Flow[ConsumerRecord[Array[Byte], Array[Byte]]].map(_ =>
      MetricUtils.createProducer(Metrics.MessagesSent)
    )

    val pipelineSink = ElasticsearchSink.create[TweeterMessage](indexName = "kafka", typeName = "type name")
    val metricSink = MetricUtils.createSink()

    jointSource ~> pipelineMap ~> pipelineSink
    jointSource ~> metricMap ~> metricSink
    ClosedShape
  }).run()
}
