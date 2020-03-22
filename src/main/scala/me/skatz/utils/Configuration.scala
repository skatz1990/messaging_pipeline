package me.skatz.utils

object Configuration {

  // Kafka
  val bootstrapServer: String = sys.env.getOrElse("bootstrap_servers", "127.0.0.1:9092")
  val autoOffsetReset: String = sys.env.getOrElse("auto_offset_reset", "latest")
  val groupId: String = sys.env.getOrElse("group_id", "consumer-group")
  val topicName: String = sys.env.getOrElse("TOPIC_NAME", "our_kafka_topic")

  // Elastic Search
  val esUrl: String = sys.env.getOrElse("elasticsearch_url", "127.0.0.1")
  val esPort: String = sys.env.getOrElse("elasticsearch_port", "9200")

  // Cassandra
  val cassandraUrl: String = sys.env.getOrElse("cassandra_url", "127.0.0.1")
  val cassandraPort: String = sys.env.getOrElse("cassandra_port", "9042")
  val keyspace: String = sys.env.getOrElse("cassandra_keyspace", "kafka")
}