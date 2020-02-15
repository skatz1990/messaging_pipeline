package me.skatz.kafka

import java.util
import java.util.Properties
import org.apache.kafka.clients.consumer.KafkaConsumer

object Consumer {

  def main(args: Array[String]): Unit = {
    println("Consumer started")
    val props = configure()
    consumeFromKafka(props, Config().getString("kafka_app.topic"))
    println("Consumer completed")
  }

  def configure(): Properties = {
    val props = new Properties()
    props.put("bootstrap.servers", Config().getString("kafka_app.bootstrap.servers"))
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("auto.offset.reset", Config().getString("kafka_app.auto.offset.reset"))
    props.put("group.id", Config().getString("kafka_app.group.id"))

    props
  }

  def consumeFromKafka(props: Properties, topic: String): Unit = {
    val consumer: KafkaConsumer[String, String] = new KafkaConsumer[String, String](props)
    consumer.subscribe(util.Arrays.asList(topic))

    while (true) {
      val record = consumer.poll(1000)
      val i = record.iterator
      while (i.hasNext)
        print(i.next.value() + "\r\n")
    }
  }
}
