package me.skatz.kafka

import java.util
import java.util.Properties

import me.skatz.http.HttpClient
import me.skatz.models.Message
import me.skatz.utils.JsonHelper
import org.apache.kafka.clients.consumer.KafkaConsumer

import scala.collection.mutable

object Consumer {
  val batchSize: Int = 1
  val messageQueue: mutable.Queue[Message] = mutable.Queue[Message]()

  def main(args: Array[String]): Unit = {
    println("Consumer started")
    val props = configure()
    consumeFromKafka(props, sys.env.getOrElse("topic_name", "our_kafka_topic"))
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

        val message = new Message(next)
        this.messageQueue.enqueue(message)
        println(s"Consumed message: ${message.getData}")

        if (this.messageQueue.size == this.batchSize) {
          val postData = getPostData
          postBatch(postData)
        }
      }
    }
  }

  def postBatch(postData: String): Unit = {
    var url: String = "http://" + sys.env.getOrElse("elasticsearch_url", "localhost:9200")
    url += s"/samples/_doc"
    val result = HttpClient.post(url, postData)
    println(result)
  }

  def getPostData: String = {
    var jsonString = ""
    while (this.messageQueue.nonEmpty) {
      val currentMessage = this.messageQueue.dequeue()
      jsonString += JsonHelper.parseObject(currentMessage)
    }

    jsonString
  }
}
