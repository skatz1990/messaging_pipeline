package me.skatz.shared

import akka.kafka.{ConsumerSettings, ProducerSettings}
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{Deserializer, Serializer}

object KafkaUtils {

  def configureConsumerSettings[T, K](deserializerOne: Deserializer[T], deserializerTwo: Deserializer[K]): ConsumerSettings[T, K] = {
    val consumerConfig: Config = ConfigFactory.load.getConfig("akka.kafka.consumer")
    ConsumerSettings(consumerConfig, deserializerOne, deserializerTwo)
      .withBootstrapServers(Configuration.bootstrapServer)
      .withGroupId(Configuration.groupId)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  }

  def configureProducerSettings[T, K](serializerOne: Serializer[T], serializerTwo: Serializer[K]): ProducerSettings[T, K] = {
    val producerConfig = ConfigFactory.load.getConfig("akka.kafka.producer")
    ProducerSettings(producerConfig, serializerOne, serializerTwo)
      .withBootstrapServers(Configuration.bootstrapServer)
  }
}
