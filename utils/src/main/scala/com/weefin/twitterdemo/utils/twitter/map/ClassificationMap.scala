package com.weefin.twitterdemo.utils.twitter.map

import com.weefin.twitterdemo.utils.twitter.entities.SimpleStatus
import org.apache.flink.api.common.functions.RichMapFunction

import scala.util.Try

abstract class ClassificationMap[I, O](terms: Map[String, (String, Float)])
    extends RichMapFunction[I, O] {
  protected def fromSimpleStatuses(hashtagWeight: Option[Int],
                                   ss: SimpleStatus*): Option[String] = {
    val ws: (Seq[Seq[String]], Seq[Seq[String]]) =
      ss.map(s => (s.allWords, s.allHashtags)).unzip
    val wws: Seq[(String, Int)] = ws._1.flatten.map((_, 1)) ++ ws._2.flatten
      .map((_, hashtagWeight.getOrElse(3)))
    val wls: Seq[(String, Float)] = wws.flatMap {
      case (k, v) => terms.get(k).map(t => t.copy(_2 = t._2 * v))
    }
    Try(
      wls
        .groupBy(identity)
        .map { case (k, v) => k._1 -> v.map(_._2).sum }
        .maxBy(_._2)
        ._1
    ).toOption
  }
}
