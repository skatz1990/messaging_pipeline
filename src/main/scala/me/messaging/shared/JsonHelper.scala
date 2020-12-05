package me.messaging.shared

import com.google.gson.Gson

object JsonHelper {

  def parseObject(obj: Any): String = {
    val gson = new Gson
    val jsonString = gson.toJson(obj)
    jsonString
  }
}
