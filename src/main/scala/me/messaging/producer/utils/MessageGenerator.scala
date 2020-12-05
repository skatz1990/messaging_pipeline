package me.messaging.producer.utils

import java.io.File

import me.messaging.cassandraProc.database.TweeterMessage
import me.messaging.shared.{Configuration, Utilities}

object MessageGenerator {
  val numOfTweets: Integer = 10
  val tweetLength: Integer = 70
  val fullDirPath: String = s"${System.getProperty("user.dir")}${Configuration.filesDir}"
  val fnameFm: FileManager = new FileManager(new File(s"${fullDirPath}/${Configuration.firstNamesFile}"))
  val surnameFm: FileManager = new FileManager(new File(s"${fullDirPath}/${Configuration.lastNamesFile}"))
  val wordsFm: FileManager = new FileManager(new File(s"${fullDirPath}/${Configuration.wordsFile}"))

  def generateTweetMsg(): TweeterMessage = {
    val tweet = new StringBuilder
    val append = (str1: StringBuilder, str2: String) => str1.append(str2 + ' ')

    for (_ <- 0 to tweetLength) {
      append(tweet, wordsFm.getRandomElement)
    }
    TweeterMessage(fnameFm.getRandomElement, surnameFm.getRandomElement, tweet.toString.trim, Utilities.currentDate)
  }
}
