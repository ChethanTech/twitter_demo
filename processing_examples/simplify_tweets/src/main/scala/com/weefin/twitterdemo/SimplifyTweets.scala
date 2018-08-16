package com.weefin.twitterdemo

import com.danielasfregola.twitter4s.entities.Tweet
import com.typesafe.scalalogging.LazyLogging
import com.weefin.twitterdemo.utils.twitter.entities.SimpleStatus
import com.weefin.twitterdemo.utils.twitter.sink.KafkaJsonProducer
import com.weefin.twitterdemo.utils.twitter.source.KafkaJsonConsumer
import org.apache.flink.streaming.api.scala._

object SimplifyTweets extends App with LazyLogging {
  private val jobName = this.getClass.getSimpleName.split("\\$").last
  private val params = Parameters(args)
  private val env = StreamExecutionEnvironment.getExecutionEnvironment
  logger.info(s"$jobName job started")

  private val tweets: DataStream[Tweet] =
    env.addSource(consumer).flatMap(identity(_))

  private val simple = tweets.map(SimpleStatus(_))

  simple.addSink(producer)
  env.execute(jobName)

  private def consumer =
    KafkaJsonConsumer[Tweet](
      params.consumerBootstrapServers,
      params.consumerTopicId,
      params.consumerGroupId
    )

  private def producer =
    KafkaJsonProducer[SimpleStatus](
      params.producerBootstrapServers,
      params.producerTopicId
    )
}
