package me.skatz.kafka

import java.io.ByteArrayOutputStream

import akka.actor.ActorSystem
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{CommitterSettings, ProducerMessage, ProducerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import com.google.gson.Gson
import com.sksamuel.avro4s.AvroSchema
import com.typesafe.config.ConfigFactory
import me.skatz.database.TweeterMessage
import me.skatz.utils.{Configuration, KafkaUtils}
import org.apache.avro.generic.{GenericData, GenericDatumWriter, GenericRecord}
import org.apache.avro.io.{BinaryEncoder, EncoderFactory}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}
import spray.json.DefaultJsonProtocol

object EnrichmentProc extends App with DefaultJsonProtocol {
  implicit val system: ActorSystem = ActorSystem("EnrichmentProc")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val consumerSettings = KafkaUtils.configureConsumerSettings()

  val producerConfig = ConfigFactory.load.getConfig("akka.kafka.producer")
  val producerSettings = ProducerSettings(producerConfig, new StringSerializer, new ByteArraySerializer)
    .withBootstrapServers(Configuration.bootstrapServer)

  val committerSettings = CommitterSettings(system).withMaxBatch(1L).withParallelism(1)

  Consumer
    .committableSource(consumerSettings, Subscriptions.topics(Configuration.ingestEnrichTopic))
    .map { msg =>
      val byteArray = serialize(msg.record.value())
      ProducerMessage.multi(
        List[ProducerRecord[String, Array[Byte]]](
          new ProducerRecord[String, Array[Byte]](Configuration.enrichEsprocTopic, byteArray),
          new ProducerRecord[String, Array[Byte]](Configuration.enrichCassTopic, byteArray)
        ),
        msg.committableOffset
      )
    }
    .toMat(Producer.committableSink(producerSettings, committerSettings))(Keep.both)
    .run()

  def serialize(msg: String): Array[Byte] = {
    val gson = new Gson

    // Deserialize JSON
    val message = gson.fromJson(msg, classOf[TweeterMessage])

    val schema = AvroSchema[TweeterMessage]
    val genericRecord: GenericRecord = new GenericData.Record(schema)

    genericRecord.put("firstName", message.firstName)
    genericRecord.put("lastName", message.lastName)
    genericRecord.put("tweet", message.tweet)
    genericRecord.put("date", message.date)

    // Serialize generic record into byte array
    val writer = new GenericDatumWriter[GenericRecord](schema)
    val out = new ByteArrayOutputStream()
    val encoder: BinaryEncoder = EncoderFactory.get().binaryEncoder(out, null)
    writer.write(genericRecord, encoder)
    encoder.flush()
    out.close()
    out.close()

    out.toByteArray
  }
}
