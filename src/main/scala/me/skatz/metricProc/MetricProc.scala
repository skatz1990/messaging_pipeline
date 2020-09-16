package me.skatz.metricProc

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{CommitterSettings, ConsumerMessage, ProducerMessage, Subscriptions}
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph}
import akka.stream.{ActorMaterializer, ClosedShape}
import com.google.gson.GsonBuilder
import me.skatz.shared.metrics.{Metric, MetricDeserializer}
import me.skatz.shared.{Configuration, KafkaUtils}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

object MetricProc extends App {
  implicit val system: ActorSystem = ActorSystem("MetricProc")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val log: LoggingAdapter = Logging.getLogger(ActorSystem.create, this)

  val consumerSettings = KafkaUtils.configureConsumerSettings(new StringDeserializer, new StringDeserializer)
  val producerSettings = KafkaUtils.configureProducerSettings(new StringSerializer, new StringSerializer)
  val committerSettings = CommitterSettings(system).withMaxBatch(1L).withParallelism(1)
  log.info("MetricProc started")

  RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
    val source = Consumer.committableSource(consumerSettings, Subscriptions.topics(Configuration.metricProcSourceTopic))
    val map = Flow[ConsumerMessage.CommittableMessage[String, String]].map { msg =>
      val gson = new GsonBuilder()
        .registerTypeHierarchyAdapter(classOf[Metric], new MetricDeserializer)
        .setPrettyPrinting()
        .create()
      val result = gson.fromJson(msg.record.value, classOf[Metric])

      ProducerMessage.single(
        new ProducerRecord[String, String](Configuration.metricProcSinkTopic, result.toString),
        msg.committableOffset
      )
    }
    val sink = Producer.committableSink(producerSettings, committerSettings)

    source ~> map ~> sink
    ClosedShape
  }).run()
}
