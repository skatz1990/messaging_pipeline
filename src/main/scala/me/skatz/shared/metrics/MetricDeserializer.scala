package me.skatz.shared.metrics

import java.lang.reflect.Type

import com.google.gson._

class MetricDeserializer extends JsonDeserializer[Metric] {
  override def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Metric = {
    val res = json match {
      case o: JsonPrimitive =>
        val parsed = new JsonParser().parse(o.getAsString).getAsJsonObject
        val key = parsed.get("key").getAsString
        val value = parsed.get("value").getAsString
        new Metric(key, Metric.withName(value))

      case _ => null
    }
    Option(res).getOrElse(throw new JsonParseException(s"$json can't be parsed to State"))
  }
}
