package me.messaging.LTProc

import java.io.{BufferedWriter, File, FileWriter}

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.kafka.Subscriptions
import akka.kafka.scaladsl.Consumer
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Sink}
import akka.stream.{ActorMaterializer, ClosedShape}
import com.amazonaws.auth.{AWSStaticCredentialsProvider, AnonymousAWSCredentials}
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import me.messaging.LTProc.metrics.Metrics.Metrics
import me.messaging.shared.metrics.MetricUtils
import me.messaging.shared.{AvroMessageSerializer, Configuration, KafkaUtils}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.ByteArrayDeserializer

import scala.concurrent.ExecutionContextExecutor

object LTProc extends App {
  implicit val system: ActorSystem = ActorSystem("LTProc")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val log: LoggingAdapter = Logging.getLogger(ActorSystem.create, this)
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val consumerSettings = KafkaUtils.configureConsumerSettings(new ByteArrayDeserializer, new ByteArrayDeserializer)
  log.info("LT Proc started")

  val endpoint = new EndpointConfiguration(Configuration.s3Url, Configuration.s3Region)
  val client = AmazonS3ClientBuilder
    .standard
    .withPathStyleAccessEnabled(true)
    .withEndpointConfiguration(endpoint)
    .withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))
    .build

  client.createBucket(Configuration.s3BucketName)

  RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
    val jointSource = Consumer.plainSource(consumerSettings, Subscriptions.topics(Configuration.enrichLTTopic))

    val pipelineFlow = Flow[ConsumerRecord[Array[Byte], Array[Byte]]].map { kafkaMessage =>
      val msg = AvroMessageSerializer.tweeterByteArrayToMessage(kafkaMessage.value).get
      val fileName = "tweets.txt"

      val file = new File(fileName)
      val bw = new BufferedWriter(new FileWriter(file, true))
      bw.write(s"${msg.firstName}:${msg.lastName}:${msg.tweet}\r\n")
      bw.close()
      client.putObject(Configuration.s3BucketName, fileName, file)
    }

    val pipelineSink = Sink.ignore

    val metricMap = Flow[ConsumerRecord[Array[Byte], Array[Byte]]].map(_ =>
      MetricUtils.createProducer(Metrics.MessagesSent)
    )

    val metricSink = MetricUtils.createSink()

    jointSource ~> pipelineFlow ~> pipelineSink
    jointSource ~> metricMap ~> metricSink
    ClosedShape
  }).run()
}


