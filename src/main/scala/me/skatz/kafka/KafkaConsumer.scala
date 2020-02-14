package me.skatz.kafka

import java.util
import java.util.Properties
import org.apache.kafka.clients.consumer.KafkaConsumer

object KafkaConsumer {
  def main(args: Array[String]): Unit = {
    println("Consumer started")
    consumeFromKafka("kafka-example")
    println("Consumer completed")
  }
  def consumeFromKafka(topic: String): Unit = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("auto.offset.reset", "latest")
    props.put("group.id", "consumer-group")
    val consumer: KafkaConsumer[String, String] = new KafkaConsumer[String, String](props)
    consumer.subscribe(util.Arrays.asList(topic))
    while (true) {
      val record = consumer.poll(1000)
      val i = record.iterator
      while (i.hasNext)
        print(i.next.value() + "\r\n")
    }
    println("exiting")
  }
}
