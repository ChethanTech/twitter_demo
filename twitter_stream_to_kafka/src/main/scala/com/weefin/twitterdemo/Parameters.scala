package com.weefin.twitterdemo

import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.connectors.twitter.TwitterSource

import scala.util.Try

object Parameters {
	val defaultUri = "/1.1/statuses/sample.json"
	val defaultHttpMethod = "GET"
	val defaultBootstrapServers = "localhost:9092"
	
	private def throwInvalidArgs = throw new IllegalArgumentException(
		"""Invalid arguments. Usage: TwitterStreamToKafka
			| --uri <uri>
			| --http-method <method>
			| --twitter-source.consumerKey <key>
			| --twitter-source.consumerSecret <secret>
			| --twitter-source.token <token>
			| --twitter-source.tokenSecret <tokenSecret>
			| --bootstrap.servers <server1[,server2,...]>
			| --topic.id <id>
			| """.stripMargin)
	
	def apply(args: Array[String]): Parameters = new Parameters(args)
}

class Parameters(args: Array[String]) {
	private val params = ParameterTool.fromArgs(args)
	val uri: String = params.get("uri", Parameters.defaultUri)
	val httpMethod: String = params.get("http-method", Parameters.defaultHttpMethod)
	val consumerKey: String = Try(params.getRequired(TwitterSource.CONSUMER_KEY)).getOrElse(Parameters
		.throwInvalidArgs)
	val consumerSecret: String = Try(params.getRequired(TwitterSource.CONSUMER_SECRET))
		.getOrElse(Parameters.throwInvalidArgs)
	val token: String = Try(params.getRequired(TwitterSource.TOKEN)).getOrElse(Parameters.throwInvalidArgs)
	val tokenSecret: String = Try(params.getRequired(TwitterSource.TOKEN_SECRET)).getOrElse(Parameters
		.throwInvalidArgs)
	val bootstrapServers: String = params.get("bootstrap.servers", Parameters.defaultBootstrapServers)
	val topicId: String = Try(params.getRequired("topic.id")).getOrElse(Parameters.throwInvalidArgs)
}
