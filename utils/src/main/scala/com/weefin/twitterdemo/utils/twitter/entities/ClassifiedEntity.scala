package com.weefin.twitterdemo.utils.twitter.entities

import scala.util.Try

case class ClassifiedEntity[T](
  entity: T,
  classification: Map[WordClassification.Value, Float] = Map.empty
) {
  lazy val mainClass: Option[WordClassification.Value] = Try(
    classification
      .filterNot(_._1 == WordClassification.Other)
      .maxBy(_._2)
      ._1
  ).toOption
}
