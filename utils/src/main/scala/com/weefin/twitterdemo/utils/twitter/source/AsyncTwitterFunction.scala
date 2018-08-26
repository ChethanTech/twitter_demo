package com.weefin.twitterdemo.utils.twitter.source

import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities.{
  AccessToken,
  ConsumerToken,
  Tweet
}
import org.apache.flink.runtime.concurrent.Executors
import org.apache.flink.streaming.api.scala.async.AsyncFunction

import scala.concurrent.{ExecutionContext, Future}

abstract class AsyncTwitterFunction[T, U](consumerKey: String,
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

  protected def getTimeline(userId: Long,
                            tweetCount: Option[Int],
                            queryCount: Option[Int]): Future[Seq[Tweet]] = {
    def getChunk(id: Long, ts: Seq[Tweet], q: Int): Future[Seq[Tweet]] =
      client
        .userTimelineForUserId(user_id = id, max_id = ts.headOption.map(_.id))
        .map(_.data)
        .flatMap(t => {
          val nts = t.reverse ++ ts.drop(1)
          if (t.size <= 1 ||
              queryCount.exists(q >= _) ||
              tweetCount.exists(nts.size >= _))
            Future.successful(nts.takeRight(tweetCount.getOrElse(nts.size)))
          else getChunk(id, nts, q + 1)
        })
    getChunk(userId, Seq.empty, 1)
  }
}
