package com.weefin.twitterdemo.utils.twitter.filter

object KeywordFilter {
	def apply[T](f: T => String)(whiteList: Set[String] = Set.empty, blackList: Set[String] = Set.empty,
	                             ignoreCase: Boolean = false) = new GenericFilter[T, String](f) {
		val wl = whiteList.map(properCase(_, ignoreCase))
		val bl = blackList.map(properCase(_, ignoreCase))
		
		override def filter2(value: String) = if (wl.nonEmpty) wl.contains(properCase(value, ignoreCase)) else !bl
			.contains(properCase(value, ignoreCase))
	}
	
	private def properCase(keyword: String, ignoreCase: Boolean) = if (ignoreCase) keyword.toLowerCase else keyword
}
