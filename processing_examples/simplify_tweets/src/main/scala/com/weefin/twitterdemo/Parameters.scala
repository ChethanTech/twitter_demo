package com.weefin.twitterdemo

import org.apache.flink.api.java.utils.ParameterTool

import scala.util.Try

case class Parameters(producerBootstrapServers: String,
                      producerTopicId: String,
                      consumerBootstrapServers: String,
                      consumerGroupId: String,
                      consumerTopicId: String)

object Parameters {
  private val defaultBootstrapServers = "localhost:9092"

  def apply(args: Array[String]): Parameters = {
    val params = ParameterTool.fromArgs(args)
    Try(
      new Parameters(
        params.get("producer.bootstrap.servers", defaultBootstrapServers),
        params.getRequired("producer.topic.id"),
        params.get("consumer.bootstrap.servers", defaultBootstrapServers),
        params.getRequired("consumer.group.id"),
        params.getRequired("consumer.topic.id")
      )
    ).getOrElse(throwInvalidArgs)
  }

  private def throwInvalidArgs = throw new IllegalArgumentException(
    """Invalid arguments. Usage: simplify_statuses
			| --consumer.bootstrap.servers <server1[,server2,...]>
			| --consumer.group.id <id>
			| --consumer.topic.id <id>
			| --producer.bootstrap.servers <server1[,server2,...]>
			| --producer.topic.id <id>
			| """.stripMargin
  )
}
