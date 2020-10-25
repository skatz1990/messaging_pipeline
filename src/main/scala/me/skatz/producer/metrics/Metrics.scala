package me.skatz.producer.metrics

import me.skatz.shared.metrics.Metric

object Metrics {
  val MessagesSent: Metric = Metric("producer.messages.sent", Metric.SUM)
}
