package com.weefin.twitterdemo

import java.util.concurrent.TimeUnit

import com.danielasfregola.twitter4s.entities.Tweet
import com.typesafe.scalalogging.LazyLogging
import com.weefin.twitterdemo.utils.twitter.sink.KafkaJsonProducer
import com.weefin.twitterdemo.utils.twitter.source.AsyncTwitterRequest
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.async.ResultFuture

import scala.util.{Failure, Success}

object TwitterUserTimelinesToKafka extends App with LazyLogging {
  private val jobName = this.getClass.getSimpleName.split("\\$").last
  private val params = Parameters(args)
  private val env = StreamExecutionEnvironment.getExecutionEnvironment
  logger.info(s"$jobName job started")

  val users: DataStream[Long] = env.fromCollection(params.userIds)

  val timelines: DataStream[Seq[Tweet]] = AsyncDataStream
    .unorderedWait(users, asyncTimelineRequest, 5, TimeUnit.SECONDS, 100)

  val tweets: DataStream[Tweet] = timelines.flatMap(identity(_))

  tweets.addSink(producer)
  env.execute(jobName)

  private def asyncTimelineRequest =
    new AsyncTwitterRequest[Long, Seq[Tweet]](
      params.consumerKey,
      params.consumerSecret,
      params.token,
      params.tokenSecret
    ) {
      override def timeout(user: Long,
                           resultFuture: ResultFuture[Seq[Tweet]]): Unit = {
        logger.warn(s"Get timeline for user id $user: query timed out")
        resultFuture.complete(Iterable.empty)
      }

      override def asyncInvoke(user: Long,
                               resultFuture: ResultFuture[Seq[Tweet]]): Unit =
        client
          .userTimelineForUserId(user)
          .map(_.data)
          .onComplete {
            case Success(timeline) =>
              logger.info(
                s"Get timeline for user id $user: received the ${timeline.length} most recent Tweets"
              )
              resultFuture.complete(Iterable(timeline))
            case Failure(throwable) =>
              logger.warn(
                s"Get timeline for user id $user: received error '${throwable.getMessage}'"
              )
              resultFuture.complete(Iterable.empty)
          }
    }

  private def producer =
    KafkaJsonProducer[Tweet](params.bootstrapServers, params.topicId)
}
