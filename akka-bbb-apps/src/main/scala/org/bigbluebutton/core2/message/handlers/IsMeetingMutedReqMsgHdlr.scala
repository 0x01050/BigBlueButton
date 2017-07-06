package org.bigbluebutton.core2.message.handlers

import org.bigbluebutton.common2.msgs._
import org.bigbluebutton.core.OutMessageGateway
import org.bigbluebutton.core.running.MeetingActor
import org.bigbluebutton.core2.MeetingStatus2x

trait IsMeetingMutedReqMsgHdlr {
  this: MeetingActor =>

  val outGW: OutMessageGateway

  def handle(msg: IsMeetingMutedReqMsg) {

    def build(meetingId: String, userId: String, muted: Boolean): BbbCommonEnvCoreMsg = {
      val routing = Routing.addMsgToClientRouting(MessageTypes.DIRECT, meetingId, userId)
      val envelope = BbbCoreEnvelope(IsMeetingMutedRespMsg.NAME, routing)
      val header = BbbClientMsgHeader(IsMeetingMutedRespMsg.NAME, meetingId, userId)

      val body = IsMeetingMutedRespMsgBody(muted)
      val event = IsMeetingMutedRespMsg(header, body)

      BbbCommonEnvCoreMsg(envelope, event)
    }

    val event = build(liveMeeting.props.meetingProp.intId, msg.body.requesterId, MeetingStatus2x.isMeetingMuted(liveMeeting.status))
    outGW.send(event)
  }
}
