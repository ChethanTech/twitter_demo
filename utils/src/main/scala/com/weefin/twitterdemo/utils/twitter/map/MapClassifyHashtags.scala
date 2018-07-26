package com.weefin.twitterdemo.utils.twitter.map

import com.weefin.twitterdemo.utils.twitter.entities.{SimpleStatus, WordClassification}
import org.apache.flink.api.common.functions.RichMapFunction

object MapClassifyHashtags extends RichMapFunction[SimpleStatus, (SimpleStatus, Map[WordClassification.Value, Float])] {
	override def map(status: SimpleStatus) = (status, status.hashtags
		.map(word => WordClassification.get(word.toLowerCase)).groupBy(identity).mapValues(_.size.toFloat)
		.mapValues(_ / status.hashtags.size).withDefaultValue(0))
}
