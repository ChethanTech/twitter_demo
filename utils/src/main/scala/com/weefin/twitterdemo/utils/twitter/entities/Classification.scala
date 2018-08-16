package com.weefin.twitterdemo.utils.twitter.entities

import scala.util.Try

object Classification {

  def merge(ms: Map[Label.Value, Float]*): Map[Label.Value, Float] =
    ms.reduceOption { (a, m) =>
        a ++ m.map {
          case (k, v) => k -> (v + a.getOrElse(k, 0F))
        }
      }
      .getOrElse(Map.empty)
      .mapValues(_ / ms.length)

  def classify(ws: String*): Map[Label.Value, Float] =
    ws.map(getLabel(_))
      .groupBy(identity)
      .mapValues(_.size.toFloat / ws.size)

  def stringify(m: Map[Label.Value, Float]): Map[String, Float] = m.map {
    case (k, v) => k.toString -> v
  }

  def getMainLabel(m: Map[Label.Value, Float]): Option[Label.Value] =
    Try(m.maxBy(_._2)._1).toOption

  def getMainDefinedLabel(m: Map[Label.Value, Float]): Option[Label.Value] =
    getMainLabel(m.filterNot(_._1 == Label.Other))

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
