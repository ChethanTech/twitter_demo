package com.weefin.twitterdemo

import java.util.Properties

import com.danielasfregola.twitter4s.entities.{HashTag, Tweet}
import com.danielasfregola.twitter4s.http.serializers.JsonSupport
import com.typesafe.scalalogging.LazyLogging
import org.apache.flink.api.common.functions.RichFilterFunction
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer011, FlinkKafkaProducer011}
import org.apache.flink.util.Collector
import org.json4s.native.Serialization

import scala.util.Try

object ExtractHashtags extends App with LazyLogging with JsonSupport {
	logger.info("Count hashtags job started")
	val params = Parameters(args)
	val env = StreamExecutionEnvironment.getExecutionEnvironment
	env.addSource(consumer).map(json => Try(Serialization.read[Tweet](json)).toOption).flatMap(ExtractHashtags)
		.filter(new FilterHashtags(params)).map(Serialization.write(_)).addSink(producer)
	env.execute("Extract hashtags")
	
	private def ExtractHashtags = (tweet: Option[Tweet], out: Collector[HashTag]) => {
		def extractHashtags = (tweet: Option[Tweet]) => tweet
			.flatMap(tweet => tweet.extended_entities.orElse(tweet.entities))
			.foreach(_.hashtags.foreach(hashtag => out.collect(hashtag)))
		
		if (tweet.exists(_.is_quote_status)) {
			extractHashtags(tweet.flatMap(_.quoted_status))
		} else if (tweet.exists(_.retweeted)) {
			extractHashtags(tweet.flatMap(_.retweeted_status))
		} else {
			extractHashtags(tweet)
		}
	}
	
	private class FilterHashtags(params: Parameters) extends RichFilterFunction[HashTag] {
		private val whiteList = params.whiteList
		private val blackList = params.blackList
		
		override def filter(value: HashTag): Boolean = if (whiteList.nonEmpty) whiteList
			.contains(value.text.toLowerCase) else !blackList.contains(value.text.toLowerCase)
	}
	
	private def consumer = new FlinkKafkaConsumer011[String](params.consumerTopicId, new SimpleStringSchema,
		new Properties {
			setProperty("bootstrap.servers", params.consumerBootstrapServers)
			setProperty("group.id", params.consumerGroupId)
		})
	
	private def producer = new FlinkKafkaProducer011[String](params.producerBootstrapServers, params.producerTopicId,
		new SimpleStringSchema) {
		setWriteTimestampToKafka(true)
	}
}
