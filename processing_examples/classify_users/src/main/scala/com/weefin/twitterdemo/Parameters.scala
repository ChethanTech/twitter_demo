package com.weefin.twitterdemo

import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.connectors.twitter.TwitterSource

import scala.util.Try

case class Parameters(consumerKey: String,
                      consumerSecret: String,
                      token: String,
                      tokenSecret: String,
                      consumerBootstrapServers: String,
                      producerBootstrapServers: String,
                      consumerGroupId: String,
                      consumerTopicId: String,
                      producerTopicId: String,
                      classificationFile: String)

object Parameters {
  private val defaultBootstrapServers = "localhost:9092"

  def apply(args: Array[String]): Parameters = {
    val params = ParameterTool.fromArgs(args)
    Try(
      new Parameters(
        params.getRequired(TwitterSource.CONSUMER_KEY),
        params.getRequired(TwitterSource.CONSUMER_SECRET),
        params.getRequired(TwitterSource.TOKEN),
        params.getRequired(TwitterSource.TOKEN_SECRET),
        params.get("consumer.bootstrap.servers", defaultBootstrapServers),
        params.get("producer.bootstrap.servers", defaultBootstrapServers),
        params.getRequired("consumer.group.id"),
        params.getRequired("consumer.topic.id"),
        params.getRequired("producer.topic.id"),
        params.getRequired("classification-file")
      )
    ).getOrElse(throwInvalidArgs)
  }

  private def throwInvalidArgs =
    throw new IllegalArgumentException(
      """Invalid arguments. Usage: classify_users
	      | --twitter-source.consumerKey <key>
	      | --twitter-source.consumerSecret <secret>
	      | --twitter-source.token <token>
	      | --twitter-source.tokenSecret <tokenSecret>
	      | --consumer.bootstrap.servers <server1[,server2,...]>
	      | --producer.bootstrap.servers <server1[,server2,...]>
	      | --consumer.group.id <id>
	      | --consumer.topic.id <id>
	      | --producer.topic.id <id>
	      | --classification-file <path>
	      | """.stripMargin
    )
}
