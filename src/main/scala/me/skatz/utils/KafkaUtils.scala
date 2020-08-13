package me.skatz.utils

import akka.kafka.{ConsumerSettings, ProducerSettings}
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer, StringSerializer}

object KafkaUtils {
  def configureConsumerSettings(): ConsumerSettings[Array[Byte], String] = {
    val consumerConfig: Config = ConfigFactory.load.getConfig("akka.kafka.consumer")
    ConsumerSettings(consumerConfig, new ByteArrayDeserializer, new StringDeserializer)
      .withBootstrapServers(Configuration.bootstrapServer)
      .withGroupId(Configuration.groupId)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  }

  def configureProducerSettings(): ProducerSettings[String, String] = {
    val producerConfig = ConfigFactory.load.getConfig("akka.kafka.producer")
    ProducerSettings(producerConfig, new StringSerializer, new StringSerializer)
      .withBootstrapServers(Configuration.bootstrapServer)
  }
}
