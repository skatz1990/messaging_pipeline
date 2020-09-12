package me.skatz.metricProc

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{CommitterSettings, ConsumerMessage, ProducerMessage, Subscriptions}
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl.{Flow, GraphDSL, Keep, RunnableGraph}
import me.skatz.shared.{AvroMessageSerializer, Configuration, KafkaUtils}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringDeserializer, StringSerializer}
import GraphDSL.Implicits._

object MetricProc extends App {
  implicit val system: ActorSystem = ActorSystem("MetricProc")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val log: LoggingAdapter = Logging.getLogger(ActorSystem.create, this)

  val consumerSettings = KafkaUtils.configureConsumerSettings(new StringDeserializer, new StringDeserializer)
  val producerSettings = KafkaUtils.configureProducerSettings(new StringSerializer, new ByteArraySerializer)
  val committerSettings = CommitterSettings(system).withMaxBatch(1L).withParallelism(1)
  log.info("MetricProc started")

  RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
    val source = Consumer.committableSource(consumerSettings, Subscriptions.topics(Configuration.metricProcSourceTopic))
    val map = Flow[ConsumerMessage.CommittableMessage[String, String]].map { msg =>
      val byteArray = AvroMessageSerializer.jsonToGenericByteArray(msg.record.value())
      ProducerMessage.single(
        new ProducerRecord[String, Array[Byte]](Configuration.metricProcSinkTopic, byteArray),
        msg.committableOffset
      )
    }
    val sink = Producer.committableSink(producerSettings, committerSettings)

    source ~> map ~> sink
    ClosedShape
  }).run()
}
