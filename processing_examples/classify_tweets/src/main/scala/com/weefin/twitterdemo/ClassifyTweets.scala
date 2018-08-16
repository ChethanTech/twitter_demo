package com.weefin.twitterdemo

import com.danielasfregola.twitter4s.entities.Tweet
import com.typesafe.scalalogging.LazyLogging
import com.weefin.twitterdemo.utils.twitter.entities.{
  Classification,
  SimpleStatus
}
import com.weefin.twitterdemo.utils.twitter.sink.KafkaJsonProducer
import com.weefin.twitterdemo.utils.twitter.source.KafkaJsonConsumer
import org.apache.flink.streaming.api.scala._

object ClassifyTweets extends App with LazyLogging {
  private val jobName = this.getClass.getSimpleName.split("\\$").last
  private val params = Parameters(args)
  private val env = StreamExecutionEnvironment.getExecutionEnvironment
  logger.info(s"$jobName job started")

  private val tweets: DataStream[Tweet] =
    env.addSource(consumer).flatMap(identity(_))

  private val simpleStatuses: DataStream[SimpleStatus] =
    tweets.map(SimpleStatus(_))

  private val richStatuses: DataStream[RichStatus] = simpleStatuses
    .map { s =>
      RichStatus(
        s,
        Classification.stringify(Classification.classify(s.hashtags: _*))
      )
    }

  richStatuses.addSink(producer)
  env.execute(jobName)

  private def consumer =
    KafkaJsonConsumer[Tweet](
      params.consumerBootstrapServers,
      params.consumerTopicId,
      params.consumerGroupId
    )

  private def producer =
    KafkaJsonProducer[RichStatus](
      params.producerBootstrapServers,
      params.producerTopicId
    )

  private case class RichStatus(status: SimpleStatus,
                                classification: Map[String, Float] = Map.empty)

}
