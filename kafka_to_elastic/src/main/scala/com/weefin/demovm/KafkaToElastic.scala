package com.weefin.demovm

import java.net.{InetAddress, InetSocketAddress}
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

object KafkaToElastic {
	val logger: Logger = LoggerFactory.getLogger(getClass)
	
	def main(args: Array[String]) {
		val params = ParameterTool.fromArgs(args)
		if (!validateArguments(params)) {
			logger
				.error("Invalid arguments. Usage: KafkaToElastic --bootstrap.servers <servers> --group.id <id> " +
					"--topic.id <id> --cluster.name <name> --index.name <name> --type.name <name>")
			return
		}
		val env = StreamExecutionEnvironment.getExecutionEnvironment
		val properties = new Properties()
		properties.setProperty("bootstrap.servers", params.get("bootstrap.servers"))
		properties.setProperty("group.id", params.get("group.id"))
		val consumer = new FlinkKafkaConsumer011[String](params.get("topic.id"), new SimpleStringSchema(), properties)
		val config = Map("cluster.name" -> params.get("cluster.name"), "bulk.flush.max.actions" -> "1")
		val transportAddresses = Seq(new InetSocketAddress(InetAddress.getByName("localhost"), 9300))
		env.addSource(consumer)
			.addSink(new ElasticsearchSink(config, transportAddresses,
				new RawSinkFunction(params.get("index.name"), params.get("type.name"))))
		env.execute("Kafka topic to Elasticsearch")
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
	
	private def validateArguments(params: ParameterTool) = {
		params.has("bootstrap.servers") && params.has("topic.id") && params.has("group.id") &&
			params.has("cluster.name") && params.has("index.name") && params.has("type.name")
	}
}
