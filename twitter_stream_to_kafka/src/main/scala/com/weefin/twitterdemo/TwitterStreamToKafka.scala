package com.weefin.twitterdemo

import com.twitter.hbc.core.endpoint.RawEndpoint
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011
import org.apache.flink.streaming.connectors.twitter.TwitterSource
import org.slf4j.{Logger, LoggerFactory}

object TwitterStreamToKafka {
	val logger: Logger = LoggerFactory.getLogger(getClass)
	val DEFAULT_URI = "/1.1/statuses/sample.json"
	val DEFAULT_HTTP_METHOD = "GET"
	val DEFAULT_BOOTSTRAP_SERVERS = "localhost:9092"
	
	def main(args: Array[String]) {
		val params = ParameterTool.fromArgs(args)
		if (!validateArguments(params)) {
			logger
				.error("Invalid arguments. Usage: TwitterStreamToKafka --uri <uri> --http-method <method> " +
					"--twitter-source.consumerKey <key> --twitter-source.consumerSecret <secret> --twitter-source" +
					".token <token> --twitter-source.tokenSecret <tokenSecret> --bootstrap.servers <server1[," +
					"server2," + "." + "..]> --topic.id <id>")
			return
		}
		val env = StreamExecutionEnvironment.getExecutionEnvironment
		val twitterSource = new TwitterSource(params.getProperties)
		twitterSource
			.setCustomEndpointInitializer(
				new TweetFilter(params.get("uri", DEFAULT_URI), params.get("http-method", DEFAULT_HTTP_METHOD)))
		val producer = new FlinkKafkaProducer011[String](params.get("bootstrap.servers", DEFAULT_BOOTSTRAP_SERVERS),
			params.get("topic.id"), new SimpleStringSchema)
		producer.setWriteTimestampToKafka(true)
		env.addSource(twitterSource).addSink(producer)
		env.execute("Twitter stream to kafka")
	}
	
	private def validateArguments(params: ParameterTool) = {
		params.has(TwitterSource.CONSUMER_KEY) && params.has(TwitterSource.CONSUMER_SECRET) &&
			params.has(TwitterSource.TOKEN) && params.has(TwitterSource.TOKEN_SECRET) && params.has("topic.id")
	}
	
	private class TweetFilter(uri: String, httpMethod: String)
		extends TwitterSource.EndpointInitializer with Serializable {
		override def createEndpoint: RawEndpoint = new RawEndpoint(uri, httpMethod)
	}
	
}
