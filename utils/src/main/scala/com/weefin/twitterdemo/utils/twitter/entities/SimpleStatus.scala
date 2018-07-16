package com.weefin.twitterdemo.utils.twitter.entities

import java.util.Date

import com.danielasfregola.twitter4s.entities.{Coordinates, GeoPlace, Tweet}

case class SimpleStatus(created_at: Date,
                        coordinates: Option[Coordinates] = None,
                        favorite_count: Int = 0,
                        hashtags: Seq[String] = Seq.empty,
                        id: Long,
                        lang: Option[String] = None,
                        place: Option[GeoPlace] = None,
                        quoted_status: Option[SimpleStatus] = None,
                        retweet_count: Long = 0,
                        retweeted_status: Option[SimpleStatus] = None,
                        text: String,
                        user: Option[SimpleUser] = None) {
	
	def this(tweet: Tweet) = this(created_at = tweet.created_at,
	                              coordinates = tweet.coordinates,
	                              favorite_count = tweet.favorite_count,
	                              hashtags = tweet.extended_tweet.map(_.entities).getOrElse(tweet.entities)
		                              .map(_.hashtags.map(_.text)).getOrElse(Seq.empty[String]),
	                              id = tweet.id,
	                              lang = tweet.lang,
	                              place = tweet.place,
	                              quoted_status = tweet.quoted_status.map(SimpleStatus(_)),
	                              retweet_count = tweet.retweet_count,
	                              retweeted_status = tweet.retweeted_status.map(SimpleStatus(_)),
	                              text = tweet.extended_tweet.map(_.full_text).getOrElse(tweet.text),
	                              user = tweet.user.map(SimpleUser(_)))
}

object SimpleStatus {
	def apply(tweet: Tweet) = new SimpleStatus(tweet)
}