package com.weefin.twitterdemo.utils.twitter.flatmap

import java.util.Date

import com.danielasfregola.twitter4s.entities.{HashTag, Tweet}
import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.util.Collector

case class DatedHashtag(created_at: Date, hashtag: HashTag)

//object FlatMapExtractHashtags extends RichFlatMapFunction[Option[Tweet], Seq[HashTag]] {
//override def flatMap(tweet: Option[Tweet], out: Collector[Seq[HashTag]]): Unit = {
//	def extractHashtags = (tweet: Option[Tweet]) => tweet
//		.flatMap(tweet => tweet.extended_entities.orElse(tweet.entities))
//		.foreach(entities => out.collect(entities.hashtags))
//
//	if (tweet.exists(_.is_quote_status)) {
//		extractHashtags(tweet.flatMap(_.quoted_status))
//	} else if (tweet.exists(_.retweeted)) {
//		extractHashtags(tweet.flatMap(_.retweeted_status))
//	} else {
//		extractHashtags(tweet)
//	}
//}
//}
object FlatMapExtractDatedHashtags
    extends RichFlatMapFunction[Option[Tweet], Seq[DatedHashtag]] {

  override def flatMap(tweet: Option[Tweet],
                       out: Collector[Seq[DatedHashtag]]): Unit = {

    def extractDatedHashtags(
      tweet: Option[Tweet]
    ): Option[Seq[DatedHashtag]] = {
      var datedHashtags = Seq.empty[DatedHashtag]
      tweet
        .flatMap(tweet => tweet.extended_entities.orElse(tweet.entities))
        .foreach(
          _.hashtags
            .foreach(datedHashtags :+= DatedHashtag(tweet.get.created_at, _))
        )
      if (datedHashtags.nonEmpty) Some(datedHashtags) else None
    }

    if (tweet.exists(_.is_quote_status)) {
      extractDatedHashtags(tweet.flatMap(_.quoted_status))
        .foreach(out.collect(_))
    } else if (tweet.exists(_.retweeted)) {
      extractDatedHashtags(tweet.flatMap(_.retweeted_status))
        .foreach(out.collect(_))
    } else {
      extractDatedHashtags(tweet).foreach(out.collect(_))
    }
  }
}
