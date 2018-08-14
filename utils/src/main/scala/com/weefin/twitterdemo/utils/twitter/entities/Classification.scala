package com.weefin.twitterdemo.utils.twitter.entities

import com.weefin.twitterdemo.utils.twitter.entities.Classification.Label

import scala.util.Try

case class Classification(label: Label.Value, weight: Option[Float] = None)

object Classification {
  def fromWord(w: String): Classification = Classification(getLabel(w))

  def fromWords(ws: Seq[String]): Seq[Classification] =
    classify(ws)
      .map(t => Classification(t._1, Some(t._2)))
      .toSeq

  private def classify(ws: Seq[String]): Map[Label.Value, Float] =
    ws.map(getLabel(_))
      .groupBy(identity)
      .mapValues(_.size.toFloat / ws.size)

  def serializableMap(ws: Seq[String]): Map[String, Float] =
    classify(ws)
      .map(t => t._1.toString -> t._2)

  def getMainClassification(cs: Seq[Classification]): Option[Classification] =
    Try(cs.maxBy(_.weight)).toOption

  def getMainDefinedClassification(
    cs: Seq[Classification]
  ): Option[Classification] =
    getMainClassification(cs.filterNot(_.label == Label.Other))

  private val terms = Map(
    /* A */
    "ai" -> Label.AI,
    "airdrop" -> Label.Blockchain,
    "airdrops" -> Label.Blockchain,
    "altcoin" -> Label.Blockchain,
    "analytics" -> Label.DataScience,
    "artificialintelligence" -> Label.AI,
    /* B */
    "bank" -> Label.Finance,
    "banking" -> Label.Finance,
    "banks" -> Label.Finance,
    "bigdata" -> Label.BigData,
    "bitcoin" -> Label.Blockchain,
    "blockchain" -> Label.Blockchain,
    "btc" -> Label.Blockchain,
    /* C */
    "cloud" -> Label.Tech,
    "coin" -> Label.Blockchain,
    "coindrop" -> Label.Blockchain,
    "crypto" -> Label.Blockchain,
    "cryptocurrency" -> Label.Blockchain,
    "cryptopos" -> Label.Blockchain,
    /* D */
    "dataanalytics" -> Label.DataScience,
    "datascience" -> Label.DataScience,
    "dataviz" -> Label.DataScience,
    "decentralized" -> Label.Blockchain,
    "deeplearning" -> Label.AI,
    "devops" -> Label.Tech,
    "dgd" -> Label.Blockchain,
    "digital" -> Label.Tech,
    "digitalization" -> Label.Tech,
    "digitization" -> Label.Tech,
    "dl" -> Label.AI,
    /* E */
    "ecology" -> Label.Ecology,
    "eth" -> Label.Blockchain,
    "ethereum" -> Label.Blockchain,
    /* F */
    "finance" -> Label.Finance,
    "finserv" -> Label.Finance,
    "fintech" -> Label.Finance,
    "flink" -> Label.BigData,
    "freeico" -> Label.Blockchain,
    /* G */
    "green" -> Label.Ecology,
    "greentech" -> Label.Ecology,
    /* H */
    "hadoop" -> Label.BigData,
    /* I */
    "ico" -> Label.Blockchain,
    "iiot" -> Label.Tech,
    "invest" -> Label.Finance,
    "internetofthings" -> Label.Tech,
    "iot" -> Label.Tech,
    /* L */
    "litecoin" -> Label.Blockchain,
    "ltc" -> Label.Blockchain,
    /* M */
    "machinelearning" -> Label.AI,
    "ml" -> Label.AI,
    /* N */
    "neo" -> Label.Blockchain,
    /* P */
    "paas" -> Label.Tech,
    "preico" -> Label.Blockchain,
    "profits" -> Label.Finance,
    /* R */
    "regtech" -> Label.Finance,
    "ripple" -> Label.Blockchain,
    "robotics" -> Label.Tech,
    "rockchain" -> Label.Blockchain,
    /* S */
    "saas" -> Label.Tech,
    "smartcontract" -> Label.Blockchain,
    "spark" -> Label.BigData,
    /* T */
    "tech" -> Label.Tech,
    "technology" -> Label.Tech,
    "token" -> Label.Blockchain,
    "tokens" -> Label.Blockchain,
    /* V */
    "virtualassistants" -> Label.AI,
    "visualthinking" -> Label.DataScience,
    /* X */
    "xrp" -> Label.Blockchain
  ).withDefaultValue(Label.Other)

  private def getLabel(w: String) = terms(w.toLowerCase)

  object Label extends Enumeration {
    val AI, BigData, Blockchain, DataScience, Ecology, Finance, Other, Tech =
      Value
  }
}
