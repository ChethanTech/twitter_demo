package com.weefin.twitterdemo.utils.twitter.source

import java.util.Properties

import com.twitter.hbc.core.endpoint.RawEndpoint
import org.apache.flink.streaming.connectors.twitter.TwitterSource

object RawTwitterSource {
	def apply(uri: String, httpMethod: String, consumerKey: String, consumerSecret: String, token: String,
	          tokenSecret: String): TwitterSource = {
		val props = new Properties
		props.setProperty("uri", uri)
		props.setProperty("http-method", httpMethod)
		props.setProperty("twitter-source.consumerKey", consumerKey)
		props.setProperty("twitter-source.consumerSecret", consumerSecret)
		props.setProperty("twitter-source.token", token)
		props.setProperty("twitter-source.tokenSecret", tokenSecret)
		val source = new TwitterSource(props)
		source.setCustomEndpointInitializer(new TweetFilter(uri, httpMethod))
		source
	}
	
	private class TweetFilter(uri: String, httpMethod: String)
		extends TwitterSource.EndpointInitializer with Serializable {
		override def createEndpoint: RawEndpoint = new RawEndpoint(uri, httpMethod)
	}
	
}