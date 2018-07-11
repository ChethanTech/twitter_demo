package com.weefin.twitterdemo

import com.danielasfregola.twitter4s.entities.{HashTag, Tweet}
import com.typesafe.scalalogging.LazyLogging
import com.weefin.twitterdemo.utils.twitter._
import org.apache.flink.api.common.functions.RichFilterFunction
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}

object ExtractHashtags extends App with LazyLogging {
	logger.info("Count hashtags job started")
	val params = Parameters(args)
	val env = StreamExecutionEnvironment.getExecutionEnvironment
	env.addSource(consumer).flatMap(FlatMapExtractHashtags).flatMap(FlatMapExplodeSeq[HashTag])
		.filter(new FilterHashtags(params)).addSink(producer)
	env.execute("Extract hashtags")
	
	private class FilterHashtags(params: Parameters) extends RichFilterFunction[HashTag] {
		private val whiteList = params.whiteList
		private val blackList = params.blackList
		
		override def filter(value: HashTag): Boolean = if (whiteList.nonEmpty) whiteList
			.contains(value.text.toLowerCase) else !blackList.contains(value.text.toLowerCase)
	}
	
	private def consumer = KafkaJsonConsumer[Tweet](params.consumerBootstrapServers, params.consumerTopicId,
		params.consumerGroupId)
	
	private def producer = KafkaJsonProducer[HashTag](params.producerBootstrapServers, params.producerTopicId)
}
