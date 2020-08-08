package me.skatz.kafka

import akka.actor.ActorSystem
import akka.kafka.Subscriptions
import akka.kafka.javadsl.Consumer
import akka.stream.alpakka.cassandra.scaladsl.CassandraSink
import akka.stream.scaladsl.Flow
import com.datastax.driver.core.{BoundStatement, Cluster, PreparedStatement, Session}
import com.google.gson.Gson
import me.skatz.database.TweeterMessage
import me.skatz.utils.{Configuration, KafkaUtils}
import org.apache.kafka.clients.consumer.ConsumerRecord
import spray.json.DefaultJsonProtocol

object CassandraProc extends App with DefaultJsonProtocol {
  implicit val system: ActorSystem = ActorSystem("CassandraProc")
  implicit val actorSystem: ActorSystem = ActorSystem()

  val consumerSettings = KafkaUtils.configureConsumerSettings()

  implicit val session: Session = Cluster.builder
    .addContactPoint(Configuration.cassandraUrl)
    .withPort(Configuration.cassandraPort.toInt)
    .build
    .connect()

  val kafkaSource = Consumer.plainSource(consumerSettings, Subscriptions.topics(Configuration.enrichCassTopic))

  // flow to map kafka message which comes as JSON string to Message
  val toMessageFlow = Flow[ConsumerRecord[Array[Byte], String]]
    .map(kafkaMessage =>
      new Gson().fromJson(kafkaMessage.value(), classOf[TweeterMessage])
    )

  val sink = {
    val statement = session.prepare(s"INSERT INTO ${Configuration.keyspace}.tweets(firstName, lastName, tweet, date) VALUES (?, ?, ?, ?)")

    // we need statement binder to convert scala case class object types into java types
    val statementBinder: (TweeterMessage, PreparedStatement) => BoundStatement = (tweet, ps) =>
      ps.bind(tweet.firstName: String, tweet.lastName: String, tweet.tweet: String, tweet.date: String)

    // parallelism defines no of concurrent queries that can execute to cassandra
    CassandraSink[TweeterMessage](parallelism = 2, statement = statement, statementBinder = statementBinder)
  }

  kafkaSource.via(toMessageFlow).runWith(sink, system)
}
