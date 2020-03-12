package me.skatz.kafka

import java.util
import java.util.Properties

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{Behavior, PostStop, Signal}
import me.skatz.http.HttpClient
import me.skatz.models.Message
import me.skatz.utils.{Configuration, JsonHelper, KafkaUtils}
import org.apache.kafka.clients.consumer.KafkaConsumer

import scala.collection.mutable

object ElasticSearchProc {
  def apply(): Behavior[String] =
    Behaviors.setup(context => new ElasticSearchProc(context))
}

class ElasticSearchProc(context: ActorContext[String]) extends AbstractBehavior[String](context) {
  val batchSize: Int = 10
  val messageQueue: mutable.Queue[Message] = mutable.Queue[Message]()

  override def onMessage(msg: String): Behavior[String] =
    msg match {
      case "process" =>
        context.log.info("Elastic Search Processor started")
        val props = KafkaUtils.configureConsumer()
        consumeFromKafka(props, Configuration.topicName)
        context.log.info("Elastic Search Processor completed")
        this
      case "stop" => Behaviors.stopped
    }

  override def onSignal: PartialFunction[Signal, Behavior[String]] = {
    case PostStop =>
      context.log.info("Elastic Search Processor stopped")
      this
  }

  def consumeFromKafka(props: Properties, topic: String): Unit = {
    val consumer: KafkaConsumer[String, String] = new KafkaConsumer[String, String](props)
    consumer.subscribe(util.Arrays.asList(topic))

    while (true) {
      val iterator = consumer.poll(1000).iterator
      while (iterator.hasNext) {
        val next = iterator.next.value()

        val message = new Message(next)
        this.messageQueue.enqueue(message)
        context.log.info(s"Elastic Search Processor: consumed message ${message.getData}")

        if (this.messageQueue.size == this.batchSize) {
          val postData = getPostData
          postBatch(postData)
        }
      }
    }

    consumer.close()
  }

  def postBatch(postData: String): Unit = {
    context.log.info(postData)
    val url: String = "http://" + Configuration.esUrl + Configuration.esBulkEndpoint
    val result = HttpClient.post(url, postData)
    context.log.info(result)
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