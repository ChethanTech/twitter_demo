package com.weefin.twitterdemo

import java.net.InetSocketAddress
import java.util.Properties

import org.apache.flink.api.common.functions.RuntimeContext
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.connectors.elasticsearch.{ElasticsearchSinkFunction, RequestIndexer}
import org.apache.flink.streaming.connectors.elasticsearch5.ElasticsearchSink
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011
import org.elasticsearch.client.Requests
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConversions.{mapAsJavaMap, seqAsJavaList}

object KafkaToElastic extends App {
	val logger: Logger = LoggerFactory.getLogger(getClass)
	val defaultClusterName = "elasticsearch"
	val defaultNodes = "localhost:9300"
	val defaultBootstrapServers = "localhost:9092"
	val params = ParameterTool.fromArgs(args)
	if (!validateArguments) {
		logErrorInvalidArguments()
		sys.exit(1)
	}
	val env = StreamExecutionEnvironment.getExecutionEnvironment
	val source = getConsumer
	val sink = getElasticsearchSink
	env.addSource(source).addSink(sink)
	env.execute("Kafka topic to Elasticsearch")
	
	private def logErrorInvalidArguments(): Unit = logger
		.error("Invalid arguments. Usage: KafkaToElastic --bootstrap.servers <server1[,server2,...]> --group.id" +
			" <id> --topic.id <id> --nodes <node1[,node2,...]> --cluster.name <name> --index.name <name>" +
			" --type.name <name>")
	
	private def validateArguments = params.has("topic.id") && params.has("group.id") && params.has("index.name") &&
		params.has("type.name")
	
	private def getConsumer = {
		val properties = new Properties()
		properties.setProperty("bootstrap.servers", params.get("bootstrap.servers", defaultBootstrapServers))
		properties.setProperty("group.id", params.get("group.id"))
		new FlinkKafkaConsumer011[String](params.get("topic.id"), new SimpleStringSchema(), properties)
	}
	
	private def getElasticsearchSink = {
		val userConfig = Map("cluster.name" -> params.get("cluster.name", defaultClusterName),
			"bulk.flush.max.actions" -> "1")
		val transportAddresses = try {
			Seq(params.get("nodes", defaultNodes).replaceAll("\\s", "").split(",").map(_.split(":"))
				.map(parts => new InetSocketAddress(parts(0), parts(1).toInt)): _*)
		} catch {
			case e@(_: ArrayIndexOutOfBoundsException | _: NumberFormatException) => throw new
					IllegalArgumentException(
				"Invalid Elasticsearch nodes. Usage: --nodes <host1:port1[,host2:port2,...]>", e)
		}
		new ElasticsearchSink(userConfig, transportAddresses,
			new RawSinkFunction(params.get("index.name"), params.get("type.name")))
	}
	
	private class RawSinkFunction(indexName: String, typeName: String) extends ElasticsearchSinkFunction[String] {
		override def process(element: String, context: RuntimeContext, indexer: RequestIndexer): Unit = {
			try {
				indexer.add(Requests.indexRequest.index(indexName).`type`(typeName).source(element))
			} catch {
				case e: Exception => logger.warn(e.getMessage)
			}
		}
	}
	
}
