import { check } from 'meteor/check';
import Logger from '/imports/startup/server/logger';
import Meetings from '/imports/api/2.0/meetings';
import Users from '/imports/api/2.0/users';

import addChat from '/imports/api/2.0/chat/server/modifiers/addChat';
import clearUserSystemMessages from '/imports/api/2.0/chat/server/modifiers/clearUserSystemMessages';

const addWelcomeChatMessage = (meetingId, userId) => {
  const APP_CONFIG = Meteor.settings.public.app;
  const CHAT_CONFIG = Meteor.settings.public.chat;

  const Meeting = Meetings.findOne({ 'meetingProp.intId': meetingId });

  if (!Meeting) {
    // TODO add meeting properly so it does not get reset
    return;
  }

  const welcomeMessage = APP_CONFIG.defaultWelcomeMessage
    .concat(APP_CONFIG.defaultWelcomeMessageFooter)
    .replace(/%%CONFNAME%%/, Meeting.meetingProp.name);

  const message = {
    chatType: CHAT_CONFIG.type_system,
    message: welcomeMessage,
    fromColor: '0x3399FF',
    toUserId: userId,
    fromUserId: CHAT_CONFIG.type_system,
    fromUsername: '',
    fromTime: (new Date()).getTime(),
  };

  addChat(meetingId, message);
};

export default function handleValidateAuthToken({ body }, meetingId) {
  const { userId, valid, waitForApproval } = body;

  check(userId, String);
  check(valid, Boolean);
  check(waitForApproval, Boolean);

  const selector = {
    meetingId,
    userId,
  };

  const User = Users.findOne(selector);

  // If we dont find the user on our collection is a flash user and we can skip
  if (!User) return;

  // User already flagged so we skip
  if (User.validated === valid) return;

  const modifier = {
    $set: {
      validated: valid,
      approved: !waitForApproval,
    },
  };

  const cb = (err, numChanged) => {
    if (err) {
      Logger.error(`Validating auth token: ${err}`);
      return;
    }

    if (numChanged) {
      if (valid && !waitForApproval) {
        clearUserSystemMessages(meetingId, userId);
        addWelcomeChatMessage(meetingId, userId);
      }

      Logger.info(`Validated auth token as ${valid
       }${+' user='}${userId} meeting=${meetingId}`,
      );
    }

    return Logger.info('No auth to validate');
  };

  Users.update(selector, modifier, cb);
}
