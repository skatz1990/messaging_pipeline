package me.skatz.kafka

import java.util.{Date, Properties}

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

object KafkaProducer extends App {
  println("Producer started")

  val props = new Properties()
  props.put("bootstrap.servers", "localhost:9092")
  props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  val producer = new KafkaProducer[Nothing, String](props)
  val TOPIC = "kafka-example"

  for (i <- 1 to 1000) {
    val message = s"Hello from Kafka land -> message: $i"
    val record = new ProducerRecord(TOPIC, message)
    producer.send(record)
  }

  producer.close()
  println("Producer completed")
}
