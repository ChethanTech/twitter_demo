package com.weefin.twitterdemo.utils.twitter.process

import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.util.Collector

class KeyedProcessThrottle[K, I, O](throttle: Long, f: I => O)
    extends KeyedProcessFunction[K, I, O] {
  private lazy val state: ValueState[Boolean] = getRuntimeContext
    .getState(new ValueStateDescriptor[Boolean]("throttling", classOf[Boolean]))

  override def onTimer(timestamp: Long,
                       ctx: KeyedProcessFunction[K, I, O]#OnTimerContext,
                       out: Collector[O]): Unit = state.clear()

  override def processElement(value: I,
                              ctx: KeyedProcessFunction[K, I, O]#Context,
                              out: Collector[O]): Unit = if (!state.value) {
    out.collect(f(value))
    state.update(true)
    ctx.timerService.registerProcessingTimeTimer(
      ctx.timerService.currentProcessingTime() + throttle
    )
  }
}

object KeyedProcessThrottle {
  def apply[K, I, O](throttle: Long, f: I => O) =
    new KeyedProcessThrottle[K, I, O](throttle, f)
}
