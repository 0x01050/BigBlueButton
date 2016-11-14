import Breakouts from '/imports/api/breakouts';
import { Meteor } from 'meteor/meteor';
import { check } from 'meteor/check';
import Logger from '/imports/startup/server/logger';
import { isAllowedTo } from '/imports/startup/server/userPermissions';

Meteor.publish('breakouts', (credentials) => {
  Logger.info(`PUBLISHIIIIIIING breakouts for ${credentials.meetingId}`);
  return Breakouts.find({
    $or: [
      { parentMeetingId: credentials.meetingId },
      { breakoutMeetingId: credentials.meetingId },
    ],
  });
});
