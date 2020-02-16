package me.skatz.kafka

import java.util
import java.util.Properties
import org.apache.kafka.clients.consumer.KafkaConsumer

object Consumer {

  def main(args: Array[String]): Unit = {
    println("Consumer started")
    val props = configure()
    consumeFromKafka(props, "kafka-example")
    println("Consumer completed")
  }

  def configure(): Properties = {
    val props = new Properties()
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("bootstrap.servers", sys.env.getOrElse("bootstrap_servers", "localhost:9092"))
    props.put("auto.offset.reset", sys.env.getOrElse("auto_offset_reset", "latest"))
    props.put("group.id", sys.env.getOrElse("group_id", "consumer-group"))
    props
  }

  def consumeFromKafka(props: Properties, topic: String): Unit = {
    val consumer: KafkaConsumer[String, String] = new KafkaConsumer[String, String](props)
    consumer.subscribe(util.Arrays.asList(topic))

    while (true) {
      val record = consumer.poll(1000)
      val i = record.iterator
      while (i.hasNext) {
        val next = i.next.value()
        println(s"CONSUMED: $next\r\n")
      }
    }
  }
}
