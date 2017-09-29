import { check } from 'meteor/check';
import Logger from '/imports/startup/server/logger';
import Users from '/imports/api/2.0/users';
import VoiceUsers from '/imports/api/2.0/voice-users';
import removeVoiceUser from '/imports/api/2.0/voice-users/server/modifiers/removeVoiceUser';

export default function userEjected(meetingId, userId) {
  check(meetingId, String);
  check(userId, String);

  const selector = {
    meetingId,
    userId,
  };

  const modifier = {
    $set: {
      ejected: true,
      connectionStatus: 'offline',
      listenOnly: false,
      validated: false,
      emoji: 'none',
      presenter: false,
      role: 'VIEWER',
    },
  };

  const user = VoiceUsers.findOne({ meetingId, voiceUserId: userId });

  if (user) {
    const voiceUser = {
      voiceConf: user.voiceConf,
      voiceUserId: user.voiceUserId,
      intId: user.intId,
    };

    removeVoiceUser(meetingId, voiceUser);
  }

  const cb = (err, numChanged) => {
    if (err) {
      return Logger.error(`Ejecting user from collection: ${err}`);
    }

    if (numChanged) {
      return Logger.info(`Ejected user id=${userId} meeting=${meetingId}`);
    }

    return null;
  };

  return Users.update(selector, modifier, cb);
}
