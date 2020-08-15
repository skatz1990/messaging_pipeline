package me.skatz.kafka

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.kafka.Subscriptions
import akka.kafka.javadsl.Consumer
import akka.stream.alpakka.cassandra.scaladsl.CassandraSink
import akka.stream.scaladsl.Flow
import com.datastax.driver.core.{BoundStatement, Cluster, PreparedStatement, Session}
import me.skatz.database.TweeterMessage
import me.skatz.utils.{AvroMessageSerializer, Configuration, KafkaUtils}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import spray.json.DefaultJsonProtocol

object CassandraProc extends App with DefaultJsonProtocol {
  implicit val system: ActorSystem = ActorSystem("CassandraProc")
  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val log: LoggingAdapter = Logging.getLogger(ActorSystem.create,this)

  val consumerSettings = KafkaUtils.configureConsumerSettings(new ByteArrayDeserializer, new ByteArrayDeserializer)

  implicit val session: Session = Cluster.builder
    .addContactPoint(Configuration.cassandraUrl)
    .withPort(Configuration.cassandraPort.toInt)
    .build
    .connect()
  log.info("CassandraProc started")

  Consumer
    .plainSource(consumerSettings, Subscriptions.topics(Configuration.enrichCassTopic))
    .via(Flow[ConsumerRecord[Array[Byte], Array[Byte]]].map { kafkaMessage =>
      AvroMessageSerializer.genericByteArrayToMessage(kafkaMessage.value).get
    })
    .runWith({
      val statement = session.prepare(s"INSERT INTO ${Configuration.keyspace}.tweets(firstName, lastName, tweet, date) VALUES (?, ?, ?, ?)")

      // we need statement binder to convert scala case class object types into java types
      val statementBinder: (TweeterMessage, PreparedStatement) => BoundStatement = (tweet, ps) =>
        ps.bind(tweet.firstName: String, tweet.lastName: String, tweet.tweet: String, tweet.date: String)

      // parallelism defines number of concurrent queries that can execute to cassandra
      CassandraSink[TweeterMessage](parallelism = 2, statement = statement, statementBinder = statementBinder)
    }, system)
}
