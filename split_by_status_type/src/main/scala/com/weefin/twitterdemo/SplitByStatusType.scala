package com.weefin.twitterdemo

import java.util.Properties

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer011, FlinkKafkaProducer011}
import org.slf4j.{Logger, LoggerFactory}
import twitter4j.{Status, TwitterObjectFactory}

object SplitByStatusType extends App {
	val logger: Logger = LoggerFactory.getLogger(getClass)
	val params = ParameterTool.fromArgs(args)
	val defaultBootstrapServers = "localhost:9092"
	if (!validateArguments) {
		logErrorInvalidArguments()
		sys.exit(1)
	}
	val env = StreamExecutionEnvironment.getExecutionEnvironment
	val source = getConsumer
	val sink = getProducer
	env.addSource(source).map(TwitterObjectFactory.createObject(_)).filter(_.isInstanceOf[Status])
		.map(_.asInstanceOf[Status]).map(_.getCreatedAt.toString).addSink(sink)
	env.execute("Split By Status Type")
	
	private def validateArguments = params.has("consumer.group.id") && params.has("consumer.topic.id") &&
		params.has("producer.topic.id")
	
	private def logErrorInvalidArguments(): Unit = logger
		.error("Invalid arguments. Usage: split_by_status_type --consumer.bootstrap.servers <server1[,server2,...]> " +
			"--consumer.group.id <id> --consumer.topic.id <id> --producer.bootstrap.servers <server1[,server2,.." +
			".]> --producer.topic.id <id>")
	
	private def getConsumer = {
		val properties = new Properties()
		properties.setProperty("bootstrap.servers", params.get("consumer.bootstrap.servers", defaultBootstrapServers))
		properties.setProperty("group.id", params.get("consumer.group.id"))
		new FlinkKafkaConsumer011[String](params.get("consumer.topic.id"), new SimpleStringSchema(), properties)
			.setStartFromEarliest()
	}
	
	private def getProducer = {
		val p = new FlinkKafkaProducer011[String](params.get("producer.bootstrap.servers", defaultBootstrapServers),
			params.get("producer.topic.id"), new SimpleStringSchema)
		p.setWriteTimestampToKafka(true)
		p
	}
}
