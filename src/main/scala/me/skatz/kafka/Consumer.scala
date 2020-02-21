package me.skatz.kafka

import java.util
import java.util.Properties
import me.skatz.http.HttpClient
import me.skatz.models.Message
import me.skatz.utils.{Configuration, JsonHelper}
import org.apache.kafka.clients.consumer.KafkaConsumer

import scala.collection.mutable

object Consumer {
  val batchSize: Int = 10
  val messageQueue: mutable.Queue[Message] = mutable.Queue[Message]()

  def main(args: Array[String]): Unit = {
    println("Consumer started")
    val props = configure()
    consumeFromKafka(props, Configuration.topicName)
    println("Consumer completed")
  }

  def configure(): Properties = {
    val props = new Properties()
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("bootstrap.servers", Configuration.bootstrapServer)
    props.put("auto.offset.reset", Configuration.autoOffsetReset)
    props.put("group.id", Configuration.groupId)
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
    val url: String = "http://" + Configuration.esUrl + Configuration.esBulkEndpoint
    val result = HttpClient.post(url, postData)
    println(result)
  }

  def getPostData: String = {
    var jsonString = ""
    while (this.messageQueue.nonEmpty) {
      val currentMessage = this.messageQueue.dequeue()
      jsonString += "{ \"index\":{} }\r\n"
      jsonString += s"${JsonHelper.parseObject(currentMessage)}\r\n"
    }

    jsonString
  }
}
