package org.bigbluebutton.core.apps.presentation

import org.bigbluebutton.common2.msgs._
import org.bigbluebutton.core.apps.presentationpod.PresentationPodsApp
import org.bigbluebutton.core.bus.MessageBus
import org.bigbluebutton.core.domain.MeetingState2x
import org.bigbluebutton.core.models.Users2x
import org.bigbluebutton.core.running.LiveMeeting

trait PresentationUploadTokenReqMsgHdlr {
  this: PresentationApp2x =>

  def handle(msg: PresentationUploadTokenReqMsg, state: MeetingState2x,
             liveMeeting: LiveMeeting, bus: MessageBus): Unit = {

    def broadcastPresentationUploadTokenPassResp(msg: PresentationUploadTokenReqMsg, token: String): Unit = {
      // send back to client
      val routing = Routing.addMsgToClientRouting(MessageTypes.DIRECT, liveMeeting.props.meetingProp.intId, msg.header.userId)
      val envelope = BbbCoreEnvelope(PresentationUploadTokenPassRespMsg.NAME, routing)
      val header = BbbClientMsgHeader(PresentationUploadTokenPassRespMsg.NAME, liveMeeting.props.meetingProp.intId, msg.header.userId)

      val body = PresentationUploadTokenPassRespMsgBody(msg.body.podId, token, msg.body.filename)
      val event = PresentationUploadTokenPassRespMsg(header, body)
      val msgEvent = BbbCommonEnvCoreMsg(envelope, event)
      bus.outGW.send(msgEvent)
    }

    def broadcastPresentationUploadTokenFailResp(msg: PresentationUploadTokenReqMsg): Unit = {
      // send back to client
      val routing = Routing.addMsgToClientRouting(MessageTypes.DIRECT, liveMeeting.props.meetingProp.intId, msg.header.userId)
      val envelope = BbbCoreEnvelope(PresentationUploadTokenFailRespMsg.NAME, routing)
      val header = BbbClientMsgHeader(PresentationUploadTokenFailRespMsg.NAME, liveMeeting.props.meetingProp.intId, msg.header.userId)

      val body = PresentationUploadTokenFailRespMsgBody(msg.body.podId, msg.body.filename)
      val event = PresentationUploadTokenFailRespMsg(header, body)
      val msgEvent = BbbCommonEnvCoreMsg(envelope, event)
      bus.outGW.send(msgEvent)
    }

    def broadcastPresentationUploadTokenSysPubMsg(msg: PresentationUploadTokenReqMsg, token: String): Unit = {
      // send to bbb-web
      val routing = collection.immutable.HashMap("sender" -> "bbb-apps-akka")
      val envelope = BbbCoreEnvelope(PresentationUploadTokenSysPubMsg.NAME, routing)
      val header = BbbClientMsgHeader(PresentationUploadTokenSysPubMsg.NAME, liveMeeting.props.meetingProp.intId, msg.header.userId)

      val body = PresentationUploadTokenSysPubMsgBody(msg.body.podId, token, msg.body.filename)
      val event = PresentationUploadTokenSysPubMsg(header, body)
      val msgEvent = BbbCommonEnvCoreMsg(envelope, event)

      bus.outGW.send(msgEvent)
    }

    def userIsAllowedToUploadInPod(podId: String, userId: String): Boolean = {
      if (Users2x.userIsInPresenterGroup(liveMeeting.users2x, userId)) {
        for {
          pod <- PresentationPodsApp.getPresentationPod(state, podId)
        } yield {
          return pod.currentPresenter == userId
        }
      }

      false
    }

    log.info("handlePresentationUploadTokenReqMsg" + liveMeeting.props.meetingProp.intId +
      " userId=" + msg.header.userId + " filename=" + msg.body.filename)

    if (userIsAllowedToUploadInPod(msg.body.podId, msg.header.userId)) {
      val token = PresentationPodsApp.generateToken(msg.body.podId, msg.header.userId)
      broadcastPresentationUploadTokenPassResp(msg, token)
      broadcastPresentationUploadTokenSysPubMsg(msg, token)
    } else {
      broadcastPresentationUploadTokenFailResp(msg)
    }
  }

}
