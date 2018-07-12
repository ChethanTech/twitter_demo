package com.weefin.twitterdemo.utils.twitter.flatmap

import com.danielasfregola.twitter4s.entities.{HashTag, Tweet}
import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.util.Collector

object FlatMapExtractHashtags extends RichFlatMapFunction[Option[Tweet], Seq[HashTag]] {
	override def flatMap(tweet: Option[Tweet], out: Collector[Seq[HashTag]]): Unit = {
		def extractHashtags = (tweet: Option[Tweet]) => tweet
			.flatMap(tweet => tweet.extended_entities.orElse(tweet.entities))
			.foreach(entities => out.collect(entities.hashtags))
		
		if (tweet.exists(_.is_quote_status)) {
			extractHashtags(tweet.flatMap(_.quoted_status))
		} else if (tweet.exists(_.retweeted)) {
			extractHashtags(tweet.flatMap(_.retweeted_status))
		} else {
			extractHashtags(tweet)
		}
	}
}
