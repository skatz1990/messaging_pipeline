package me.skatz.utils

import java.util.Properties

object KafkaUtils {
  def configureConsumer(): Properties = {
    val props = new Properties()
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("bootstrap.servers", Configuration.bootstrapServer)
    props.put("auto.offset.reset", Configuration.autoOffsetReset)
    props.put("group.id", Configuration.groupId)
    props
  }

  def configureProducer(): Properties = {
    val props = new Properties()
    props.put("bootstrap.servers", Configuration.bootstrapServer)
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props
  }

}
