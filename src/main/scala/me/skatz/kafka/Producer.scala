package me.skatz.kafka

import java.time.LocalDateTime
import java.util.Properties

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{Behavior, PostStop, Signal}
import me.skatz.models.Message
import me.skatz.utils.{Configuration, KafkaUtils}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

object Producer {
  def apply(): Behavior[String] =
    Behaviors.setup(context => new Producer(context))
}

class Producer(context: ActorContext[String]) extends AbstractBehavior[String](context) {

  override def onMessage(msg: String): Behavior[String] =
    msg match {
      case "produce" =>
        context.log.info("Kafka Producer started")
        val props = KafkaUtils.configureProducer()
        produceToKafka(props, Configuration.topicName)
        context.log.info("Kafka Producer completed")
        this
      case "stop" => Behaviors.stopped
    }

  override def onSignal: PartialFunction[Signal, Behavior[String]] = {
    case PostStop =>
      context.log.info("Kafka Producer stopped")
      this
  }

  def produceToKafka(props: Properties, topic: String): Unit = {
    val producer = new KafkaProducer[Nothing, String](props)
    var i = 0

    while (true) {
      val message = new Message(s"Message #$i : ${LocalDateTime.now().toString}")
      val record = new ProducerRecord(topic, message.getData)
      producer.send(record)
      context.log.info(s"Message sent from Kafka Producer: ${message.getData}")
      i += 1
      Thread.sleep(1000)
    }

    producer.close()
  }

}
