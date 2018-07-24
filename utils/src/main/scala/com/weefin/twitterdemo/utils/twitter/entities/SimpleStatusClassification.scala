package com.weefin.twitterdemo.utils.twitter.entities

case class SimpleStatusClassification(status: SimpleStatus, weights: Map[SimpleStatusClassification.Value, Float]) {
	def this(status: SimpleStatus) = this(status,
		status.hashtags.map(word => SimpleStatusClassification.terms(word.toLowerCase)).groupBy(identity)
			.mapValues(_.size.toFloat).mapValues(_ / status.hashtags.size).withDefaultValue(0))
}

object SimpleStatusClassification extends Enumeration {
	val AI, BigData, Blockchain, DataScience, Ecology, Finance, Other, Tech = Value
	private val terms = Map(/* A */ "ai" -> AI,
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
		
		/* M */
		"machinelearning" -> AI,
		"ml" -> AI,
		
		/* P */
		"paas" -> Tech,
		"preico" -> Blockchain,
		"profits" -> Finance,
		
		/* R */
		"regtech" -> Finance,
		"robotics" -> Tech,
		"rockchain" -> Blockchain,
		
		/* S */
		"saas" -> Tech,
		"smartcontract" -> Blockchain,
		"spark" -> BigData,
		
		/* T */
		"tech" -> Tech,
		"technology" -> Tech,
		"tokens" -> Blockchain,
		
		/* V */
		"virtualassistants" -> AI,
		"visualthinking" -> DataScience).withDefaultValue(Other)
	
	def apply(status: SimpleStatus) = new SimpleStatusClassification(status)
}