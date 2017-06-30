package org.bigbluebutton.core2.message.senders

import org.bigbluebutton.common2.msgs._
import org.bigbluebutton.core.models.GuestWaiting

object MsgBuilder {
  def buildGuestPolicyChangedEvtMsg(meetingId: String, userId: String, policy: String, setBy: String): BbbCommonEnvCoreMsg = {
    val routing = Routing.addMsgToClientRouting(MessageTypes.BROADCAST_TO_MEETING, meetingId, userId)
    val envelope = BbbCoreEnvelope(GuestPolicyChangedEvtMsg.NAME, routing)
    val header = BbbClientMsgHeader(GuestPolicyChangedEvtMsg.NAME, meetingId, userId)

    val body = GuestPolicyChangedEvtMsgBody(policy, setBy)
    val event = GuestPolicyChangedEvtMsg(header, body)

    BbbCommonEnvCoreMsg(envelope, event)
  }

  def buildGuestApprovedEvtMsg(meetingId: String, userId: String, approved: Boolean, approvedBy: String): BbbCommonEnvCoreMsg = {
    val routing = Routing.addMsgToClientRouting(MessageTypes.DIRECT, meetingId, userId)
    val envelope = BbbCoreEnvelope(GuestApprovedEvtMsg.NAME, routing)
    val header = BbbClientMsgHeader(GuestApprovedEvtMsg.NAME, meetingId, userId)

    val body = GuestApprovedEvtMsgBody(approved, approvedBy)
    val event = GuestApprovedEvtMsg(header, body)

    BbbCommonEnvCoreMsg(envelope, event)
  }

  def buildGuestsWaitingApprovedEvtMsg(meetingId: String, userId: String,
    guests: Vector[GuestApprovedVO], approvedBy: String): BbbCommonEnvCoreMsg = {
    val routing = Routing.addMsgToClientRouting(MessageTypes.DIRECT, meetingId, userId)
    val envelope = BbbCoreEnvelope(GuestsWaitingApprovedEvtMsg.NAME, routing)
    val header = BbbClientMsgHeader(GuestsWaitingApprovedEvtMsg.NAME, meetingId, userId)

    val body = GuestsWaitingApprovedEvtMsgBody(guests, approvedBy)
    val event = GuestsWaitingApprovedEvtMsg(header, body)

    BbbCommonEnvCoreMsg(envelope, event)
  }

  def buildGetGuestsWaitingApprovalRespMsg(meetingId: String, userId: String, guests: Vector[GuestWaiting]): BbbCommonEnvCoreMsg = {
    val routing = Routing.addMsgToClientRouting(MessageTypes.DIRECT, meetingId, userId)
    val envelope = BbbCoreEnvelope(GetGuestsWaitingApprovalRespMsg.NAME, routing)
    val header = BbbClientMsgHeader(GetGuestsWaitingApprovalRespMsg.NAME, meetingId, userId)

    val guestsWaiting = guests.map(g => GuestWaitingVO(g.intId, g.name, g.role))
    val body = GetGuestsWaitingApprovalRespMsgBody(guestsWaiting)
    val event = GetGuestsWaitingApprovalRespMsg(header, body)

    BbbCommonEnvCoreMsg(envelope, event)
  }

  def buildGuestsWaitingForApprovalEvtMsg(meetingId: String, userId: String, guests: Vector[GuestWaiting]): BbbCommonEnvCoreMsg = {
    val routing = Routing.addMsgToClientRouting(MessageTypes.DIRECT, meetingId, userId)
    val envelope = BbbCoreEnvelope(GuestsWaitingForApprovalEvtMsg.NAME, routing)
    val header = BbbClientMsgHeader(GuestsWaitingForApprovalEvtMsg.NAME, meetingId, userId)

    val guestsWaiting = guests.map(g => GuestWaitingVO(g.intId, g.name, g.role))
    val body = GuestsWaitingForApprovalEvtMsgBody(guestsWaiting)
    val event = GuestsWaitingForApprovalEvtMsg(header, body)

    BbbCommonEnvCoreMsg(envelope, event)
  }

  def buildValidateAuthTokenRespMsg(meetingId: String, userId: String, authToken: String,
    valid: Boolean, waitForApproval: Boolean): BbbCommonEnvCoreMsg = {
    val routing = Routing.addMsgToClientRouting(MessageTypes.DIRECT, meetingId, userId)
    val envelope = BbbCoreEnvelope(ValidateAuthTokenRespMsg.NAME, routing)
    val header = BbbClientMsgHeader(ValidateAuthTokenRespMsg.NAME, meetingId, userId)
    val body = ValidateAuthTokenRespMsgBody(userId, authToken, valid, waitForApproval)
    val event = ValidateAuthTokenRespMsg(header, body)
    BbbCommonEnvCoreMsg(envelope, event)
  }

  def buildGetUsersMeetingRespMsg(meetingId: String, userId: String, webusers: Vector[WebUser]): BbbCommonEnvCoreMsg = {
    val routing = Routing.addMsgToClientRouting(MessageTypes.DIRECT, meetingId, userId)
    val envelope = BbbCoreEnvelope(GetUsersMeetingRespMsg.NAME, routing)
    val header = BbbClientMsgHeader(GetUsersMeetingRespMsg.NAME, meetingId, userId)

    val body = GetUsersMeetingRespMsgBody(webusers)
    val event = GetUsersMeetingRespMsg(header, body)

    BbbCommonEnvCoreMsg(envelope, event)
  }

  def buildGetWebcamStreamsMeetingRespMsg(meetingId: String, userId: String, streams: Vector[WebcamStreamVO]): BbbCommonEnvCoreMsg = {
    val routing = Routing.addMsgToClientRouting(MessageTypes.DIRECT, meetingId, userId)
    val envelope = BbbCoreEnvelope(GetWebcamStreamsMeetingRespMsg.NAME, routing)
    val header = BbbClientMsgHeader(GetWebcamStreamsMeetingRespMsg.NAME, meetingId, userId)

    val body = GetWebcamStreamsMeetingRespMsgBody(streams)
    val event = GetWebcamStreamsMeetingRespMsg(header, body)

    BbbCommonEnvCoreMsg(envelope, event)
  }

  def buildGetVoiceUsersMeetingRespMsg(meetingId: String, userId: String, voiceUsers: Vector[VoiceConfUser]): BbbCommonEnvCoreMsg = {
    val routing = Routing.addMsgToClientRouting(MessageTypes.DIRECT, meetingId, userId)
    val envelope = BbbCoreEnvelope(GetVoiceUsersMeetingRespMsg.NAME, routing)
    val header = BbbClientMsgHeader(GetVoiceUsersMeetingRespMsg.NAME, meetingId, userId)

    val body = GetVoiceUsersMeetingRespMsgBody(voiceUsers)
    val event = GetVoiceUsersMeetingRespMsg(header, body)

    BbbCommonEnvCoreMsg(envelope, event)
  }

  def buildPresenterAssignedEvtMsg(meetingId: String, intId: String, name: String, assignedBy: String): BbbCommonEnvCoreMsg = {
    val routing = Routing.addMsgToClientRouting(MessageTypes.BROADCAST_TO_MEETING, meetingId, intId)
    val envelope = BbbCoreEnvelope(PresenterAssignedEvtMsg.NAME, routing)

    val body = PresenterAssignedEvtMsgBody(intId, name, assignedBy)
    val header = BbbClientMsgHeader(PresenterAssignedEvtMsg.NAME, meetingId, intId)
    val event = PresenterAssignedEvtMsg(header, body)

    BbbCommonEnvCoreMsg(envelope, event)
  }
}
