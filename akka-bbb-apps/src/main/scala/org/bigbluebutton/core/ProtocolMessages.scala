package org.bigbluebutton.core

case class OutMsgHeader(name: String)
case class OutMsgEnvelopeHeader(`type`: MessageType.MessageType, address: String)

trait OutMessage

case class CreateBreakoutRoomOutMsgEnvelope(header: OutMsgEnvelopeHeader, payload: CreateBreakoutRoomOutMsgEnvelopePayload)
case class CreateBreakoutRoomOutMsgEnvelopePayload(header: OutMsgHeader, payload: CreateBreakoutRoomOutMsgPayload)
case class CreateBreakoutRoomOutMsgPayload(meetingId: String, name: String, parentId: String,
  voiceConfId: String, durationInMinutes: Int,
  moderatorPassword: String, viewerPassword: String,
  sourcePresentationId: String, sourcePresentationSlide: Int, record: Boolean)

