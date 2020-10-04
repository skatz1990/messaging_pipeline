package me.skatz.metricProc

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerMessage, Subscriptions}
import akka.stream.alpakka.cassandra.scaladsl.CassandraSink
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph}
import akka.stream.{ActorMaterializer, ClosedShape}
import com.datastax.driver.core.{BoundStatement, Cluster, PreparedStatement, Session}
import com.google.gson.GsonBuilder
import me.skatz.shared.metrics.{Metric, MetricDeserializer}
import me.skatz.shared.{Configuration, KafkaUtils, Utilities}
import org.apache.kafka.common.serialization.StringDeserializer

object MetricProc extends App {
  implicit val system: ActorSystem = ActorSystem("MetricProc")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val log: LoggingAdapter = Logging.getLogger(ActorSystem.create, this)
  implicit val session: Session = Cluster.builder
    .addContactPoint(Configuration.cassandraUrl)
    .withPort(Configuration.cassandraPort.toInt)
    .build
    .connect()

  val consumerSettings = KafkaUtils.configureConsumerSettings(new StringDeserializer, new StringDeserializer)
  log.info("MetricProc started")

  RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
    val source = Consumer.committableSource(consumerSettings, Subscriptions.topics(Configuration.metricProcSourceTopic))
    val map = Flow[ConsumerMessage.CommittableMessage[String, String]].map { msg =>
      val gson = new GsonBuilder()
        .registerTypeHierarchyAdapter(classOf[Metric], new MetricDeserializer)
        .setPrettyPrinting()
        .create()

      gson.fromJson(msg.record.value, classOf[Metric])
    }

    val sink = {
      val statement = session.prepare(s"INSERT INTO ${Configuration.keyspace}.metrics(key, aggregator, date, value) VALUES (?, ?, ?, ?)")

      val statementBinder: (Metric, PreparedStatement) => BoundStatement = (metric, ps) =>
        ps.bind(metric.key: String, metric.aggregator.toString: String, Utilities.currentDateMs: String, 1: Double)

      // parallelism defines number of concurrent queries that can execute to cassandra
      CassandraSink[Metric](parallelism = 2, statement = statement, statementBinder = statementBinder)
    }

    source ~> map ~> sink
    ClosedShape
  }).run()
}
