package com.weefin.twitterdemo.utils.twitter

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

trait StreamEnvironment {
	protected lazy val env = StreamExecutionEnvironment.getExecutionEnvironment
}
