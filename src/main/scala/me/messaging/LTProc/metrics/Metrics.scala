package me.messaging.LTProc.metrics

import me.messaging.shared.metrics.Metric

object Metrics {
  object Metrics {
    val MessagesSent = new Metric("LTProc.messages.processed", Metric.SUM)
  }
}
