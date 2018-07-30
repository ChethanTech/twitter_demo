package com.weefin.twitterdemo

import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.connectors.twitter.TwitterSource

import scala.util.Try

case class Parameters(uri: String,
                      httpMethod: String,
                      consumerKey: String,
                      consumerSecret: String,
                      token: String,
                      tokenSecret: String,
                      bootstrapServers: String,
                      topicId: String)

object Parameters {
  private val defaultUri = "/1.1/statuses/sample.json"
  private val defaultHttpMethod = "GET"
  private val defaultBootstrapServers = "localhost:9092"

  def apply(args: Array[String]): Parameters = {
    val params = ParameterTool.fromArgs(args)
    Try(
      new Parameters(
        params.get("uri", defaultUri),
        params.get("http-method", defaultHttpMethod),
        params.getRequired(TwitterSource.CONSUMER_KEY),
        params.getRequired(TwitterSource.CONSUMER_SECRET),
        params.getRequired(TwitterSource.TOKEN),
        params.getRequired(TwitterSource.TOKEN_SECRET),
        params.get("bootstrap.servers", defaultBootstrapServers),
        params.getRequired("topic.id")
      )
    ).getOrElse(throwInvalidArgs)
  }

  private def throwInvalidArgs =
    throw new IllegalArgumentException(
      """Invalid arguments. Usage: TwitterStreamToKafka
			| --uri <uri>
			| --http-method <method>
			| --twitter-source.consumerKey <key>
			| --twitter-source.consumerSecret <secret>
			| --twitter-source.token <token>
			| --twitter-source.tokenSecret <tokenSecret>
			| --bootstrap.servers <server1[,server2,...]>
			| --topic.id <id>
			| """.stripMargin
    )
}
