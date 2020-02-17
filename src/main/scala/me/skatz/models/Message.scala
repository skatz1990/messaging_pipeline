package me.skatz.models

class Message {
  var data: String = ""

  def this(dataVal: String) = {
    this()
    this.data = dataVal
  }

  def getData: String = {
    return this.data
  }
}
