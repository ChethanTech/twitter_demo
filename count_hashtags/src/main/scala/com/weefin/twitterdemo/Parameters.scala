package com.weefin.twitterdemo

import org.apache.flink.api.java.utils.ParameterTool

object Parameters {
	private val required = Seq("consumer.group.id", "consumer.topic.id", "producer.topic.id")
	
	def apply(args: Array[String]): Parameters = new Parameters(args)
}

class Parameters(args: Array[String]) {
	private val params = ParameterTool.fromArgs(args)
	if (!Parameters.required.forall(params.has(_))) {
		throw new IllegalArgumentException(
			"""Invalid arguments. Usage: count_hashtags
			  | --consumer.bootstrap.servers <server1[,server2,...]>
			  | --consumer.group.id <id>
			  | --consumer.topic.id <id>
			  | --producer.bootstrap.servers <server1[,server2,...]>
			  | --producer.topic.id <id>
			  | --black-list <word1[,word2,...]>
			  | --white-list <word1[,word2,...]>
			  | """.stripMargin)
	}
	
	def producerBootstrapServers = Option(params.get("producer.bootstrap.servers", "localhost:9092"))
	
	def consumerBootstrapServers = Option(params.get("consumer.bootstrap.servers", "localhost:9092"))
	
	def consumerGroupId = Option(params.get("consumer.group.id"))
	
	def consumerTopicId = Option(params.get("consumer.topic.id"))
	
	def producerTopicId = Option(params.get("producer.topic.id"))
	
	def whiteList = Option(params.get("white-list"))
	
	def blackList = Option(params.get("black-list"))
}
