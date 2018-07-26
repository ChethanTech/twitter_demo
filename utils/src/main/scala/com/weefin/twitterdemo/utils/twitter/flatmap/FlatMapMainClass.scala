package com.weefin.twitterdemo.utils.twitter.flatmap

import com.weefin.twitterdemo.utils.twitter.entities.{SimpleStatus, WordClassification}
import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.util.Collector

object FlatMapMainClass
	extends RichFlatMapFunction[(SimpleStatus, Map[WordClassification.Value, Float]), (SimpleStatus,
		WordClassification.Value)] {
	override def flatMap(value: (SimpleStatus, Map[WordClassification.Value, Float]),
		out: Collector[(SimpleStatus, WordClassification.Value)]): Unit = {
		val classification = value._2.filterNot(_._1 == WordClassification.Other)
		if (classification.nonEmpty) out.collect((value._1, classification.maxBy(_._2)._1))
	}
}
