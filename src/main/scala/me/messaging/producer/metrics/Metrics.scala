package me.messaging.producer.metrics

import me.messaging.shared.metrics.Metric

object Metrics {
  val MessagesSent: Metric = Metric("producer.messages.sent", Metric.SUM)
}
