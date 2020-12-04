package me.messaging.producer.utils

import java.io.File

import scala.util.Random

class FileManager() {

  var filesLines: Seq[String] = _

  def this(filename: File) {
    this()
    this.filesLines = readFile(filename)
  }

  def getRandomElement: String = {
    val randomIndex = new Random().nextInt(this.filesLines.length)
    this.filesLines(randomIndex)
  }

  private def readFile(filename: File): Seq[String] = {
    val bufferedSource = scala.io.Source.fromFile(filename)
    val lines = (for (line <- bufferedSource.getLines()) yield line).toList
    bufferedSource.close
    lines
  }
}
