package com.weefin.twitterdemo

import java.util.concurrent.TimeUnit

import com.danielasfregola.twitter4s.entities.Tweet
import com.github.tototoshi.csv.CSVReader
import com.typesafe.scalalogging.LazyLogging
import com.weefin.twitterdemo.utils.twitter.entities.{
  ClassifiedEntity,
  SimpleStatus,
  SimpleUser
}
import com.weefin.twitterdemo.utils.twitter.map.ClassificationMap
import com.weefin.twitterdemo.utils.twitter.sink.KafkaJsonProducer
import com.weefin.twitterdemo.utils.twitter.source.{
  AsyncTwitterFunction,
  KafkaJsonConsumer
}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.async.ResultFuture

import scala.io.Source
import scala.util.{Failure, Success, Try}

object ClassifyUsers extends App with LazyLogging {
  private val jobName = this.getClass.getSimpleName.split("\\$").last
  private val params = Parameters(args)
  private val env = StreamExecutionEnvironment.getExecutionEnvironment
  private type SU = SimpleUser
  private type CSU = ClassifiedEntity[SU]
  private type SS = SimpleStatus
  logger.info(s"$jobName job started")

  private val users: DataStream[SU] =
    env.addSource(consumer).flatMap(identity(_))

  private val timelines: DataStream[(SU, Seq[Tweet])] = AsyncDataStream
    .unorderedWait(
      users,
      asyncTimelineRequest(params.tweetCount, params.queryCount),
      5,
      TimeUnit.SECONDS,
      100
    )
    .filter(_._2.nonEmpty)

  private val simpleTimelines: DataStream[(SU, Seq[SS])] =
    timelines.map(t => (t._1, t._2.map(SimpleStatus(_))))

  private val classifiedUsers =
    simpleTimelines.map(csvClassificationMap)

  classifiedUsers.addSink(producer)
  env.execute(jobName)

  private def csvToMap(y: String): Map[String, (String, Float)] = {
    val reader = CSVReader.open(Source.fromFile(y))
    val map = reader.allWithHeaders.map { m =>
      m("term").toLowerCase -> (m("label"), m.getOrElse("weight", "1").toFloat)
    }.toMap
    reader.close
    map
  }

  private def csvClassificationMap =
    new ClassificationMap[(SU, Seq[SS]), CSU](
      csvToMap(params.classificationFile)
    ) {
      override def map(value: (SU, Seq[SS])) = {
        val cs = value._2.flatMap(x => fromWords(x.hashtags: _*))
        val m = Try(cs.groupBy(identity).maxBy(_._2.size)._1).toOption
        ClassifiedEntity(value._1, m, Some(Math.min(1F, cs.length / 25F)))
      }
    }

  private def asyncTimelineRequest(tweetCount: Option[Int],
                                   queryCount: Option[Int]) =
    new AsyncTwitterFunction[SU, (SU, Seq[Tweet])](
      params.consumerKey,
      params.consumerSecret,
      params.token,
      params.tokenSecret
    ) {
      override def timeout(
        user: SU,
        resultFuture: ResultFuture[(SU, Seq[Tweet])]
      ): Unit = {
        logger.warn(s"Get timeline for user id ${user.id}: query timed out")
        resultFuture.complete(Iterable.empty)
      }

      override def asyncInvoke(
        user: SU,
        resultFuture: ResultFuture[(SU, Seq[Tweet])]
      ): Unit =
        getTimeline(user.id, tweetCount, queryCount).onComplete {
          case Success(tweets) =>
            logger.info(
              s"Get timeline for user id ${user.id}: received the ${tweets.length} most recent Tweets"
            )
            resultFuture.complete(Iterable((user, tweets)))
          case Failure(throwable) =>
            logger.warn(
              s"Get timeline for user id ${user.id}: received error '${throwable.getMessage}'"
            )
            resultFuture.complete(Iterable.empty)
        }
    }

  private def consumer =
    KafkaJsonConsumer[SU](
      params.consumerBootstrapServers,
      params.consumerTopicId,
      params.consumerGroupId
    )

  private def producer =
    KafkaJsonProducer[CSU](
      params.producerBootstrapServers,
      params.producerTopicId
    )
}
