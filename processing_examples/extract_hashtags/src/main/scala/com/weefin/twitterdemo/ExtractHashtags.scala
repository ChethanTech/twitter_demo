package com.weefin.twitterdemo

import com.danielasfregola.twitter4s.entities.Tweet
import com.typesafe.scalalogging.LazyLogging
import com.weefin.twitterdemo.utils.twitter.filter.KeywordFilter
import com.weefin.twitterdemo.utils.twitter.flatmap.{
  DatedHashtag,
  FlatMapExtractDatedHashtags
}
import com.weefin.twitterdemo.utils.twitter.sink.KafkaJsonProducer
import com.weefin.twitterdemo.utils.twitter.source.KafkaJsonConsumer
import org.apache.flink.streaming.api.scala._

object ExtractHashtags extends App with LazyLogging {
  private val jobName = this.getClass.getSimpleName.split("\\$").last
  private val params = Parameters(args)
  private val env = StreamExecutionEnvironment.getExecutionEnvironment
  logger.info(s"$jobName job started")
  env
    .addSource(consumer)
    .flatMap(FlatMapExtractDatedHashtags)
    .flatMap(identity(_))
    .filter(hashtagFilter)
    .addSink(producer)
  env.execute(jobName)

  private def hashtagFilter =
    KeywordFilter[DatedHashtag](_.hashtag.text)(
      params.whiteList,
      params.blackList,
      ignoreCase = true
    )

  private def consumer =
    KafkaJsonConsumer[Tweet](
      params.consumerBootstrapServers,
      params.consumerTopicId,
      params.consumerGroupId
    )

  private def producer =
    KafkaJsonProducer[DatedHashtag](
      params.producerBootstrapServers,
      params.producerTopicId
    )
}
