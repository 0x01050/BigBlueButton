import { Meteor } from 'meteor/meteor';
import Chat from '/imports/api/2.0/chat';
import Logger from '/imports/startup/server/logger';
import addChat from './../modifiers/addChat';

export default function publicHistoryClear({ header }, meetingId) {
  const CHAT_CONFIG = Meteor.settings.public.chat;
  const PUBLIC_CHAT_USERID = CHAT_CONFIG.public_userid;
  const PUBLIC_CHAT_USERNAME = CHAT_CONFIG.public_username;

  if (meetingId) {
    Chat.remove({ meetingId, 'message.toUserId': PUBLIC_CHAT_USERID },
      Logger.info(`Cleared Chats (${meetingId})`));

    addChat(meetingId, {
      message: '<b><i>The public chat history was cleared by a moderator</i></b>',
      fromTime: 0,
      toUserId: PUBLIC_CHAT_USERID,
      toUsername: PUBLIC_CHAT_USERNAME,
      fromUserId: header.userId,
      fromUsername: '',
    },
    );
  }

  return null;
}
