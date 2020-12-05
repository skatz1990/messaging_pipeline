package me.messaging.cassandraProc.database

import java.text.SimpleDateFormat
import java.util.Date

case class TweeterMessage(firstName: String, lastName: String, tweet: String, date: String) {
  // Requires for serialization
  def this() = this("default", "default", "default", "default")

  def dTestDate: Date = {
    val sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
    sdf.parse(date)
  }
}
