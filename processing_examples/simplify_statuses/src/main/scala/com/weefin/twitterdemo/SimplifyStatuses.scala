package com.weefin.twitterdemo

import com.danielasfregola.twitter4s.entities.Tweet
import com.typesafe.scalalogging.LazyLogging
import com.weefin.twitterdemo.utils.twitter.entities.SimpleStatus
import com.weefin.twitterdemo.utils.twitter.sink.KafkaJsonProducer
import com.weefin.twitterdemo.utils.twitter.source.KafkaJsonConsumer
import org.apache.flink.streaming.api.scala._

object SimplifyStatuses extends App with LazyLogging {
  private val jobName = this.getClass.getSimpleName.split("\\$").last
  private val params = Parameters(args)
  private val env = StreamExecutionEnvironment.getExecutionEnvironment
  logger.info(s"$jobName job started")

  private val simpleStatuses: DataStream[SimpleStatus] = env
    .addSource(consumer)
    .flatMap(_.map(SimpleStatus(_)))

  //private val simpleStatuses2 = AsyncDataStream.unorderedWait(
  //  simpleStatuses,
  //  asyncRichUserQuery,
  //  5,
  //  TimeUnit.SECONDS,
  //  100
  //)

  simpleStatuses.addSink(producer)
  env.execute(jobName)

  //private def asyncRichUserQuery =
  //  new AsyncElasticQuery[SimpleStatus, SimpleStatus]("http://localhost:9200") {
  //    import com.sksamuel.elastic4s.http.ElasticDsl._
  //    override def asyncInvoke(user: SimpleStatus,
  //                             resultFuture: ResultFuture[SimpleStatus]): Unit =
  //      client
  //        .execute(search("classified_users").matchQuery("capital", "ulaanbaatar"))
  //        .onComplete {
  //          case Success(s) =>
  //            logger.info(
  //              s"Successful Elasticsearch query: received ${s.result.hits.hits.head.sourceAsString}"
  //            )
  //            resultFuture.complete(Iterable(user))
  //          case Failure(t) =>
  //            logger.info(s"Elasticsearch query failed: ${t.getMessage}")
  //            resultFuture.complete(Iterable.empty)
  //        }
  //  }

  private def consumer =
    KafkaJsonConsumer[Tweet](
      params.consumerBootstrapServers,
      params.consumerTopicId,
      params.consumerGroupId
    )

  private def producer =
    KafkaJsonProducer[SimpleStatus](
      params.producerBootstrapServers,
      params.producerTopicId
    )
}
