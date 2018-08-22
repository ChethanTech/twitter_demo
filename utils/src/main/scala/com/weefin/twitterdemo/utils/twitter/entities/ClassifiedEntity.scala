package com.weefin.twitterdemo.utils.twitter.entities

case class ClassifiedEntity[T](entity: T,
                               classification: Option[String] = None,
                               confidence: Option[Float] = None)
