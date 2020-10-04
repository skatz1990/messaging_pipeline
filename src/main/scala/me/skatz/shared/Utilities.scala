package me.skatz.shared

import java.io.{ByteArrayOutputStream, ObjectOutputStream}
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Calendar

object Utilities {
  def objToByteArray(obj: Any): Array[Byte] = {
    val stream: ByteArrayOutputStream = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(stream)
    oos.writeObject(obj)
    oos.close()
    stream.toByteArray
  }

  def objToString(obj: Any): String = {
    val stream = objToByteArray(obj)
    new String(stream, StandardCharsets.UTF_8)
  }

  def currentDate: String = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ").format(Calendar.getInstance.getTime)

  def currentDateMs: String = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance.getTime)
}
