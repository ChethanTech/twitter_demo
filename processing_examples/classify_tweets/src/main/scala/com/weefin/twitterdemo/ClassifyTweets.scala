package com.weefin.twitterdemo

import com.danielasfregola.twitter4s.entities.Tweet
import com.github.tototoshi.csv.CSVReader
import com.typesafe.scalalogging.LazyLogging
import com.weefin.twitterdemo.utils.twitter.entities.{
  ClassifiedEntity,
  SimpleStatus
}
import com.weefin.twitterdemo.utils.twitter.map.ClassificationMap
import com.weefin.twitterdemo.utils.twitter.sink.KafkaJsonProducer
import com.weefin.twitterdemo.utils.twitter.source.KafkaJsonConsumer
import org.apache.flink.streaming.api.scala._

import scala.io.Source

object ClassifyTweets extends App with LazyLogging {
  private val jobName = this.getClass.getSimpleName.split("\\$").last
  private val params = Parameters(args)
  private val env = StreamExecutionEnvironment.getExecutionEnvironment
  private type SS = SimpleStatus
  private type CSS = ClassifiedEntity[SS]
  logger.info(s"$jobName job started")

  private val tweets: DataStream[Tweet] =
    env.addSource(consumer).flatMap(identity(_))

  private val simpleTweets: DataStream[SS] =
    tweets.map(SimpleStatus(_))

  private val classifiedStatuses: DataStream[CSS] =
    simpleTweets.map(csvClassificationMap)

  classifiedStatuses.addSink(producer)
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
    new ClassificationMap[SS, CSS](csvToMap(params.classificationFile)) {
      override def map(status: SS) = {
        ClassifiedEntity(status, fromSimpleStatuses(Some(3), status))
      }
    }

  private def consumer =
    KafkaJsonConsumer[Tweet](
      params.consumerBootstrapServers,
      params.consumerTopicId,
      params.consumerGroupId
    )

  private def producer =
    KafkaJsonProducer[CSS](
      params.producerBootstrapServers,
      params.producerTopicId
    )
}
