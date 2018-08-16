package com.weefin.twitterdemo

import com.typesafe.scalalogging.LazyLogging
import com.weefin.twitterdemo.utils.twitter.source.RawTwitterSource
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011

object TwitterStreamToKafka extends App with LazyLogging {
  private val jobName = this.getClass.getSimpleName.split("\\$").last
  private val params = Parameters(args)
  private val env = StreamExecutionEnvironment.getExecutionEnvironment
  logger.info(s"$jobName job started")

  env
    .addSource(twitterSource)
    .filter { t =>
      logger.trace(s"Received status: ${t.substring(0, 75)}…").->(true)._2
    }
    .addSink(producer)

  env.execute(jobName)

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
}
