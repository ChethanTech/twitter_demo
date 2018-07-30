package com.weefin.twitterdemo.utils.twitter.flatmap

import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.util.Collector

class FlatMapExplodeSeq[T] extends RichFlatMapFunction[Seq[T], T] {
  override def flatMap(sequence: Seq[T], out: Collector[T]): Unit =
    sequence.foreach(out.collect(_))
}

object FlatMapExplodeSeq {
  def apply[T]: FlatMapExplodeSeq[T] = new FlatMapExplodeSeq[T]
}
