package com.weefin.twitterdemo.utils.twitter.flatmap

import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.util.Collector

class FlatMapDefinedOption[T] extends RichFlatMapFunction[Option[T], T] {
	override def flatMap(value: Option[T], out: Collector[T]): Unit = value.foreach(out.collect(_))
}

object FlatMapDefinedOption {
	def apply[T] = new FlatMapDefinedOption[T]
}

