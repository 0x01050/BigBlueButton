package org.bigbluebutton.core.apps.groupchats

import org.bigbluebutton.common2.msgs.{ GroupChatAccess, GroupChatMsgFromUser, GroupChatUser }
import org.bigbluebutton.core.domain.{ BbbSystemConst, MeetingState2x }
import org.bigbluebutton.core.models._
import org.bigbluebutton.core.running.LiveMeeting

object GroupChatApp {

  val MAIN_PUBLIC_CHAT = "MAIN-PUBLIC-GROUP-CHAT"

  def createGroupChat(chatName: String, access: String, createBy: GroupChatUser): GroupChat = {
    val gcId = GroupChatFactory.genId()
    GroupChatFactory.create(gcId, chatName, access, createBy)
  }

  def toGroupChatMessage(sender: GroupChatUser, msg: GroupChatMsgFromUser): GroupChatMessage = {
    val now = System.currentTimeMillis()
    val id = GroupChatFactory.genId()
    GroupChatMessage(id, now, msg.correlationId, now, now, sender, msg.font,
      msg.size, msg.color, msg.message)
  }

  def addGroupChatMessage(chat: GroupChat, chats: GroupChats,
                          msg: GroupChatMessage): GroupChats = {
    val c = chat.add(msg)
    chats.update(c)
  }

  def sender(userId: String, users: Users2x): Option[GroupChatUser] = {
    Users2x.findWithIntId(users, userId) match {
      case Some(u) => Some(GroupChatUser(u.intId, u.name))
      case None =>
        if (userId == BbbSystemConst.SYSTEM_USER) {
          Some(GroupChatUser(BbbSystemConst.SYSTEM_USER, BbbSystemConst.SYSTEM_USER))
        } else {
          None
        }
    }
  }

  def createDefaultPublicGroupChat(state: MeetingState2x): MeetingState2x = {
    val createBy = GroupChatUser(BbbSystemConst.SYSTEM_USER, BbbSystemConst.SYSTEM_USER)
    val defaultPubGroupChat = GroupChatFactory.create(MAIN_PUBLIC_CHAT, MAIN_PUBLIC_CHAT,
      GroupChatAccess.PUBLIC, createBy)
    val groupChats = state.groupChats.add(defaultPubGroupChat)
    state.update(groupChats)
  }

  def genTestChatMsgHistory(state: MeetingState2x, userId: String, liveMeeting: LiveMeeting): MeetingState2x = {
    def addH(state: MeetingState2x, userId: String, liveMeeting: LiveMeeting, msg: GroupChatMsgFromUser): MeetingState2x = {
      val newState = for {
        sender <- GroupChatApp.sender(userId, liveMeeting.users2x)
        chat <- state.groupChats.find(MAIN_PUBLIC_CHAT)
      } yield {

        val gcm1 = GroupChatApp.toGroupChatMessage(sender, msg)
        val gcs1 = GroupChatApp.addGroupChatMessage(chat, state.groupChats, gcm1)
        state.update(gcs1)
      }

      newState match {
        case Some(ns) => ns
        case None     => state
      }
    }

    val sender = GroupChatUser(BbbSystemConst.SYSTEM_USER, BbbSystemConst.SYSTEM_USER)
    val h1 = GroupChatMsgFromUser(correlationId = "cor1", sender = sender,
      font = "arial", size = 12, color = "red", message = "Hello Foo!")
    val h2 = GroupChatMsgFromUser(correlationId = "cor2", sender = sender,
      font = "arial", size = 12, color = "red", message = "Hello Bar!")
    val h3 = GroupChatMsgFromUser(correlationId = "cor3", sender = sender,
      font = "arial", size = 12, color = "red", message = "Hello Baz!")
    val state1 = addH(state, BbbSystemConst.SYSTEM_USER, liveMeeting, h1)
    val state2 = addH(state1, BbbSystemConst.SYSTEM_USER, liveMeeting, h2)
    val state3 = addH(state2, BbbSystemConst.SYSTEM_USER, liveMeeting, h3)
    state3
  }
}
