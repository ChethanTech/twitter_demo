package com.weefin.twitterdemo

import java.util.concurrent.TimeUnit

import com.danielasfregola.twitter4s.entities.Tweet
import com.typesafe.scalalogging.LazyLogging
import com.weefin.twitterdemo.utils.twitter.entities.SimpleStatus
import com.weefin.twitterdemo.utils.twitter.sink.KafkaJsonProducer
import com.weefin.twitterdemo.utils.twitter.source.AsyncRestRequest
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.async.ResultFuture

import scala.util.{Failure, Success}

object TwitterUserTimelinesToKafka extends App with LazyLogging {
  private val jobName = this.getClass.getSimpleName.split("\\$").last
  private val params = Parameters(args)
  private val env = StreamExecutionEnvironment.getExecutionEnvironment
  logger.info(s"$jobName job started")

  AsyncTimelinesStream(env.fromCollection(params.userIds))
    .map(SimpleStatus(_))
    .addSink(producer)
  env.execute(jobName)

  private def AsyncTimelinesStream(userIdStream: DataStream[Long]) =
    AsyncDataStream
      .unorderedWait(
        userIdStream,
        AsyncTimelineRequest,
        10,
        TimeUnit.SECONDS,
        20
      )

  private def AsyncTimelineRequest =
    new AsyncRestRequest[Long, Tweet](
      params.consumerKey,
      params.consumerSecret,
      params.token,
      params.tokenSecret
    ) {
      override def asyncInvoke(input: Long,
                               resultFuture: ResultFuture[Tweet]): Unit = {
        client
          .userTimelineForUserId(input)
          .map(_.data)
          .onComplete {
            case Success(tweets) =>
              logger.info(s"Received timeline for user id $input")
              resultFuture.complete(tweets)
            case Failure(throwable) =>
              logger.warn(
                s"Invalid response for user id $input: ${throwable.getMessage}"
              )
              resultFuture.complete(None)
          }
      }
    }

  private def producer =
    KafkaJsonProducer[SimpleStatus](params.bootstrapServers, params.topicId)
}
