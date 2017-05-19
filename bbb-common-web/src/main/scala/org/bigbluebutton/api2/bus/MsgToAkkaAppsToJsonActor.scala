package org.bigbluebutton.api2.bus

import akka.actor.{Actor, ActorLogging, Props}
import org.bigbluebutton.api2.SystemConfiguration
import org.bigbluebutton.common2.messages.BbbCoreWithEvelopeMsg
import org.bigbluebutton.common2.util.JsonUtil

object MsgToAkkaAppsToJsonActor {
  def props(jsonMsgToAkkaAppsBus: JsonMsgToAkkaAppsBus): Props =
    Props(classOf[MsgToAkkaAppsToJsonActor], jsonMsgToAkkaAppsBus)

}

class MsgToAkkaAppsToJsonActor(jsonMsgToAkkaAppsBus: JsonMsgToAkkaAppsBus)
  extends Actor with ActorLogging with SystemConfiguration {

  def receive = {
    case msg: BbbCoreWithEvelopeMsg => handle(msg)
  }

  def handle(msg: BbbCoreWithEvelopeMsg): Unit = {
    val json = JsonUtil.toJson(msg)
    val jsonMsg = JsonMsgToSendToAkkaApps(toAkkaAppsRedisChannel, json)
    jsonMsgToAkkaAppsBus.publish(JsonMsgToAkkaAppsBusMsg(toAkkaAppsJsonChannel, jsonMsg))
  }

}
