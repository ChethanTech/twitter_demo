package com.weefin.twitterdemo

import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.connectors.twitter.TwitterSource

import scala.util.Try

case class Parameters(userIds: Seq[Long],
                      consumerKey: String,
                      consumerSecret: String,
                      token: String,
                      tokenSecret: String,
                      bootstrapServers: String,
                      topicId: String,
                      tweetCount: Option[Int],
                      queryCount: Option[Int])

object Parameters {
  private val defaultBootstrapServers = "localhost:9092"

  def apply(args: Array[String]): Parameters = {
    val params = ParameterTool.fromArgs(args)
    Try(
      new Parameters(
        asLongSet(params.getRequired("user-ids")),
        params.getRequired(TwitterSource.CONSUMER_KEY),
        params.getRequired(TwitterSource.CONSUMER_SECRET),
        params.getRequired(TwitterSource.TOKEN),
        params.getRequired(TwitterSource.TOKEN_SECRET),
        params.get("bootstrap.servers", defaultBootstrapServers),
        params.getRequired("topic.id"),
        Option(params.get("tweet-count")).map(_.toInt),
        Option(params.get("query-count")).map(_.toInt)
      )
    ).getOrElse(throwInvalidArgs)
  }

  private def asLongSet =
    (list: String) =>
      list.split(",").map(_.trim).filter(_.nonEmpty).map(_.toLong).distinct

  private def throwInvalidArgs =
    throw new IllegalArgumentException(
      """Invalid arguments. Usage: TwitterUserTimelinesToKafka
			| --user-ids <id1[,id2,...]>
			| --twitter-source.consumerKey <key>
			| --twitter-source.consumerSecret <secret>
			| --twitter-source.token <token>
			| --twitter-source.tokenSecret <tokenSecret>
			| --bootstrap.servers <server1[,server2,...]>
			| --topic.id <id>
			| --tweet-count <count>
			| --query-count <count>
			| """.stripMargin
    )
}
