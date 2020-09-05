package me.skatz.enrichment.metrics

import me.skatz.shared.Metric

object Metrics {
  val MessagesSent = new Metric("enrichmentProc.messages.processed", Metric.SUM)
}
