package me.messaging.cassandraProc.metrics

import me.messaging.shared.metrics.Metric

object Metrics {
  val MessagesSent = new Metric("cassandraProc.messages.processed", Metric.SUM)
}

