package com.weefin.twitterdemo

import java.util.Properties

import org.apache.flink.api.common.functions.{AggregateFunction, RichFilterFunction}
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer011, FlinkKafkaProducer011}
import org.apache.flink.util.Collector
import org.slf4j.{Logger, LoggerFactory}
import scalaz.Scalaz._
import twitter4j.{Status, TwitterObjectFactory}

import scala.util.Try

object CountHashtags extends App {
	val logger: Logger = LoggerFactory.getLogger(getClass)
	val params = Parameters(args)
	val env = StreamExecutionEnvironment.getExecutionEnvironment
	val source = getConsumer
	val sink = getProducer
	env.addSource(source).map(rawJSON => Try(TwitterObjectFactory.createStatus(rawJSON)).toOption)
		.flatMap(FlattenHashtags).map(_.toLowerCase).filter(new HashtagFilter(params))
		.windowAll(SlidingProcessingTimeWindows.of(Time.seconds(params.windowSize), Time.seconds(params.windowSlide)))
		.aggregate(new AggregateHashtags(params)).addSink(sink)
	env.execute("Count hashtags")
	
	private class AggregateHashtags(params: Parameters) extends AggregateFunction[String, Map[String, Long], String] {
		private val displayOnly = params.displayOnly
		private val minOccurrences = params.minOccurrences
		
		override def createAccumulator(): Map[String, Long] = Map.empty.withDefaultValue(0L)
		
		override def add(value: String, accumulator: Map[String, Long]): Map[String, Long] = accumulator +
			(value -> (accumulator(value) + 1))
		
		override def getResult(accumulator: Map[String, Long]): String = accumulator.toSeq.sortBy(-_._2)
			.take(displayOnly).filter(_._2 >= minOccurrences).toString
		
		override def merge(a: Map[String, Long], b: Map[String, Long]): Map[String, Long] = a |+| b
	}
	
	private def FlattenHashtags = (status: Option[Status], out: Collector[String]) => {
		status.flatMap(status => Option(status.getHashtagEntities))
			.foreach(_.foreach(hashtag => out.collect(hashtag.getText)))
		status.flatMap(status => Option(status.getRetweetedStatus)).flatMap(status => Option(status
			.getHashtagEntities))
			.foreach(_.foreach(hashtag => out.collect(hashtag.getText)))
		status.flatMap(status => Option(status.getQuotedStatus)).flatMap(status => Option(status.getHashtagEntities))
			.foreach(_.foreach(hashtag => out.collect(hashtag.getText)))
	}
	
	private class HashtagFilter(params: Parameters) extends RichFilterFunction[String] {
		private val whiteList = params.whiteList
		private val blackList = params.blackList
		
		override def filter(value: String): Boolean = if (whiteList.nonEmpty) whiteList.contains(value) else !blackList
			.contains(value)
	}
	
	private def getConsumer = new FlinkKafkaConsumer011[String](params.consumerTopicId, new SimpleStringSchema(),
		new Properties() {
			setProperty("bootstrap.servers", params.consumerBootstrapServers)
			setProperty("group.id", params.consumerGroupId)
		})
	
	private def getProducer = new FlinkKafkaProducer011[String](params.producerBootstrapServers, params
		.producerTopicId,
		new SimpleStringSchema) {
		setWriteTimestampToKafka(true)
	}
}
