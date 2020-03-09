package me.skatz.entry_point

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorSystem, Behavior}
import me.skatz.kafka.Producer

object ProducerMain {
  def apply(): Behavior[String] =
    Behaviors.setup(context => new ProducerMain(context))
}

class ProducerMain(context: ActorContext[String]) extends AbstractBehavior[String](context) {
  override def onMessage(msg: String): Behavior[String] =
    msg match {
      case "startProducer" =>
        val producerRef = context.spawn(Producer(), "producer-actor")
        context.log.info(s"Producer: $producerRef")
        producerRef ! "produce"
        this
    }
}

object ActorHierarchyProducer extends App {
  val mainSystem = ActorSystem(ProducerMain(), "mainSystem")
  mainSystem ! "startProducer"
}