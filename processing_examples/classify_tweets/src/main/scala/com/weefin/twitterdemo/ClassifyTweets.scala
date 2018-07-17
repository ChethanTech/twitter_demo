package com.weefin.twitterdemo

import com.danielasfregola.twitter4s.entities.Tweet
import com.typesafe.scalalogging.LazyLogging
import com.weefin.twitterdemo.utils.twitter.entities
import com.weefin.twitterdemo.utils.twitter.entities.{SimpleStatus, SimpleStatusClassification}
import com.weefin.twitterdemo.utils.twitter.flatmap.FlatMapDefinedOption
import com.weefin.twitterdemo.utils.twitter.sink.KafkaJsonProducer
import com.weefin.twitterdemo.utils.twitter.source.KafkaJsonConsumer
import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector

object ClassifyTweets extends App with LazyLogging {
	private val jobName = this.getClass.getSimpleName.split("\\$").last
	private val params = Parameters(args)
	private val env = StreamExecutionEnvironment.getExecutionEnvironment
	logger.info(s"$jobName job started")
	env.addSource(consumer).flatMap(FlatMapDefinedOption[Tweet]).map(SimpleStatus(_)).flatMap(map).addSink(producer)
	env.execute(jobName)
	
	private object map extends RichFlatMapFunction[SimpleStatus, (Seq[String], SimpleStatusClassification.Value)] {
		override def flatMap(value: SimpleStatus,
			out: Collector[(Seq[String], entities.SimpleStatusClassification.Value)]): Unit = {
			val classification = SimpleStatusClassification(value).weights
				.filterNot(_._1 == SimpleStatusClassification.Other)
			if (classification.nonEmpty) out.collect((value.hashtags, classification.maxBy(_._2)._1))
		}
	}
	
	private def consumer = KafkaJsonConsumer[Tweet](params.consumerBootstrapServers,
		params.consumerTopicId,
		params.consumerGroupId)
	
	private def producer = KafkaJsonProducer[(Seq[String], SimpleStatusClassification.Value)](params
		.producerBootstrapServers, params.producerTopicId)
	
}
