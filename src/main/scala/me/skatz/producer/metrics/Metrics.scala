package me.skatz.producer.metrics

import me.skatz.shared.Metric

object Metrics {
  val MessagesSent = new Metric("producer.messages.sent", Metric.SUM)
}
