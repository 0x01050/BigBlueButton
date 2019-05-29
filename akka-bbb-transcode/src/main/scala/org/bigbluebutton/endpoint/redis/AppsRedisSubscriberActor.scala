package org.bigbluebutton.endpoint.redis

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import org.bigbluebutton.common2.redis.RedisConfig
import org.bigbluebutton.common2.bus.IncomingJsonMessageBus
import org.bigbluebutton.common2.redis.{ RedisSubscriber, RedisSubscriberProvider }

import akka.actor.ActorSystem
import akka.actor.Props

object AppsRedisSubscriberActor {

  def props(
      system:              ActorSystem,
      jsonMsgBus:          IncomingJsonMessageBus,
      redisConfig:         RedisConfig,
      channelsToSubscribe: Seq[String],
      patternsToSubscribe: Seq[String],
      forwardMsgToChannel: String
  ): Props =
    Props(
      classOf[AppsRedisSubscriberActor],
      system,
      jsonMsgBus,
      redisConfig,
      channelsToSubscribe,
      patternsToSubscribe,
      forwardMsgToChannel
    ).withDispatcher("akka.redis-subscriber-worker-dispatcher")
}

class AppsRedisSubscriberActor(
    system:              ActorSystem,
    jsonMsgBus:          IncomingJsonMessageBus,
    redisConfig:         RedisConfig,
    channelsToSubscribe: Seq[String],
    patternsToSubscribe: Seq[String],
    forwardMsgToChannel: String
)
  extends RedisSubscriberProvider(
    system,
    "BbbTranscodeAkkaSub",
    channelsToSubscribe,
    patternsToSubscribe,
    jsonMsgBus,
    redisConfig
  ) {

  var lastPongReceivedOn = 0L
  system.scheduler.schedule(10 seconds, 10 seconds)(checkPongMessage())

  def checkPongMessage() {
    val now = System.currentTimeMillis()

    if (lastPongReceivedOn != 0 && (now - lastPongReceivedOn > 30000)) {
      log.error("BBB-Transcode pubsub error!");
    }
  }

  addListener(forwardMsgToChannel)
  subscribe()
}
