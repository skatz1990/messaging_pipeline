package me.skatz.cassandraProc.metrics

import me.skatz.shared.metrics.Metric

object Metrics {
  val MessagesSent = new Metric("cassandraProc.messages.processed", Metric.SUM)
}

