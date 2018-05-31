package com.finaxys

import org.apache.flink.api.scala.ExecutionEnvironment

object HelloWorld {
	private val data = Seq("Hello", "world", "!")
	
	def main(args: Array[String]) {
		val env = ExecutionEnvironment.getExecutionEnvironment
		val strings = env.fromCollection(data)
		strings.print
		print(strings.count)
	}
}
