package com.weefin.twitterdemo.utils.twitter.source

import com.sksamuel.elastic4s.http.{ElasticClient, ElasticProperties}
import org.apache.flink.api.common.functions.AbstractRichFunction
import org.apache.flink.runtime.concurrent.Executors
import org.apache.flink.streaming.api.scala.async.AsyncFunction

import scala.concurrent.ExecutionContext

abstract class AsyncElasticQuery[T, U](uri: String)
    extends AbstractRichFunction
    with AsyncFunction[T, U] {

  protected lazy val client = ElasticClient(ElasticProperties(uri))
  implicit lazy val executor: ExecutionContext =
    ExecutionContext.fromExecutor(Executors.directExecutor)

  override def close(): Unit = {
    super.close()
    client.close
  }
}
