package com.weefin.twitterdemo

import java.util.concurrent.TimeUnit

import com.danielasfregola.twitter4s.entities.Tweet
import com.typesafe.scalalogging.LazyLogging
import com.weefin.twitterdemo.utils.twitter.sink.KafkaJsonProducer
import com.weefin.twitterdemo.utils.twitter.source.AsyncTimelineRequest
import org.apache.flink.streaming.api.scala._

object TwitterUserTimelinesToKafka extends App with LazyLogging {
  private val jobName = this.getClass.getSimpleName.split("\\$").last
  private val params = Parameters(args)
  private val env = StreamExecutionEnvironment.getExecutionEnvironment
  logger.info(s"$jobName job started")

  val users: DataStream[Long] = env.fromCollection(params.userIds)

  val timelines: DataStream[Seq[Tweet]] = AsyncDataStream
    .unorderedWait(users, asyncTimelineRequest, 85, TimeUnit.SECONDS, 100)

  val tweets: DataStream[Tweet] = timelines.flatMap(identity(_))

  tweets.addSink(producer)
  env.execute(jobName)

  private def asyncTimelineRequest =
    new AsyncTimelineRequest(
      params.consumerKey,
      params.consumerSecret,
      params.token,
      params.tokenSecret,
      params.tweetCount
    )

  private def producer =
    KafkaJsonProducer[Tweet](params.bootstrapServers, params.topicId)
}
