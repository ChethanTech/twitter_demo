package com.weefin.twitterdemo.utils.twitter.source

import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities.{AccessToken, ConsumerToken}
import org.apache.flink.runtime.concurrent.Executors
import org.apache.flink.streaming.api.scala.async.AsyncFunction

import scala.concurrent.ExecutionContext

abstract class AsyncTwitterRequest[T, U](consumerKey: String,
                                         consumerSecret: String,
                                         token: String,
                                         tokenSecret: String)
    extends AsyncFunction[T, U] {

  protected lazy val client = TwitterRestClient(
    ConsumerToken(consumerKey, consumerSecret),
    AccessToken(token, tokenSecret)
  )
  implicit lazy val executor: ExecutionContext =
    ExecutionContext.fromExecutor(Executors.directExecutor)

}
