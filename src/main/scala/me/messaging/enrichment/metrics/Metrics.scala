package me.messaging.enrichment.metrics

import me.messaging.shared.metrics.Metric

object Metrics {
  val MessagesSent = new Metric("enrichmentProc.messages.processed", Metric.SUM)
}
