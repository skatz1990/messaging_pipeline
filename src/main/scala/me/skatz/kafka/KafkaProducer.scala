package me.skatz.kafka

import java.io.File
import java.util.Calendar

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory
import me.skatz.database.TweeterMessage
import me.skatz.utils.{Configuration, FileManager, JsonHelper}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.Future

object KafkaProducer extends App {
  val numOfTweets: Integer = 5
  val tweetLength: Integer = 100

  implicit val system: ActorSystem = ActorSystem("Producer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val config = ConfigFactory.load.getConfig("akka.kafka.producer")
  val producerSettings =
    ProducerSettings(config, new StringSerializer, new StringSerializer)
      .withBootstrapServers(Configuration.bootstrapServer)

  val fullDirPath: String = s"${System.getProperty("user.dir")}${Configuration.filesDir}"
  val fnameFm = new FileManager(new File(s"${fullDirPath}/${Configuration.firstNamesFile}"))
  val surnameFm = new FileManager(new File(s"${fullDirPath}/${Configuration.lastNamesFile}"))
  val wordsFm = new FileManager(new File(s"${fullDirPath}/${Configuration.wordsFile}"))

  val producerSink: Future[Done] =
    Source(1 to numOfTweets)
      .map(_ => new ProducerRecord[String, String](
        Configuration.topicName,
        JsonHelper.parseObject(generateTweetMsg(wordsFm, tweetLength, fnameFm.getRandomElement, surnameFm.getRandomElement))))
      .runWith(Producer.plainSink(producerSettings))

  println("************ Message produced ************")

  private def generateTweetMsg(fileManager: FileManager, tweetLength: Integer, fname: String, lname: String): TweeterMessage = {
    val tweet = new StringBuilder
    val append = (str1: StringBuilder, str2: String) => str1.append(str2 + ' ')

    for (_ <- 0 to tweetLength) {
      append(tweet, fileManager.getRandomElement)
    }
    TweeterMessage(fname, lname, tweet.toString.trim, Calendar.getInstance().getTime.toString)
  }
}
