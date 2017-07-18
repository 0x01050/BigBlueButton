package org.bigbluebutton.core.apps.layout

import org.bigbluebutton.common2.msgs._
import org.bigbluebutton.core2.MeetingStatus2x
import org.bigbluebutton.core.OutMessageGateway
import org.bigbluebutton.core.models.Layouts
import org.bigbluebutton.core.running.LiveMeeting

trait GetCurrentLayoutReqMsgHdlr {
  this: LayoutApp2x =>

  val liveMeeting: LiveMeeting
  val outGW: OutMessageGateway

  def handleGetCurrentLayoutReqMsg(msg: GetCurrentLayoutReqMsg): Unit = {

    def broadcastEvent(msg: GetCurrentLayoutReqMsg): Unit = {

      val routing = Routing.addMsgToClientRouting(MessageTypes.DIRECT, liveMeeting.props.meetingProp.intId, msg.header.userId)
      val envelope = BbbCoreEnvelope(GetCurrentLayoutRespMsg.NAME, routing)
      val header = BbbClientMsgHeader(GetCurrentLayoutRespMsg.NAME, liveMeeting.props.meetingProp.intId, msg.header.userId)

      val body = GetCurrentLayoutRespMsgBody(
        Layouts.getCurrentLayout(),
        MeetingStatus2x.getPermissions(liveMeeting.status).lockedLayout,
        Layouts.getLayoutSetter()
      )
      val event = GetCurrentLayoutRespMsg(header, body)
      val msgEvent = BbbCommonEnvCoreMsg(envelope, event)

      outGW.send(msgEvent)
    }

    broadcastEvent(msg)
  }
}
