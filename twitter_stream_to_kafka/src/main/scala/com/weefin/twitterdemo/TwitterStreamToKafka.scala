package com.weefin.twitterdemo

import com.weefin.twitterdemo.utils.twitter.RawTwitterSource
import org.apache.flink.api.common.functions.FilterFunction
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011
import org.apache.flink.streaming.connectors.twitter.TwitterSource
import org.slf4j.{Logger, LoggerFactory}

object TwitterStreamToKafka extends App {
	val logger: Logger = LoggerFactory.getLogger(getClass)
	val defaultUri = "/1.1/statuses/sample.json"
	val defaultHttpMethod = "GET"
	val defaultBootstrapServers = "localhost:9092"
	val params = ParameterTool.fromArgs(args)
	if (!validateArguments) {
		logErrorInvalidArguments()
		sys.exit(1)
	}
	val env = StreamExecutionEnvironment.getExecutionEnvironment
	val source = getTwitterSource
	val sink = getProducer
	logger.info("Job *Twitter stream to kafka* started")
	env.addSource(source).filter(LogFilter).addSink(sink)
	env.execute("Twitter stream to kafka")
	
	private object LogFilter extends FilterFunction[String] {
		private val logger = LoggerFactory.getLogger(getClass)
		
		override def filter(message: String): Boolean = {
			logger.trace("Received status: {}…", message.substring(0, 100))
			true
		}
	}
	
	private def validateArguments = params.has(TwitterSource.CONSUMER_KEY) &&
		params.has(TwitterSource.CONSUMER_SECRET) && params.has(TwitterSource.TOKEN) &&
		params.has(TwitterSource.TOKEN_SECRET) && params.has("topic.id")
	
	private def logErrorInvalidArguments(): Unit = logger
		.error("Invalid arguments. Usage: TwitterStreamToKafka --uri <uri> --http-method <method> " +
			"--twitter-source.consumerKey <key> --twitter-source.consumerSecret <secret> --twitter-source" +
			".token <token> --twitter-source.tokenSecret <tokenSecret> --bootstrap.servers <server1[,server2," +
			"...]> --topic.id <id>")
	
	private def getTwitterSource = RawTwitterSource(params.get("uri", defaultUri),
		params.get("http-method", defaultHttpMethod), params.get(TwitterSource.CONSUMER_KEY),
		params.get(TwitterSource.CONSUMER_SECRET), params.get(TwitterSource.TOKEN),
		params.get(TwitterSource.TOKEN_SECRET))
	
	private def getProducer = {
		val p = new FlinkKafkaProducer011[String](params.get("bootstrap.servers", defaultBootstrapServers),
			params.get("topic.id"), new SimpleStringSchema)
		p.setWriteTimestampToKafka(true)
		p
	}
}
