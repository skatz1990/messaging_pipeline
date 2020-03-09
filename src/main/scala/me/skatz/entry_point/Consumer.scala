package me.skatz.entry_point

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorSystem, Behavior}
import me.skatz.kafka.Consumer

object ConsumerMain {
  def apply(): Behavior[String] =
    Behaviors.setup(context => new ConsumerMain(context))
}

class ConsumerMain(context: ActorContext[String]) extends AbstractBehavior[String](context) {
  override def onMessage(msg: String): Behavior[String] =
    msg match {
      case "startConsumer" =>
        val consumerRef = context.spawn(Consumer(), "consumer-actor")
        context.log.info(s"Consumer: $consumerRef")
        consumerRef ! "consume"
        this
    }
}

object ActorHierarchyConsumer extends App {
  val mainSystem = ActorSystem(ConsumerMain(), "mainSystem")
  mainSystem ! "startConsumer"
}