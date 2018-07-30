package com.weefin.twitterdemo

import com.danielasfregola.twitter4s.entities.Tweet
import com.typesafe.scalalogging.LazyLogging
import com.weefin.twitterdemo.utils.twitter.entities.{SimpleStatus, SimpleUser}
import com.weefin.twitterdemo.utils.twitter.process.KeyedProcessThrottle
import com.weefin.twitterdemo.utils.twitter.sink.KafkaJsonProducer
import com.weefin.twitterdemo.utils.twitter.source.KafkaJsonConsumer
import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.util.Collector

object ExtractUsers extends App with LazyLogging {
  private val jobName = this.getClass.getSimpleName.split("\\$").last
  private val params = Parameters(args)
  private val env = StreamExecutionEnvironment.getExecutionEnvironment
  logger.info(s"$jobName job started")
  env
    .addSource(consumer)
    .flatMap(FlatMapSimpleUser)
    .keyBy(_.id)
    .process(FilterDuplicates)
    .addSink(producer)
  env.execute

  private def FilterDuplicates =
    KeyedProcessThrottle[Long, SimpleUser, SimpleUser](Time.minutes(10), x => x)

  private def consumer =
    KafkaJsonConsumer[Tweet](
      params.consumerBootstrapServers,
      params.consumerTopicId,
      params.consumerGroupId
    )

  private def producer =
    KafkaJsonProducer[SimpleUser](
      params.producerBootstrapServers,
      params.producerTopicId
    )

  private object FlatMapSimpleUser
      extends RichFlatMapFunction[Option[Tweet], SimpleUser] {
    override def flatMap(value: Option[Tweet],
                         out: Collector[SimpleUser]): Unit =
      value
        .foreach(SimpleStatus(_).user.foreach(out.collect(_)))
  }
}
