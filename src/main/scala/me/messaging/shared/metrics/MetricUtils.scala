package me.messaging.shared.metrics

import akka.Done
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.Sink
import com.google.gson.GsonBuilder
import me.messaging.shared.{Configuration, JsonHelper, KafkaUtils}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.Future

object MetricUtils {
  def createProducer(metric: Metric): ProducerRecord[String, String] = {
    new ProducerRecord[String, String](
      Configuration.metricProcSourceTopic,
      JsonHelper.parseObject(
        new GsonBuilder()
          .registerTypeHierarchyAdapter(classOf[Metric], new MetricSerializer)
          .setPrettyPrinting()
          .create
          .toJson(metric)
      )
    )
  }

  def createSink(): Sink[ProducerRecord[String, String], Future[Done]] = {
    val producerSettings = KafkaUtils.configureProducerSettings(new StringSerializer, new StringSerializer)
    Producer.plainSink(producerSettings)
  }
}
