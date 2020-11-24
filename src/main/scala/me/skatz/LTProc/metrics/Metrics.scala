package me.skatz.LTProc.metrics

import me.skatz.shared.metrics.Metric

object Metrics {
  object Metrics {
    val MessagesSent = new Metric("LTProc.messages.processed", Metric.SUM)
  }
}
