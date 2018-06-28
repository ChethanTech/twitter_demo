package com.weefin.twitterdemo

import java.util.Properties

import org.apache.flink.api.common.functions.RichFilterFunction
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer011, FlinkKafkaProducer011}
import org.apache.flink.util.Collector
import org.slf4j.{Logger, LoggerFactory}
import twitter4j.{Status, TwitterObjectFactory}

import scala.util.Try

object CountHashtags extends App {
	val logger: Logger = LoggerFactory.getLogger(getClass)
	val params = Parameters(args)
	val env = StreamExecutionEnvironment.getExecutionEnvironment
	val source = getConsumer
	val sink = getProducer
	env.addSource(source).map(rawJSON => Try(TwitterObjectFactory.createStatus(rawJSON)).toOption).flatMap(HashtagMap)
		.map(_.toLowerCase).filter(new HashtagFilter(params)).addSink(sink)
	env.execute("Count hashtags")
	
	private def HashtagMap = (status: Option[Status], out: Collector[String]) => {
		status.flatMap(status => Option(status.getHashtagEntities))
			.foreach(_.foreach(hashtag => out.collect(hashtag.getText)))
		status.flatMap(status => Option(status.getRetweetedStatus)).flatMap(status => Option(status
			.getHashtagEntities))
			.foreach(_.foreach(hashtag => out.collect(hashtag.getText)))
		status.flatMap(status => Option(status.getQuotedStatus)).flatMap(status => Option(status.getHashtagEntities))
			.foreach(_.foreach(hashtag => out.collect(hashtag.getText)))
	}
	
	private class HashtagFilter(params: Parameters) extends RichFilterFunction[String] {
		private val whiteList = params.whiteList.map(_.split(",").map(_.trim.toLowerCase).filter(_.nonEmpty).toSet)
		private val blackList = params.blackList.map(_.split(",").map(_.trim.toLowerCase).filter(_.nonEmpty).toSet)
		
		override def filter(value: String): Boolean = if (whiteList.exists(_.nonEmpty)) whiteList.get
			.contains(value) else !blackList.exists(_.contains(value))
	}
	
	private def getConsumer = new FlinkKafkaConsumer011[String](params.consumerTopicId.get, new SimpleStringSchema(),
		new Properties() {
			setProperty("bootstrap.servers", params.consumerBootstrapServers.get)
			setProperty("group.id", params.consumerGroupId.get)
		}).setStartFromEarliest()
	
	private def getProducer = new FlinkKafkaProducer011[String](params.producerBootstrapServers.get,
		params.producerTopicId.get, new SimpleStringSchema) {
		setWriteTimestampToKafka(true)
	}
}
