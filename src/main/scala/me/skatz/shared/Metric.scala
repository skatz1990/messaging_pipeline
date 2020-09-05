package me.skatz.shared

case class Metric(key: String, value: Metric.Value)

object Metric extends Enumeration {
  type MetricValue = Value
  val MIN = Value("MIN")
  val MAX = Value("MAX")
  val SUM = Value("SUM")
}
