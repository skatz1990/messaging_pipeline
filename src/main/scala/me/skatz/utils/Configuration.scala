package me.skatz.utils

object Configuration {

  // Kafka
  val bootstrapServer: String = sys.env.getOrElse("bootstrap_servers", "127.0.0.1:9092")
  val autoOffsetReset: String = sys.env.getOrElse("auto_offset_reset", "latest")
  val groupId: String = sys.env.getOrElse("group_id", "consumer-group")
  val ingestEnrichTopic: String = sys.env.getOrElse("ingest_enrich_topic", "ingest_enrich")

  // Elastic Search
  val esUrl: String = sys.env.getOrElse("elasticsearch_url", "127.0.0.1")
  val esPort: String = sys.env.getOrElse("elasticsearch_port", "9200")
  val enrichEsprocTopic: String = sys.env.getOrElse("enrich_esproc_topic", "enrich_esproc")

  // Cassandra
  val cassandraUrl: String = sys.env.getOrElse("cassandra_url", "127.0.0.1")
  val cassandraPort: String = sys.env.getOrElse("cassandra_port", "9042")
  val keyspace: String = sys.env.getOrElse("cassandra_keyspace", "kafka")
  val enrichCassTopic: String = sys.env.getOrElse("enrich_cassproc_topic", "enrich_cassproc")

  // Files Directory
  val filesDir: String = sys.env.getOrElse("files_dir", "/src/main/resources/twitter")
  val firstNamesFile: String = sys.env.getOrElse("fname_file", "firstname.txt")
  val lastNamesFile: String = sys.env.getOrElse("lname_file", "surname.txt")
  val wordsFile: String = sys.env.getOrElse("words_file", "words.txt")
}