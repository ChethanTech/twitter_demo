package com.weefin.twitterdemo

import org.apache.flink.api.java.utils.ParameterTool

import scala.util.Try

case class Parameters(producerBootstrapServers: String,
                      consumerBootstrapServers: String,
                      consumerGroupId: String,
                      consumerTopicId: String,
                      producerTopicId: String,
                      whiteList: Set[String],
                      blackList: Set[String])

object Parameters {
  private val defaultBootstrapServers = "localhost:9092"
  private val defaultList = ""

  def apply(args: Array[String]): Parameters = {
    val params = ParameterTool.fromArgs(args)
    Try(
      new Parameters(
        params.get("producer.bootstrap.servers", defaultBootstrapServers),
        params.get("consumer.bootstrap.servers", defaultBootstrapServers),
        params.getRequired("consumer.group.id"),
        params.getRequired("consumer.topic.id"),
        params.getRequired("producer.topic.id"),
        asStringSet(params.get("white-list", defaultList)),
        asStringSet(params.get("black-list", defaultList))
      )
    ).getOrElse(throwInvalidArgs)
  }

  private def asStringSet =
    (list: String) =>
      list.split(",").map(_.trim.toLowerCase).filter(_.nonEmpty).toSet

  private def throwInvalidArgs =
    throw new IllegalArgumentException(
      """Invalid arguments. Usage: extract_hashtags
			| --consumer.bootstrap.servers <server1[,server2,...]>
			| --consumer.group.id <id>
			| --consumer.topic.id <id>
			| --producer.bootstrap.servers <server1[,server2,...]>
			| --producer.topic.id <id>
			| --black-list <word1[,word2,...]>
			| --white-list <word1[,word2,...]>
			| """.stripMargin
    )
}
