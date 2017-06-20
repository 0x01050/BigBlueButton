package org.bigbluebutton.core2.message.handlers.breakoutrooms

import org.bigbluebutton.common2.messages._
import org.bigbluebutton.common2.messages.MessageTypes
import org.bigbluebutton.common2.messages.Routing
import org.bigbluebutton.common2.messages.breakoutrooms._
import org.bigbluebutton.core.OutMessageGateway
import org.bigbluebutton.core.models.BreakoutRooms
import org.bigbluebutton.core.running.MeetingActor

trait BreakoutRoomEndedMsgHdlr {
  this: MeetingActor =>

  val outGW: OutMessageGateway

  def handleBreakoutRoomEndedMsg(msg: BreakoutRoomEndedMsg): Unit = {

    def broadcastEvent(msg: BreakoutRoomEndedMsg): Unit = {

      BreakoutRooms.removeRoom(liveMeeting.breakoutRooms, msg.body.breakoutRoomId)

      val routing = Routing.addMsgToClientRouting(MessageTypes.BROADCAST_TO_MEETING, props.meetingProp.intId, msg.header.userId)
      val envelope = BbbCoreEnvelope(BreakoutRoomEndedEvtMsg.NAME, routing)
      val header = BbbClientMsgHeader(BreakoutRoomEndedEvtMsg.NAME, props.meetingProp.intId, msg.header.userId)

      val body = BreakoutRoomEndedEvtMsgBody(msg.body.meetingId, msg.body.breakoutRoomId)
      val event = BreakoutRoomEndedEvtMsg(header, body)
      val msgEvent = BbbCommonEnvCoreMsg(envelope, event)
      outGW.send(msgEvent)
    }

    broadcastEvent(msg)
  }
}
