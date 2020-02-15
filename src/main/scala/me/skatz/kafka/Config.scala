package me.skatz.kafka

import com.typesafe.config._

object Config {
  val env: String = if (System.getenv("SCALA_ENV") == null) "development" else System.getenv("SCALA_ENV")
  val conf: Config = ConfigFactory.load()
  def apply(): Config = conf.getConfig(env)
}
