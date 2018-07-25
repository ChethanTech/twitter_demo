package com.weefin.twitterdemo

import java.util.concurrent.TimeUnit

import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities.{AccessToken, ConsumerToken, Tweet}
import com.typesafe.scalalogging.LazyLogging
import com.weefin.twitterdemo.utils.twitter.entities.SimpleStatus
import com.weefin.twitterdemo.utils.twitter.sink.KafkaJsonProducer
import org.apache.flink.runtime.concurrent.Executors
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.async.{AsyncFunction, ResultFuture}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object TwitterUserTimelinesToKafka extends App with LazyLogging {
	private val jobName = this.getClass.getSimpleName.split("\\$").last
	private val params = Parameters(args)
	private val env = StreamExecutionEnvironment.getExecutionEnvironment
	logger.info(s"$jobName job started")
	
	AsyncTimelinesStream(env.fromCollection(params.userIds)).map(SimpleStatus(_)).addSink(producer)
	env.execute(jobName)
	
	private def AsyncTimelinesStream(userIdsStream: DataStream[Long]) = AsyncDataStream.unorderedWait(userIdsStream,
		new AsyncTimelineRequest(params.consumerKey, params.consumerSecret, params.token, params.tokenSecret),
		10,
		TimeUnit.SECONDS,
		20)
	
	class AsyncTimelineRequest(consumerKey: String, consumerSecret: String, token: String, tokenSecret: String)
		extends AsyncFunction[Long, Tweet] with LazyLogging {
		
		private lazy val restClient = TwitterRestClient(ConsumerToken(consumerKey, consumerSecret),
			AccessToken(token, tokenSecret))
		implicit lazy val executor: ExecutionContext = ExecutionContext.fromExecutor(Executors.directExecutor())
		
		override def asyncInvoke(value: Long, resultFuture: ResultFuture[Tweet]): Unit = {
			restClient.userTimelineForUserId(value).map(_.data)
				.onComplete { case Success(tweets) => logger.info(s"Received timeline for user id $value")
					resultFuture.complete(tweets)
				case Failure(throwable) => logger.warn(s"Invalid response for user id $value: ${throwable.getMessage}")
					resultFuture.complete(None)
				}
		}
	}
	
	private def producer = KafkaJsonProducer[SimpleStatus](params.bootstrapServers, params.topicId)
}
