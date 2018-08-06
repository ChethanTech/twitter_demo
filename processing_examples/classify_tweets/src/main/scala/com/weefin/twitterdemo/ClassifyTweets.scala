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
  env
    .addSource(consumer)
    .flatMap(identity(_))
    .map(SimpleStatus(_))
    .map { s =>
      ClassifiedSimpleStatus(
        s,
        Classification
          .getMainDefinedClassification(Classification.fromWords(s.hashtags))
          .map(_.label.toString)
          .getOrElse("None")
      )
    }
    .addSink(producer)
  env.execute(jobName)

  private def consumer =
    KafkaJsonConsumer[Tweet](
      params.consumerBootstrapServers,
      params.consumerTopicId,
      params.consumerGroupId
    )

  private def producer =
    KafkaJsonProducer[ClassifiedSimpleStatus](
      params.producerBootstrapServers,
      params.producerTopicId
    )

  private case class ClassifiedSimpleStatus(status: SimpleStatus,
                                            mainClass: String)

}
