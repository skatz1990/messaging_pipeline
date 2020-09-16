package me.skatz.shared.metrics

import java.lang.reflect.Type

import com.google.gson._

class MetricSerializer extends JsonSerializer[Metric] {
  override def serialize(metric: Metric, typeOfSrc: Type, context: JsonSerializationContext): JsonElement = {
    val result = new JsonObject
    result.addProperty("key", metric.key)
    result.addProperty("value", metric.value.toString)
    result
  }
}
