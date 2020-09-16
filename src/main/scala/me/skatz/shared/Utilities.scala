package me.skatz.shared

import java.io.{ByteArrayOutputStream, ObjectOutputStream}
import java.nio.charset.StandardCharsets

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
}
