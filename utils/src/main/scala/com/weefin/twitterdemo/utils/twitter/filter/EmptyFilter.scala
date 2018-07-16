package com.weefin.twitterdemo.utils.twitter.filter

import org.apache.flink.api.common.functions.FilterFunction

abstract class EmptyFilter[T] extends FilterFunction[T] {
	override def filter(value: T) = {
		action(value)
		true
	}
	
	def action(value: T): Any
}
