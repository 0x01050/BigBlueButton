package org.bigbluebutton.core.apps.presentation

import org.bigbluebutton.core.OutMessageGateway
import org.bigbluebutton.common2.msgs._
import org.bigbluebutton.common2.domain.PageVO

trait ResizeAndMovePagePubMsgHdlr {
  this: PresentationApp2x =>

  val outGW: OutMessageGateway

  def handleResizeAndMovePagePubMsg(msg: ResizeAndMovePagePubMsg): Unit = {

    def broadcastEvent(msg: ResizeAndMovePagePubMsg, page: PageVO): Unit = {
      val routing = Routing.addMsgToClientRouting(MessageTypes.BROADCAST_TO_MEETING, liveMeeting.props.meetingProp.intId, msg.header.userId)
      val envelope = BbbCoreEnvelope(ResizeAndMovePageEvtMsg.NAME, routing)
      val header = BbbClientMsgHeader(ResizeAndMovePageEvtMsg.NAME, liveMeeting.props.meetingProp.intId, msg.header.userId)

      val body = ResizeAndMovePageEvtMsgBody(msg.body.presentationId, page.id, page.xOffset, page.yOffset, page.widthRatio, page.heightRatio)
      val event = ResizeAndMovePageEvtMsg(header, body)
      val msgEvent = BbbCommonEnvCoreMsg(envelope, event)
      outGW.send(msgEvent)

      //record(event)
    }

    for {
      page <- resizeAndMovePage(msg.body.presentationId, msg.body.pageId, msg.body.xOffset, msg.body.yOffset, msg.body.widthRatio, msg.body.heightRatio)
    } yield {
      broadcastEvent(msg, page)
    }
  }
}
