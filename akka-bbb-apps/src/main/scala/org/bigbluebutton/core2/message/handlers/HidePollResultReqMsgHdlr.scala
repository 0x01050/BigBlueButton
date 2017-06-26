package org.bigbluebutton.core2.message.handlers

import org.bigbluebutton.common2.messages._
import org.bigbluebutton.common2.messages.polls.{ HidePollResultReqMsg, PollHideResultEvtMsg, PollHideResultEvtMsgBody }
import org.bigbluebutton.core.OutMessageGateway
import org.bigbluebutton.core.models.Polls
import org.bigbluebutton.core.running.MeetingActor

trait HidePollResultReqMsgHdlr {
  this: MeetingActor =>

  val outGW: OutMessageGateway

  def handleHidePollResultReqMsg(msg: HidePollResultReqMsg): Unit = {

    def broadcastEvent(msg: HidePollResultReqMsg, hiddenPollId: String): Unit = {
      val routing = Routing.addMsgToClientRouting(MessageTypes.BROADCAST_TO_MEETING, props.meetingProp.intId, msg.header.userId)
      val envelope = BbbCoreEnvelope(PollHideResultEvtMsg.NAME, routing)
      val header = BbbClientMsgHeader(PollHideResultEvtMsg.NAME, props.meetingProp.intId, msg.header.userId)

      val body = PollHideResultEvtMsgBody(msg.header.userId, hiddenPollId)
      val event = PollHideResultEvtMsg(header, body)
      val msgEvent = BbbCommonEnvCoreMsg(envelope, event)
      outGW.send(msgEvent)
    }

    for {
      hiddenPollId <- Polls.handleHidePollResultReqMsg(msg.header.userId, msg.body.pollId, liveMeeting)
    } yield {
      broadcastEvent(msg, hiddenPollId)
    }
  }
}
