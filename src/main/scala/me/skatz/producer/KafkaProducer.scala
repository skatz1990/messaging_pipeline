package me.skatz.producer

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Source}
import akka.stream.{ActorMaterializer, ClosedShape}
import me.skatz.producer.metrics.Metrics
import me.skatz.producer.utils.MessageGenerator
import me.skatz.shared.metrics.MetricUtils
import me.skatz.shared.{Configuration, JsonHelper, KafkaUtils}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

object KafkaProducer extends App {
  implicit val system: ActorSystem = ActorSystem("Producer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val log: LoggingAdapter = Logging.getLogger(ActorSystem.create, this)

  val producerSettings = KafkaUtils.configureProducerSettings(new StringSerializer, new StringSerializer)

  log.info("KafkaProducer started")

  RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
    val jointSource = Source(1 to MessageGenerator.numOfTweets)

    val pipelineMap = Flow[Int].map(_ =>
      new ProducerRecord[String, String](Configuration.ingestEnrichTopic, JsonHelper.parseObject(MessageGenerator.generateTweetMsg()))
    )
    val metricMap = Flow[Int].map(_ =>
      MetricUtils.createProducer(Metrics.MessagesSent)
    )

    val jointSink = MetricUtils.createSink()

    jointSource ~> pipelineMap ~> jointSink
    jointSource ~> metricMap ~> jointSink
    ClosedShape
  }).run()
}
