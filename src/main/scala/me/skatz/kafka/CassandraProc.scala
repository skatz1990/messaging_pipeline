package me.skatz.kafka

import java.util
import java.util.Properties

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{Behavior, PostStop, Signal}
import me.skatz.models.Message
import me.skatz.utils.{Configuration, KafkaUtils}
import org.apache.kafka.clients.consumer.KafkaConsumer

object CassandraProc {
  def apply(): Behavior[String] =
    Behaviors.setup(context => new ElasticSearchProc(context))
}

class CassandraProc(context: ActorContext[String]) extends AbstractBehavior[String](context) {

  override def onMessage(msg: String): Behavior[String] =
    msg match {
      case "process" =>
        context.log.info("Cassandra Processor started")
        val props = KafkaUtils.configureConsumer()
        consumeFromKafka(props, Configuration.topicName)
        context.log.info("Cassandra Search Processor completed")
        this
      case "stop" => Behaviors.stopped
    }

  override def onSignal: PartialFunction[Signal, Behavior[String]] = {
    case PostStop =>
      context.log.info("Cassandra Processor stopped")
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
        context.log.info(s"Cassandra Processor: consumed message ${message.getData}")

        // Post to Cassandra
      }
    }

    consumer.close()
  }
}