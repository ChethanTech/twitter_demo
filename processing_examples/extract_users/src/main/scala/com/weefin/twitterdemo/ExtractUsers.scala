package com.weefin.twitterdemo

import com.danielasfregola.twitter4s.entities.Tweet
import com.typesafe.scalalogging.LazyLogging
import com.weefin.twitterdemo.utils.twitter.entities.{SimpleStatus, SimpleUser}
import com.weefin.twitterdemo.utils.twitter.sink.KafkaJsonProducer
import com.weefin.twitterdemo.utils.twitter.source.KafkaJsonConsumer
import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.util.Collector

object ExtractUsers extends App with LazyLogging {
	private val jobName = this.getClass.getSimpleName.split("\\$").last
	private val params = Parameters(args)
	private val env = StreamExecutionEnvironment.getExecutionEnvironment
	logger.info(s"$jobName job started")
	env.addSource(consumer).flatMap(FlatMapSimpleUser).keyBy(_.id).process(FilterDuplicates).addSink(producer)
	env.execute
	
	private object FilterDuplicates extends KeyedProcessFunction[Long, SimpleUser, SimpleUser] {
		private lazy val state: ValueState[Boolean] = getRuntimeContext
			.getState(new ValueStateDescriptor[Boolean]("throttle", classOf[Boolean]))
		
		override def onTimer(timestamp: Long,
			ctx: KeyedProcessFunction[Long, SimpleUser, SimpleUser]#OnTimerContext,
			out: Collector[SimpleUser]): Unit = state.clear()
		
		override def processElement(value: SimpleUser,
			ctx: KeyedProcessFunction[Long, SimpleUser, SimpleUser]#Context,
			out: Collector[SimpleUser]): Unit = if (!state.value) {
			out.collect(value)
			state.update(true)
			ctx.timerService
				.registerEventTimeTimer(ctx.timerService.currentProcessingTime() + Time.minutes(10).toMilliseconds)
		}
	}
	
	private object FlatMapSimpleUser extends RichFlatMapFunction[Option[Tweet], SimpleUser] {
		override def flatMap(value: Option[Tweet], out: Collector[SimpleUser]): Unit = value
			.foreach(SimpleStatus(_).user.foreach(out.collect(_)))
	}
	
	private def consumer = KafkaJsonConsumer[Tweet](params.consumerBootstrapServers,
		params.consumerTopicId,
		params.consumerGroupId)
	
	private def producer = KafkaJsonProducer[SimpleUser](params.producerBootstrapServers, params.producerTopicId)
}
