import { isAllowedTo } from '/imports/startup/server/userPermissions';
import { appendMessageHeader, publish } from '/imports/startup/server/helpers';
import { redisConfig } from '/config';

Meteor.methods({
  //meetingId: the meeting where the user is
  //toKickUserId: the userid of the user to kick
  //requesterUserId: the userid of the user that wants to kick
  //authToken: the authToken of the user that wants to kick
  kickUser(credentials, toKickUserId) {
    const { meetingId, requesterUserId, requesterToken } = credentials;
    let message;
    if (isAllowedTo('kickUser', credentials)) {
      message = {
        payload: {
          userid: toKickUserId,
          ejected_by: requesterUserId,
          meeting_id: meetingId,
        },
      };
      message = appendMessageHeader('eject_user_from_meeting_request_message', message);
      return publish(redisConfig.channels.toBBBApps.users, message);
    }
  },
});
