package me.messaging.shared.metrics

case class Metric(key: String, aggregator: Metric.Value)

object Metric extends Enumeration {
  type MetricValue = Value
  val MIN = Value("MIN")
  val MAX = Value("MAX")
  val SUM = Value("SUM")
}

