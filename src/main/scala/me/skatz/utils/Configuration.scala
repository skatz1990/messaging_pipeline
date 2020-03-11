package me.skatz.utils

object Configuration {
  val bootstrapServer: String = sys.env.getOrElse("bootstrap_servers", "10.63.26.194:9092")
  val autoOffsetReset: String = sys.env.getOrElse("auto_offset_reset", "latest")
  val groupId: String = sys.env.getOrElse("group_id", "consumer-group")
  val esUrl: String = sys.env.getOrElse("elasticsearch_url", "10.63.24.170:9200")
  val esBulkEndpoint: String = sys.env.getOrElse("elasticsearch_bulk_endpoint", "/kafka_data/_doc/_bulk")
  val topicName: String = sys.env.getOrElse("TOPIC_NAME", "our_kafka_topic")
}