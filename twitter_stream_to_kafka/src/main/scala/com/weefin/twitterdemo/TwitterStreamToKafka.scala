package com.weefin.twitterdemo

import com.typesafe.scalalogging.LazyLogging
import com.weefin.twitterdemo.utils.twitter.RawTwitterSource
import org.apache.flink.api.common.functions.FilterFunction
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011

object TwitterStreamToKafka extends App with LazyLogging {
	logger.info("Twitter stream to kafka job started")
	val params = Parameters(args)
	val env = StreamExecutionEnvironment.getExecutionEnvironment
	val source = getTwitterSource
	val sink = getProducer
	env.addSource(source).filter(LogFilter).addSink(sink)
	env.execute("Twitter stream to kafka")
	
	private object LogFilter extends FilterFunction[String] with LazyLogging {
		override def filter(message: String): Boolean = {
			logger.debug(s"Received status: ${message.substring(0, 100)}…")
			true
		}
	}
	
	private def getTwitterSource = RawTwitterSource(params.uri, params.httpMethod, params.consumerKey,
		params.consumerSecret, params.token, params.tokenSecret)
	
	private def getProducer = {
		val p = new FlinkKafkaProducer011[String](params.bootstrapServers, params.topicId, new SimpleStringSchema)
		p.setWriteTimestampToKafka(true)
		p
	}
}
