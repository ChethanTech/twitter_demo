package com.weefin.twitterdemo.utils.twitter.entities

import scala.util.Try

case class ClassifiedEntity[T](
  entity: T,
  classification: Map[WordClassification.Value, Float],
  confidence: Option[Float] = None
) {
  lazy val mainClass: Option[WordClassification.Value] = Try(
    classification
      .filterNot(_._1 == WordClassification.Other)
      .maxBy(_._2)
      ._1
  ).toOption
}

object ClassifiedEntity {
  def apply[T](entity: T,
               classification: Map[WordClassification.Value, Float],
               confidence: Float): ClassifiedEntity[T] =
    ClassifiedEntity(entity, classification, Some(confidence))
}
