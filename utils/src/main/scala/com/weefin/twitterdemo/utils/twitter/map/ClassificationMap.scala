package com.weefin.twitterdemo.utils.twitter.map

import org.apache.flink.api.common.functions.RichMapFunction

import scala.util.Try

abstract class ClassificationMap[I, O](terms: Map[String, (String, Float)])
    extends RichMapFunction[I, O] {
  protected def fromWords(ws: String*): Option[String] =
    Try(
      ws.flatMap(t => terms.get(t.toLowerCase))
        .groupBy(identity)
        .map { case (k, v) => k._1 -> v.map(_._2).sum }
        .maxBy(_._2)
        ._1
    ).toOption
}
