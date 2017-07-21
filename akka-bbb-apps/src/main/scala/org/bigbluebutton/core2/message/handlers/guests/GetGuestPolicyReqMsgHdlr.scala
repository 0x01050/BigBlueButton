package org.bigbluebutton.core2.message.handlers.guests

import org.bigbluebutton.common2.msgs._
import org.bigbluebutton.core.OutMessageGateway
import org.bigbluebutton.core.models.{ GuestWaiting, GuestsWaiting, Roles, Users2x }
import org.bigbluebutton.core.running.{ BaseMeetingActor, LiveMeeting }
import org.bigbluebutton.core2.message.senders.{ MsgBuilder, Sender }

trait GetGuestPolicyReqMsgHdlr {
  this: BaseMeetingActor =>

  val liveMeeting: LiveMeeting
  val outGW: OutMessageGateway

  def handleGetGuestPolicyReqMsg(msg: GetGuestPolicyReqMsg): Unit = {
    val event = buildGetGuestPolicyRespMsg(liveMeeting.props.meetingProp.intId, msg.body.requestedBy,
      liveMeeting.guestsWaiting.getGuestPolicy().policy)
    outGW.send(event)
  }

  def buildGetGuestPolicyRespMsg(meetingId: String, userId: String, policy: String): BbbCommonEnvCoreMsg = {
    val routing = Routing.addMsgToClientRouting(MessageTypes.DIRECT, meetingId, userId)
    val envelope = BbbCoreEnvelope(GetGuestPolicyRespMsg.NAME, routing)
    val header = BbbClientMsgHeader(GetGuestPolicyRespMsg.NAME, meetingId, userId)
    val body = GetGuestPolicyRespMsgBody(policy)
    val event = GetGuestPolicyRespMsg(header, body)
    BbbCommonEnvCoreMsg(envelope, event)
  }
}
