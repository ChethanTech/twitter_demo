package com.weefin.twitterdemo.utils.twitter.flatmap

import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.util.Collector

import scala.annotation.tailrec

class FlatMapDefinedOption[T, U] extends RichFlatMapFunction[T, U] {
	override def flatMap(value: T, out: Collector[U]): Unit = {
		@tailrec def go(value: Any): Unit = value match {
			case o: Some[Any] => go(o.get)
			case None =>
			case u: U => out.collect(u)
			case _ => throw new IllegalArgumentException("Invalid types: T must wrap U in an arbitrary number of Options")
		}
		
		go(value)
	}
}

object FlatMapDefinedOption {
	def apply[T, U] = new FlatMapDefinedOption[T, U]
}

