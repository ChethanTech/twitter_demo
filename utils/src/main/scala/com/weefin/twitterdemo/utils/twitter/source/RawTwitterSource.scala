package com.weefin.twitterdemo.utils.twitter.source

import java.util.Properties

import com.twitter.hbc.core.endpoint.RawEndpoint
import org.apache.flink.streaming.connectors.twitter.TwitterSource

object RawTwitterSource {
  def apply(uri: String,
            httpMethod: String,
            consumerKey: String,
            consumerSecret: String,
            token: String,
            tokenSecret: String) =
    new TwitterSource(new Properties {
      setProperty("uri", uri)
      setProperty("http-method", httpMethod)
      setProperty("twitter-source.consumerKey", consumerKey)
      setProperty("twitter-source.consumerSecret", consumerSecret)
      setProperty("twitter-source.token", token)
      setProperty("twitter-source.tokenSecret", tokenSecret)
    }) {
      setCustomEndpointInitializer(
        new TwitterSource.EndpointInitializer with Serializable {
          override def createEndpoint: RawEndpoint =
            new RawEndpoint(uri, httpMethod)
        }
      )
    }
}
