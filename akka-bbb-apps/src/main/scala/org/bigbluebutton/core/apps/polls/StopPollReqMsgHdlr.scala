package org.bigbluebutton.core.apps.polls

import org.bigbluebutton.common2.msgs._
import org.bigbluebutton.core.OutMessageGateway
import org.bigbluebutton.core.models.Polls
import org.bigbluebutton.core.running.MeetingActor

trait StopPollReqMsgHdlr {
  this: MeetingActor =>

  val outGW: OutMessageGateway

  def broadcastEvent(requesterId: String, stoppedPollId: String): Unit = {
    val routing = Routing.addMsgToClientRouting(MessageTypes.BROADCAST_TO_MEETING, props.meetingProp.intId, requesterId)
    val envelope = BbbCoreEnvelope(PollStoppedEvtMsg.NAME, routing)
    val header = BbbClientMsgHeader(PollStoppedEvtMsg.NAME, props.meetingProp.intId, requesterId)

    val body = PollStoppedEvtMsgBody(requesterId, stoppedPollId)
    val event = PollStoppedEvtMsg(header, body)
    val msgEvent = BbbCommonEnvCoreMsg(envelope, event)
    outGW.send(msgEvent)
  }

  def handleStopPollReqMsg(msg: StopPollReqMsg): Unit = {
    for {
      stoppedPollId <- Polls.handleStopPollReqMsg(msg.header.userId, liveMeeting)
    } yield {
      broadcastEvent(msg.header.userId, stoppedPollId)
    }
  }

  def handleStopPollReqMsg(requesterId: String): Unit = {
    for {
      stoppedPollId <- Polls.handleStopPollReqMsg(requesterId, liveMeeting)
    } yield {
      broadcastEvent(requesterId, stoppedPollId)
    }
  }

}
