package com.weefin.twitterdemo.utils.twitter.source

import java.nio.charset.StandardCharsets
import java.util.Properties

import com.danielasfregola.twitter4s.http.serializers.JsonSupport
import org.apache.flink.api.common.serialization.AbstractDeserializationSchema
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011
import org.json4s.native.Serialization

import scala.reflect.Manifest
import scala.util.Try

object KafkaJsonConsumer extends JsonSupport {
  def apply[T](bootstrapServers: String, topicId: String, groupId: String)(
    implicit mf: Manifest[T]
  ) =
    new FlinkKafkaConsumer011[Option[T]](
      topicId,
      JsonDeserializationSchema[T],
      new Properties {
        setProperty("bootstrap.servers", bootstrapServers)
        setProperty("group.id", groupId)
      }
    )

  private def JsonDeserializationSchema[T](implicit mf: Manifest[T]) =
    new AbstractDeserializationSchema[Option[T]] {
      override def deserialize(message: Array[Byte]) =
        Try(Serialization.read[T](new String(message, StandardCharsets.UTF_8))).toOption
    }
}
