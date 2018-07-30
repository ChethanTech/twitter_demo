package com.weefin.twitterdemo

import com.typesafe.scalalogging.LazyLogging
import com.weefin.twitterdemo.utils.twitter.filter.EmptyFilter
import com.weefin.twitterdemo.utils.twitter.source.RawTwitterSource
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011

object TwitterStreamToKafka extends App with LazyLogging {
  logger.info("Twitter stream to kafka job started")
  val params = Parameters(args)
  val env = StreamExecutionEnvironment.getExecutionEnvironment
  env.addSource(twitterSource).filter(LogFilter).addSink(producer)
  env.execute("Twitter stream to kafka")

  private def twitterSource =
    RawTwitterSource(
      params.uri,
      params.httpMethod,
      params.consumerKey,
      params.consumerSecret,
      params.token,
      params.tokenSecret
    )

  private def producer =
    new FlinkKafkaProducer011[String](
      params.bootstrapServers,
      params.topicId,
      new SimpleStringSchema
    ) {
      setWriteTimestampToKafka(true)
    }

  private object LogFilter extends EmptyFilter[String] with LazyLogging {
    override def action(value: String): Unit =
      logger.debug(s"Received status: ${value.substring(0, 100)}…")
  }
}
