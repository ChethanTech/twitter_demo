package com.weefin

import java.util.StringTokenizer

import com.twitter.hbc.core.endpoint.RawEndpoint
import org.apache.flink.api.common.functions.FlatMapFunction
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.{JsonMappingException, JsonNode, ObjectMapper}
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.connectors.twitter.TwitterSource
import org.apache.flink.util.Collector
import org.slf4j.{Logger, LoggerFactory}

object TwitterExample {
	val logger: Logger = LoggerFactory.getLogger(getClass)
	
	def main(args: Array[String]) {
		val params = ParameterTool.fromArgs(args)
		if (!validateArguments(params)) {
			logger.error(
				"Invalid arguments. Usage: TwitterExample --uri <uri> --http-method <method> --twitter-source" +
					".consumerKey <key> --twitter-source.consumerSecret <secret> --twitter-source.token <token> " +
					"--twitter-source.tokenSecret <tokenSecret>")
			return
		}
		val env = StreamExecutionEnvironment.getExecutionEnvironment
		val twitterSource = new TwitterSource(params.getProperties)
		twitterSource.setCustomEndpointInitializer(new TweetFilter(params.get("uri"), params.get("http-method")))
		env.addSource(twitterSource).flatMap(new SelectEnglishAndTokenizeFlatMap).keyBy(0).sum(1).print
		env.execute("Twitter Streaming Example")
	}
	
	private def validateArguments(params: ParameterTool) = {
		params.has(TwitterSource.CONSUMER_KEY) && params.has(TwitterSource.CONSUMER_SECRET) &&
			params.has(TwitterSource.TOKEN) && params.has(TwitterSource.TOKEN_SECRET) && params.has("uri") &&
			params.has("http-method")
	}
	
	private class SelectEnglishAndTokenizeFlatMap extends FlatMapFunction[String, (String, Int)] {
		lazy val jsonParser = new ObjectMapper()
		
		override def flatMap(value: String, out: Collector[(String, Int)]): Unit = {
			try {
				val jsonNode = jsonParser.readValue(value, classOf[JsonNode])
				val isEnglish = jsonNode.has("user") && jsonNode.get("user").has("lang") &&
					jsonNode.get("user").get("lang").asText == "en"
				val hasText = jsonNode.has("text")
				
				if (isEnglish && hasText) {
					val tokenizer = new StringTokenizer(jsonNode.get("text").asText())
					while (tokenizer.hasMoreTokens) {
						val token = tokenizer.nextToken().replaceAll("\\s*", "").toLowerCase()
						if (token.nonEmpty) out.collect((token, 1))
					}
				}
			} catch {
				case e: JsonMappingException => print(e.getMessage)
			}
		}
	}
	
	private class TweetFilter(uri: String, httpMethod: String)
		extends TwitterSource.EndpointInitializer with Serializable {
		override def createEndpoint: RawEndpoint = new RawEndpoint(uri, httpMethod)
	}
	
}
