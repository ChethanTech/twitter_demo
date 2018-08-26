package com.weefin.twitterdemo.utils.twitter.source
import com.danielasfregola.twitter4s.entities.Tweet
import com.typesafe.scalalogging.LazyLogging
import org.apache.flink.streaming.api.scala.async.ResultFuture

import scala.concurrent.Future
import scala.util.{Failure, Success}

class AsyncTimelineRequest(consumerKey: String,
                           consumerSecret: String,
                           token: String,
                           tokenSecret: String,
                           count: Option[Int] = None)
    extends AsyncTwitterRequest[Long, Seq[Tweet]](
      consumerKey,
      consumerSecret,
      token,
      tokenSecret
    )
    with LazyLogging {

  override def timeout(userId: Long,
                       resultFuture: ResultFuture[Seq[Tweet]]): Unit = {
    logger.warn(s"Get timeline for user id $userId: query timed out")
    resultFuture.complete(Iterable.empty)
  }

  override def asyncInvoke(userId: Long,
                           resultFuture: ResultFuture[Seq[Tweet]]): Unit = {
    def getTweets(ts: Seq[Tweet]): Future[Seq[Tweet]] =
      client
        .userTimelineForUserId(
          user_id = userId,
          max_id = ts.lastOption.map(_.id)
        )
        .map(_.data)
        .flatMap(t => {
          val nts = ts.dropRight(1) ++ t
          if (t.size <= 1 || count.exists(nts.size >= _))
            Future.successful(nts.take(count.getOrElse(nts.size)))
          else getTweets(nts)
        })

    getTweets(Seq.empty).onComplete {
      case Success(timeline) =>
        logger.info(
          s"Get timeline for user id $userId: received the ${timeline.length} most recent Tweets"
        )
        resultFuture.complete(Iterable(timeline))
      case Failure(throwable) =>
        logger.warn(
          s"Get timeline for user id $userId: received error '${throwable.getMessage}'"
        )
        resultFuture.complete(Iterable.empty)
    }
  }
}
