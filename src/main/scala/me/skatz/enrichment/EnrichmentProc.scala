package me.skatz.enrichment

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{CommitterSettings, ConsumerMessage, ProducerMessage, Subscriptions}
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph}
import akka.stream.{ActorMaterializer, ClosedShape}
import me.skatz.enrichment.metrics.Metrics
import me.skatz.shared.metrics.MetricUtils
import me.skatz.shared.{AvroMessageSerializer, Configuration, KafkaUtils}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringDeserializer, StringSerializer}

object EnrichmentProc extends App {
  implicit val system: ActorSystem = ActorSystem("EnrichmentProc")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val log: LoggingAdapter = Logging.getLogger(ActorSystem.create, this)

  val consumerSettings = KafkaUtils.configureConsumerSettings(new StringDeserializer, new StringDeserializer)
  val producerSettings = KafkaUtils.configureProducerSettings(new StringSerializer, new ByteArraySerializer)
  val committerSettings = CommitterSettings(system).withMaxBatch(1L).withParallelism(1)
  log.info("EnrichmentProc started")

  RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
    val jointSource = Consumer.committableSource(consumerSettings, Subscriptions.topics(Configuration.ingestEnrichTopic))

    val pipelineMap = Flow[ConsumerMessage.CommittableMessage[String, String]].map { msg =>
      val byteArray = AvroMessageSerializer.jsonToTweeterByteArray(msg.record.value())
      ProducerMessage.multi(
        List[ProducerRecord[String, Array[Byte]]](
          new ProducerRecord[String, Array[Byte]](Configuration.enrichEsprocTopic, byteArray),
          new ProducerRecord[String, Array[Byte]](Configuration.enrichCassTopic, byteArray),
          new ProducerRecord[String, Array[Byte]](Configuration.enrichLTTopic, byteArray)
        ),
        msg.committableOffset
      )
    }
    val metricMap = Flow[ConsumerMessage.CommittableMessage[String, String]].map(_ =>
      MetricUtils.createProducer(Metrics.MessagesSent)
    )

    val pipelineSink = Producer.committableSink(producerSettings, committerSettings)
    val metricSink = MetricUtils.createSink()

    jointSource ~> pipelineMap ~> pipelineSink
    jointSource ~> metricMap ~> metricSink
    ClosedShape
  }).run()
}
