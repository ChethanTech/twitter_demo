package com.weefin.twitterdemo.utils.twitter.filter

import org.apache.flink.api.common.functions.RichFilterFunction

class KeywordFilter[T](on: T => String)(whiteList: Set[String] = Set.empty, blackList: Set[String] = Set.empty,
                                        ignoreCase: Boolean = false) extends RichFilterFunction[T] {
	
	val wl = whiteList.map(properCase(_))
	val bl = blackList.map(properCase(_))
	
	override def filter(value: T) = if (wl.nonEmpty) wl.contains(properCase(on(value))) else !bl
		.contains(properCase(on(value)))
	
	private def properCase(keyword: String) = if (ignoreCase) keyword.toLowerCase else keyword
}

object KeywordFilter {
	def apply[T](on: T => String)(whiteList: Set[String] = Set.empty, blackList: Set[String] = Set.empty,
	                              ignoreCase: Boolean = false) = new KeywordFilter[T](on)(whiteList, blackList,
		ignoreCase)
}