package com.weefin.twitterdemo.utils.twitter.entities

case class SimpleStatusClassification(status: SimpleStatus, weights: Map[SimpleStatusClassification.Value, Float]) {
	def this(status: SimpleStatus) = this(status,
		status.hashtags.map(word => SimpleStatusClassification.terms(word.toLowerCase)).groupBy(identity)
			.mapValues(_.size.toFloat).mapValues(_ / status.hashtags.size).withDefaultValue(0))
}

object SimpleStatusClassification extends Enumeration {
	val AI, BigData, Blockchain, DataScience, Ecology, Finance, Other, Tech = Value
	private val terms = Map("ai" -> AI,
		"artificialintelligence" -> AI,
		"ml" -> AI,
		"machinelearning" -> AI,
		"dl" -> AI,
		"deeplearning" -> AI,
		"virtualassistants" -> AI,
		"bigdata" -> BigData,
		"hadoop" -> BigData,
		"spark" -> BigData,
		"flink" -> BigData,
		"blockchain" -> Blockchain,
		"cryptocurrency" -> Blockchain,
		"smartcontract" -> Blockchain,
		"bitcoin" -> Blockchain,
		"btc" -> Blockchain,
		"ethereum" -> Blockchain,
		"eth" -> Blockchain,
		"altcoin" -> Blockchain,
		"crypto" -> Blockchain,
		"ico" -> Blockchain,
		"preico" -> Blockchain,
		"tokens" -> Blockchain,
		"cryptopos" -> Blockchain,
		"datascience" -> DataScience,
		"dataanalytics" -> DataScience,
		"analytics" -> DataScience,
		"dataviz" -> DataScience,
		"visualthinking" -> DataScience,
		"ecology" -> Ecology,
		"green" -> Ecology,
		"greentech" -> Ecology,
		"finance" -> Finance,
		"invest" -> Finance,
		"profits" -> Finance,
		"fintech" -> Finance,
		"finserv" -> Finance,
		"regtech" -> Finance,
		"bank" -> Finance,
		"banks" -> Finance,
		"banking" -> Finance,
		"tech" -> Tech,
		"technology" -> Tech,
		"digital" -> Tech,
		"digitization" -> Tech,
		"iot" -> Tech,
		"internetofthings" -> Tech,
		"iiot" -> Tech,
		"cloud" -> Tech,
		"saas" -> Tech,
		"paas" -> Tech,
		"devops" -> Tech,
		"robotics" -> Tech).withDefaultValue(Other)
	
	def apply(status: SimpleStatus) = new SimpleStatusClassification(status)
}