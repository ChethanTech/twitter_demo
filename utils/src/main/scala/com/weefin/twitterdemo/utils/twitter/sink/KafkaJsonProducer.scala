package com.weefin.twitterdemo.utils.twitter.sink

import java.nio.charset.StandardCharsets

import com.danielasfregola.twitter4s.http.serializers.JsonSupport
import org.apache.flink.api.common.serialization.SerializationSchema
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011
import org.json4s.native.Serialization

object KafkaJsonProducer extends JsonSupport {
  def apply[T <: AnyRef](bootstrapServers: String, topicId: String) =
    new FlinkKafkaProducer011[T](
      bootstrapServers,
      topicId,
      JsonDeserializationSchema[T]
    ) {
      setWriteTimestampToKafka(true)
    }

  private def JsonDeserializationSchema[T <: AnyRef] =
    new SerializationSchema[T] {
      override def serialize(element: T) =
        Serialization.write(element).getBytes(StandardCharsets.UTF_8)
    }
}
