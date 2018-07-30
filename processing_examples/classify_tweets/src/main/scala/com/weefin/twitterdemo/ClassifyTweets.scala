package com.weefin.twitterdemo

import com.danielasfregola.twitter4s.entities.Tweet
import com.typesafe.scalalogging.LazyLogging
import com.weefin.twitterdemo.utils.twitter.entities.{ClassifiedEntity, SimpleStatus}
import com.weefin.twitterdemo.utils.twitter.flatmap.FlatMapDefinedOption
import com.weefin.twitterdemo.utils.twitter.map.MapClassifyByHashtags
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
    .flatMap(FlatMapDefinedOption[Option[Tweet], Tweet])
    .map(SimpleStatus(_))
    .map(MapClassifyByHashtags)
    .map(new ClassifiedSimpleStatus(_))
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
                                            mainClass: String) {
    def this(classifiedEntity: ClassifiedEntity[SimpleStatus]) =
      this(
        classifiedEntity.entity,
        classifiedEntity.mainClass.getOrElse("None").toString
      )
  }

}
