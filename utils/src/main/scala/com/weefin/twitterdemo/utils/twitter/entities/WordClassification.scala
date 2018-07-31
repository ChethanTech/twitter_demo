package com.weefin.twitterdemo.utils.twitter.entities

object WordClassification extends Enumeration {
  val AI, BigData, Blockchain, DataScience, Ecology, Finance, Other, Tech =
    Value
  private val terms = Map(
    /* A */
    "ai" -> AI,
    "airdrop" -> Blockchain,
    "airdrops" -> Blockchain,
    "altcoin" -> Blockchain,
    "analytics" -> DataScience,
    "artificialintelligence" -> AI,
    /* B */
    "bank" -> Finance,
    "banking" -> Finance,
    "banks" -> Finance,
    "bigdata" -> BigData,
    "bitcoin" -> Blockchain,
    "blockchain" -> Blockchain,
    "btc" -> Blockchain,
    /* C */
    "cloud" -> Tech,
    "coin" -> Blockchain,
    "coindrop" -> Blockchain,
    "crypto" -> Blockchain,
    "cryptocurrency" -> Blockchain,
    "cryptopos" -> Blockchain,
    /* D */
    "dataanalytics" -> DataScience,
    "datascience" -> DataScience,
    "dataviz" -> DataScience,
    "decentralized" -> Blockchain,
    "deeplearning" -> AI,
    "devops" -> Tech,
    "dgd" -> Blockchain,
    "digital" -> Tech,
    "digitalization" -> Tech,
    "digitization" -> Tech,
    "dl" -> AI,
    /* E */
    "ecology" -> Ecology,
    "eth" -> Blockchain,
    "ethereum" -> Blockchain,
    /* F */
    "finance" -> Finance,
    "finserv" -> Finance,
    "fintech" -> Finance,
    "flink" -> BigData,
    "freeico" -> Blockchain,
    /* G */
    "green" -> Ecology,
    "greentech" -> Ecology,
    /* H */
    "hadoop" -> BigData,
    /* I */
    "ico" -> Blockchain,
    "iiot" -> Tech,
    "invest" -> Finance,
    "internetofthings" -> Tech,
    "iot" -> Tech,
    /* L */
    "litecoin" -> Blockchain,
    "ltc" -> Blockchain,
    /* M */
    "machinelearning" -> AI,
    "ml" -> AI,
    /* N */
    "neo" -> Blockchain,
    /* P */
    "paas" -> Tech,
    "preico" -> Blockchain,
    "profits" -> Finance,
    /* R */
    "regtech" -> Finance,
    "ripple" -> Blockchain,
    "robotics" -> Tech,
    "rockchain" -> Blockchain,
    /* S */
    "saas" -> Tech,
    "smartcontract" -> Blockchain,
    "spark" -> BigData,
    /* T */
    "tech" -> Tech,
    "technology" -> Tech,
    "token" -> Blockchain,
    "tokens" -> Blockchain,
    /* V */
    "virtualassistants" -> AI,
    "visualthinking" -> DataScience,
    /* X */
    "xrp" -> Blockchain
  ).withDefaultValue(Other)

  def apply(word: String) = terms(word.toLowerCase)

  def apply(words: Seq[String]): Map[Value, Float] =
    words
      .map(this(_))
      .groupBy(identity)
      .mapValues(_.size.toFloat)
      .mapValues(_ / words.size)
      .withDefaultValue(0)
}
