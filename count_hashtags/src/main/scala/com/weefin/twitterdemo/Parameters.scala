package com.weefin.twitterdemo

import org.apache.flink.api.java.utils.ParameterTool

import scala.util.Try

object Parameters {
	val defaultBootstrapServers = "localhost:9092"
	val defaultWindowSize = 60
	val defaultWindowSlide = 5
	val defaultDisplayOnly = 10
	val defaultMinOccurrences = 1
	
	def throwInvalidArgs = throw new IllegalArgumentException(
		"""Invalid arguments. Usage: count_hashtags
			| --consumer.bootstrap.servers <server1[,server2,...]>
			| --consumer.group.id <id>
			| --consumer.topic.id <id>
			| --producer.bootstrap.servers <server1[,server2,...]>
			| --producer.topic.id <id>
			| --black-list <word1[,word2,...]>
			| --white-list <word1[,word2,...]>
			| --window-size <seconds>
			| --window-slide <seconds>
			| --display-only <count>
			| --min-occurrences <count>
			| """.stripMargin)
	
	def apply(args: Array[String]): Parameters = new Parameters(args)
}

class Parameters(args: Array[String]) {
	private val params = ParameterTool.fromArgs(args)
	val producerBootstrapServers: String = Try(
		params.get("producer.bootstrap.servers", Parameters.defaultBootstrapServers))
		.getOrElse(Parameters.throwInvalidArgs)
	val consumerBootstrapServers: String = Try(
		params.get("consumer.bootstrap.servers", Parameters.defaultBootstrapServers))
		.getOrElse(Parameters.throwInvalidArgs)
	val consumerGroupId: String = Try(params.getRequired("consumer.group.id")).getOrElse(Parameters.throwInvalidArgs)
	val consumerTopicId: String = Try(params.getRequired("consumer.topic.id")).getOrElse(Parameters.throwInvalidArgs)
	val producerTopicId: String = Try(params.getRequired("producer.topic.id")).getOrElse(Parameters.throwInvalidArgs)
	val whiteList: Option[String] = Try(Option(params.get("white-list"))).getOrElse(Parameters.throwInvalidArgs)
	val blackList: Option[String] = Try(Option(params.get("black-list"))).getOrElse(Parameters.throwInvalidArgs)
	val windowSize: Int = Try(params.getInt("window-size", Parameters.defaultWindowSize))
		.getOrElse(Parameters.throwInvalidArgs)
	val windowSlide: Int = Try(params.getInt("window-slide", Parameters.defaultWindowSlide))
		.getOrElse(Parameters.throwInvalidArgs)
	val displayOnly: Int = Try(params.getInt("display-only", Parameters.defaultDisplayOnly))
		.getOrElse(Parameters.throwInvalidArgs)
	val minOccurrences: Int = Try(params.getInt("min-occurrences", Parameters.defaultMinOccurrences))
		.getOrElse(Parameters.throwInvalidArgs)
}
