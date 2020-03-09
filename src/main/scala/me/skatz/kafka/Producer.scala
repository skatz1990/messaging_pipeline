package me.skatz.kafka

import java.time.LocalDateTime
import java.util.Properties

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{Behavior, PostStop, Signal}
import me.skatz.models.Message
import me.skatz.utils.Configuration
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

object Producer {
  def apply(): Behavior[String] =
    Behaviors.setup(context => new Producer(context))
}

class Producer(context: ActorContext[String]) extends AbstractBehavior[String](context) {

  override def onMessage(msg: String): Behavior[String] =
    msg match {
      case "produce" =>
        context.log.info("Producer started")
        val props = configure()
        produceToKafka(props, Configuration.topicName)
        context.log.info("Producer completed")
        this
      case "stop" => Behaviors.stopped
    }

  override def onSignal: PartialFunction[Signal, Behavior[String]] = {
    case PostStop =>
      context.log.info("Producer stopped")
      this
  }

  def configure(): Properties = {
    val props = new Properties()
    props.put("bootstrap.servers", Configuration.bootstrapServer)
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props
  }

  def produceToKafka(props: Properties, topic: String): Unit = {
    val producer = new KafkaProducer[Nothing, String](props)
    var i = 0

    while (true) {
      val message = new Message(s"Message #$i : ${LocalDateTime.now().toString}")
      val record = new ProducerRecord(topic, message.getData)
      producer.send(record)
      context.log.info(s"SENT: ${message.getData}")
      i += 1
      Thread.sleep(1000)
    }

    producer.close()
  }

}
