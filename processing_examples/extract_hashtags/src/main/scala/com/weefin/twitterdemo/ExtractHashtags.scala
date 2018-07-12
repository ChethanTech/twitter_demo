package com.weefin.twitterdemo

import com.danielasfregola.twitter4s.entities.{HashTag, Tweet}
import com.typesafe.scalalogging.LazyLogging
import com.weefin.twitterdemo.utils.twitter.filter.KeywordFilter
import com.weefin.twitterdemo.utils.twitter.flatmap.{FlatMapExplodeSeq, FlatMapExtractHashtags}
import com.weefin.twitterdemo.utils.twitter.sink.KafkaJsonProducer
import com.weefin.twitterdemo.utils.twitter.source.KafkaJsonConsumer
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}

object ExtractHashtags extends App with LazyLogging {
	logger.info("Count hashtags job started")
	val params = Parameters(args)
	val env = StreamExecutionEnvironment.getExecutionEnvironment
	env.addSource(consumer).flatMap(FlatMapExtractHashtags).flatMap(FlatMapExplodeSeq[HashTag]).filter(hashtagFilter)
		.addSink(producer)
	env.execute("Extract hashtags")
	
	private def hashtagFilter = KeywordFilter[HashTag](_.text)(params.whiteList, params.blackList, ignoreCase = true)
	
	private def consumer = KafkaJsonConsumer[Tweet](params.consumerBootstrapServers, params.consumerTopicId,
		params.consumerGroupId)
	
	private def producer = KafkaJsonProducer[HashTag](params.producerBootstrapServers, params.producerTopicId)
}
