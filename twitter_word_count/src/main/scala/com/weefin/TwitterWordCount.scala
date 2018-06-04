package com.weefin

import java.util.{Properties, StringTokenizer}

import org.apache.flink.api.common.functions.FlatMapFunction
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.{JsonMappingException, JsonNode, ObjectMapper}
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011
import org.apache.flink.util.Collector
import org.slf4j.{Logger, LoggerFactory}

object TwitterWordCount {
	val logger: Logger = LoggerFactory.getLogger(getClass)
	
	def main(args: Array[String]) {
		val params = ParameterTool.fromArgs(args)
		if (!validateArguments(params)) {
			logger
				.error("Invalid arguments. Usage: TwitterWordCount --zookeeper.connect <connect> --bootstrap.servers" +
					" <servers> --group.id <id> --topic.id <id>")
			return
		}
		val env = StreamExecutionEnvironment.getExecutionEnvironment
		val properties = new Properties()
		properties.setProperty("bootstrap.servers", params.get("bootstrap.servers"))
		properties.setProperty("zookeeper.connect", params.get("zookeeper.connect"))
		properties.setProperty("group.id", params.get("group.id"))
		val consumer = new FlinkKafkaConsumer011[String](params.get("topic.id"), new SimpleStringSchema(), properties)
		consumer.setStartFromEarliest()
		env.addSource(consumer).flatMap(new SelectEnglishAndTokenizeFlatMap).keyBy(0).sum(1).print
		env.execute("Twitter word count")
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
				case e: JsonMappingException => logger.info(e.getMessage)
			}
		}
	}
	
	private def validateArguments(params: ParameterTool) = {
		params.has("zookeeper.connect") && params.has("bootstrap.servers") && params.has("topic.id") && params.has("group.id")
	}
}
