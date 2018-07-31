package com.weefin.twitterdemo

import java.util.concurrent.TimeUnit

import com.danielasfregola.twitter4s.entities.Tweet
import com.typesafe.scalalogging.LazyLogging
import com.weefin.twitterdemo.utils.twitter.entities.{
  ClassifiedEntity,
  SimpleStatus,
  SimpleUser,
  WordClassification
}
import com.weefin.twitterdemo.utils.twitter.sink.KafkaJsonProducer
import com.weefin.twitterdemo.utils.twitter.source.{
  AsyncRestRequest,
  KafkaJsonConsumer
}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.async.ResultFuture
import scalaz.Scalaz._

import scala.util.{Failure, Success}

object ClassifyUsers extends App with LazyLogging {
  private val jobName = this.getClass.getSimpleName.split("\\$").last
  private val params = Parameters(args)
  private val env = StreamExecutionEnvironment.getExecutionEnvironment
  logger.info(s"$jobName job started")

  private val users: DataStream[SimpleUser] =
    env.addSource(consumer).flatMap(identity(_))

  private val timelines: DataStream[(SimpleUser, Seq[Tweet])] = AsyncDataStream
    .unorderedWait(users, AsyncTimelineRequest, 5, TimeUnit.SECONDS, 50)
    .filter(_._2.nonEmpty)

  private val simpleTimelines: DataStream[(SimpleUser, Seq[SimpleStatus])] =
    timelines.map(t => (t._1, t._2.map(SimpleStatus(_))))

  private val classifiedTimelines
    : DataStream[(SimpleUser, Seq[Map[WordClassification.Value, Float]])] =
    simpleTimelines.map { t =>
      (t._1, t._2.map(s => WordClassification(s.hashtags)))
    }

  private val classifiedUsers: DataStream[ClassifiedEntity[SimpleUser]] =
    classifiedTimelines.map { t =>
      ClassifiedEntity(
        t._1,
        t._2
          .reduce(_.mapValues(List(_)) |+| _.mapValues(List(_)))
          .mapValues(scores => scores.sum / scores.length),
        Math.min(48, t._2.length - 1) * 0.8F / 49 + 0.2F
      )
    }

  classifiedUsers.addSink(producer)
  env.execute(jobName)

  private def AsyncTimelineRequest =
    new AsyncRestRequest[SimpleUser, (SimpleUser, Seq[Tweet])](
      params.consumerKey,
      params.consumerSecret,
      params.token,
      params.tokenSecret
    ) {
      override def asyncInvoke(
        user: SimpleUser,
        resultFuture: ResultFuture[(SimpleUser, Seq[Tweet])]
      ): Unit = {
        client
          .userTimelineForUserId(user.id)
          .map(_.data)
          .onComplete {
            case Success(tweets) =>
              logger.info(
                s"Received the ${tweets.length} most recent Tweets for user id $user"
              )
              resultFuture.complete(Iterable((user, tweets)))
            case Failure(throwable) =>
              logger.warn(
                s"Invalid response for user id $user: ${throwable.getMessage}"
              )
              resultFuture.complete(Iterable.empty)
          }
      }
    }

  private def consumer =
    KafkaJsonConsumer[SimpleUser](
      params.consumerBootstrapServers,
      params.consumerTopicId,
      params.consumerGroupId
    )

  private def producer =
    KafkaJsonProducer[ClassifiedEntity[SimpleUser]](
      params.producerBootstrapServers,
      params.producerTopicId
    )
}
