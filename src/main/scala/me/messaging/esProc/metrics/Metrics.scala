package me.messaging.esProc.metrics

import me.messaging.shared.metrics.Metric

object Metrics {
  val MessagesSent = new Metric("esProc.messages.processed", Metric.SUM)
}
