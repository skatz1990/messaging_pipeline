package me.skatz.utils

import java.io.File
import java.util.Calendar

import me.skatz.database.TweeterMessage

object MessageGenerator {
  val numOfTweets: Integer = 5
  val tweetLength: Integer = 100
  val fullDirPath: String  = s"${System.getProperty("user.dir")}${Configuration.filesDir}"
  val fnameFm : FileManager = new FileManager(new File(s"${fullDirPath}/${Configuration.firstNamesFile}"))
  val surnameFm : FileManager = new FileManager(new File(s"${fullDirPath}/${Configuration.lastNamesFile}"))
  val wordsFm : FileManager = new FileManager(new File(s"${fullDirPath}/${Configuration.wordsFile}"))

  def generateTweetMsg(): TweeterMessage = {
    val tweet = new StringBuilder
    val append = (str1: StringBuilder, str2: String) => str1.append(str2 + ' ')

    for (_ <- 0 to tweetLength) {
      append(tweet, wordsFm.getRandomElement)
    }
    TweeterMessage(fnameFm.getRandomElement, surnameFm.getRandomElement, tweet.toString.trim, Calendar.getInstance().getTime.toString)
  }
}
