package me.skatz.esProc.metrics

import me.skatz.shared.metrics.Metric

object Metrics {
  val MessagesSent = new Metric("esProc.messages.processed", Metric.SUM)
}
