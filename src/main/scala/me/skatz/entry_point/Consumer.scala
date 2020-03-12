package me.skatz.entry_point

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorSystem, Behavior}
import me.skatz.kafka.{CassandraProc, ElasticSearchProc}

object ConsumerMain {
  def apply(): Behavior[String] =
    Behaviors.setup(context => new ConsumerMain(context))
}

class ConsumerMain(context: ActorContext[String]) extends AbstractBehavior[String](context) {
  override def onMessage(msg: String): Behavior[String] =
    msg match {
      case "startEsProc" =>
        val esProcessor = context.spawn(ElasticSearchProc(), "consumer-actor")
        context.log.info(s"Consumer: $esProcessor")
        esProcessor ! "process"
        this
      case "startCassandraProc" =>
        val cassandraProcessor = context.spawn(CassandraProc(), "consumer-actor")
        context.log.info(s"Consumer: $cassandraProcessor")
        cassandraProcessor ! "process"
        this
    }
}

object ActorHierarchyConsumer extends App {
  val mainSystem = ActorSystem(ConsumerMain(), "mainSystem")
  mainSystem ! "startEsProc"
  mainSystem ! "startCassandraProc"
}