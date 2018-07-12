package com.weefin.twitterdemo.utils.twitter.filter

import org.apache.flink.api.common.functions.RichFilterFunction

abstract class GenericFilter[T, U](f: T => U) extends RichFilterFunction[T] {
	
	override def filter(value: T): Boolean = filter2(f(value))
	
	def filter2(value: U): Boolean
}
