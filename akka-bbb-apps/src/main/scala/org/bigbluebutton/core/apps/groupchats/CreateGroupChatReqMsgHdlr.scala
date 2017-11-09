package org.bigbluebutton.core.apps.groupchats

import org.bigbluebutton.common2.msgs._
import org.bigbluebutton.core.bus.MessageBus
import org.bigbluebutton.core.domain.MeetingState2x
import org.bigbluebutton.core.models.GroupChat
import org.bigbluebutton.core.running.LiveMeeting
import org.bigbluebutton.core.apps.PermissionCheck
import org.bigbluebutton.SystemConfiguration
import org.bigbluebutton.core.models.Users2x
import org.bigbluebutton.core.models.Roles
import org.bigbluebutton.core2.MeetingStatus2x

trait CreateGroupChatReqMsgHdlr extends SystemConfiguration {
  this: GroupChatHdlrs =>

  def handle(msg: CreateGroupChatReqMsg, state: MeetingState2x,
             liveMeeting: LiveMeeting, bus: MessageBus): MeetingState2x = {
    log.debug("RECEIVED CREATE CHAT REQ MESSAGE")

    var chatLocked: Boolean = false;

    for {
      user <- Users2x.findWithIntId(liveMeeting.users2x, msg.header.userId)
    } yield {
      if (user.role != Roles.MODERATOR_ROLE && user.locked) {
        val permissions = MeetingStatus2x.getPermissions(liveMeeting.status)
        if (msg.body.access == GroupChatAccess.PRIVATE) {
          chatLocked = permissions.disablePrivChat
        } else {
          chatLocked = permissions.disablePubChat
        }
      }
    }

    if (applyPermissionCheck && chatLocked) {
      val meetingId = liveMeeting.props.meetingProp.intId
      val reason = "No permission to create a new group chat."
      PermissionCheck.ejectUserForFailedPermission(meetingId, msg.header.userId, reason, bus.outGW)
      state
    } else {
      val newState = for {
        createdBy <- GroupChatApp.findGroupChatUser(msg.header.userId, liveMeeting.users2x)
      } yield {
        val msgs = msg.body.msg.map(m => GroupChatApp.toGroupChatMessage(createdBy, m))
        val users = {
          if (msg.body.access == GroupChatAccess.PRIVATE) {
            val cu = msg.body.users.toSet + msg.body.requesterId
            cu.flatMap(u => GroupChatApp.findGroupChatUser(u, liveMeeting.users2x)).toVector
          } else {
            Vector.empty
          }
        }

        val gc = GroupChatApp.createGroupChat(msg.body.name, msg.body.access, createdBy, users, msgs)
        sendMessages(msg, gc, liveMeeting, bus)

        val groupChats = state.groupChats.add(gc)
        state.update(groupChats)
      }

      newState.getOrElse(state)
    }
  }

  def sendMessages(msg: CreateGroupChatReqMsg, gc: GroupChat,
                   liveMeeting: LiveMeeting, bus: MessageBus): Unit = {
    def makeHeader(name: String, meetingId: String, userId: String): BbbClientMsgHeader = {
      BbbClientMsgHeader(name, meetingId, userId)
    }

    def makeEnvelope(msgType: String, name: String, meetingId: String, userId: String): BbbCoreEnvelope = {
      val routing = Routing.addMsgToClientRouting(msgType, meetingId, userId)
      BbbCoreEnvelope(name, routing)
    }

    def makeBody(chatId: String, name: String,
                 access: String, correlationId: String,
                 createdBy: GroupChatUser, users: Vector[GroupChatUser],
                 msgs: Vector[GroupChatMsgToUser]): GroupChatCreatedEvtMsgBody = {
      GroupChatCreatedEvtMsgBody(correlationId, chatId, createdBy,
        name, access, users, msgs)
    }

    val meetingId = liveMeeting.props.meetingProp.intId
    val correlationId = msg.body.correlationId
    val users = gc.users
    val msgs = gc.msgs.map(m => GroupChatApp.toMessageToUser(m))

    if (gc.access == GroupChatAccess.PRIVATE) {
      def sendDirectMessage(userId: String): Unit = {
        val envelope = makeEnvelope(MessageTypes.DIRECT, GroupChatCreatedEvtMsg.NAME, meetingId, userId)
        val header = makeHeader(GroupChatCreatedEvtMsg.NAME, meetingId, userId)

        val body = makeBody(gc.id, gc.name, gc.access, correlationId, gc.createdBy, users, msgs)
        val event = GroupChatCreatedEvtMsg(header, body)
        val outEvent = BbbCommonEnvCoreMsg(envelope, event)
        bus.outGW.send(outEvent)
      }

      users.foreach(u => sendDirectMessage(u.id))

    } else {
      val meetingId = liveMeeting.props.meetingProp.intId
      val userId = msg.body.requesterId
      val envelope = makeEnvelope(MessageTypes.BROADCAST_TO_MEETING, GroupChatCreatedEvtMsg.NAME,
        meetingId, userId)
      val header = makeHeader(GroupChatCreatedEvtMsg.NAME, meetingId, userId)

      val body = makeBody(gc.id, gc.name, gc.access, correlationId, gc.createdBy, users, msgs)
      val event = GroupChatCreatedEvtMsg(header, body)

      val outEvent = BbbCommonEnvCoreMsg(envelope, event)
      bus.outGW.send(outEvent)
    }

  }
}
