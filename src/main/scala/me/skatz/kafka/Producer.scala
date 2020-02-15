package me.skatz.kafka

import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

object Producer {

  def main(args: Array[String]): Unit = {
    println("Producer started")
    val props = configure()
    produceToKafka(props, Config().getString("kafka_app.topic"))
    println("Producer completed")
  }

  def configure(): Properties = {
    val props = new Properties()
    props.put("bootstrap.servers", Config().getString("kafka_app.bootstrap.servers"))
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    props
  }

  def produceToKafka(props: Properties, topic: String): Unit = {
    val producer = new KafkaProducer[Nothing, String](props)

    for (i <- 1 to 1000) {
      val message = s"Hello from Kafka land -> message: $i"
      val record = new ProducerRecord(topic, message)
      producer.send(record)
    }

    producer.close()
  }
}
