package me.messaging.shared

import java.io.ByteArrayOutputStream

import com.google.gson.Gson
import com.sksamuel.avro4s.AvroSchema
import me.messaging.cassandraProc.database.TweeterMessage
import org.apache.avro.generic.{GenericData, GenericDatumReader, GenericDatumWriter, GenericRecord}
import org.apache.avro.io.{BinaryEncoder, DecoderFactory, EncoderFactory}

object AvroMessageSerializer {
  def jsonToTweeterByteArray(jsonMsg: String): Array[Byte] = {
    val gson = new Gson

    // Deserialize JSON
    val message = gson.fromJson(jsonMsg, classOf[TweeterMessage])

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

  def tweeterByteArrayToMessage(byteArray: Array[Byte]): Option[TweeterMessage] = {
    val schema = AvroSchema[TweeterMessage]

    val reader: GenericDatumReader[GenericRecord] = new GenericDatumReader[GenericRecord](schema)
    val decoder = DecoderFactory.get().binaryDecoder(byteArray, null)
    val generic: GenericRecord = reader.read(null, decoder)

    val tweeterMessage = TweeterMessage(
      generic.get("firstName").toString,
      generic.get("lastName").toString,
      generic.get("tweet").toString,
      generic.get("date").toString)

    Some(tweeterMessage)
  }
}
