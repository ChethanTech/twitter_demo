package com.weefin.twitterdemo.utils.twitter.map

import com.weefin.twitterdemo.utils.twitter.entities.{ClassifiedEntity, SimpleStatus, WordClassification}
import org.apache.flink.api.common.functions.RichMapFunction

object MapClassifyByHashtags extends RichMapFunction[SimpleStatus, ClassifiedEntity[SimpleStatus]] {
	override def map(status: SimpleStatus) = ClassifiedEntity(status,
		status.hashtags.map(word => WordClassification.get(word.toLowerCase)).groupBy(identity)
			.mapValues(_.size.toFloat).mapValues(_ / status.hashtags.size).withDefaultValue(0))
}
