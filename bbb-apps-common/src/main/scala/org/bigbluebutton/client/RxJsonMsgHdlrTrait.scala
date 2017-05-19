package org.bigbluebutton.client

import org.bigbluebutton.client.bus.JsonMsgFromAkkaApps
import org.bigbluebutton.common2.util.JsonUtil
import org.bigbluebutton.common2.messages.BbbCoreWithEvelopeMsg

trait RxJsonMsgHdlrTrait {

  def handleReceivedJsonMessage(msg: JsonMsgFromAkkaApps): Unit = {
    val serverMsg = JsonUtil.fromJson[BbbCoreWithEvelopeMsg](msg.data)

  }
}
