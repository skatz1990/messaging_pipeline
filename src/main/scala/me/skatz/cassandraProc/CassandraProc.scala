package me.skatz.cassandraProc

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.kafka.Subscriptions
import akka.kafka.javadsl.Consumer
import akka.stream.alpakka.cassandra.scaladsl.CassandraSink
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph}
import com.datastax.driver.core.{BoundStatement, Cluster, PreparedStatement, Session}
import me.skatz.cassandraProc.database.TweeterMessage
import me.skatz.shared.{AvroMessageSerializer, Configuration, KafkaUtils}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import spray.json.DefaultJsonProtocol
import GraphDSL.Implicits._
import akka.stream.{ActorMaterializer, ClosedShape}

object CassandraProc extends App with DefaultJsonProtocol {
  implicit val system: ActorSystem = ActorSystem("CassandraProc")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val log: LoggingAdapter = Logging.getLogger(ActorSystem.create, this)

  val consumerSettings = KafkaUtils.configureConsumerSettings(new ByteArrayDeserializer, new ByteArrayDeserializer)

  implicit val session: Session = Cluster.builder
    .addContactPoint(Configuration.cassandraUrl)
    .withPort(Configuration.cassandraPort.toInt)
    .build
    .connect()
  log.info("CassandraProc started")

  val graph = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
    val source = Consumer.plainSource(consumerSettings, Subscriptions.topics(Configuration.enrichCassTopic))
    val map = Flow[ConsumerRecord[Array[Byte], Array[Byte]]].map { kafkaMessage =>
      AvroMessageSerializer.genericByteArrayToMessage(kafkaMessage.value).get
    }
    val sink = {
      val statement = session.prepare(s"INSERT INTO ${Configuration.keyspace}.tweets(firstName, lastName, tweet, date) VALUES (?, ?, ?, ?)")

      // we need statement binder to convert scala case class object types into java types
      val statementBinder: (TweeterMessage, PreparedStatement) => BoundStatement = (tweet, ps) =>
        ps.bind(tweet.firstName: String, tweet.lastName: String, tweet.tweet: String, tweet.date: String)

      // parallelism defines number of concurrent queries that can execute to cassandra
      CassandraSink[TweeterMessage](parallelism = 2, statement = statement, statementBinder = statementBinder)
    }

    source ~> map ~> sink
    ClosedShape
  }).run()
}
