import Captions from '/imports/api/captions';
import Logger from '/imports/startup/server/logger';
import { check } from 'meteor/check';

export default function updateOwnerId(meetingId, userId, padId) {
  check(meetingId, String);
  check(userId, String);
  check(padId, String);

  const selector = {
    meetingId,
    padId,
  };

  const modifier = {
    $set: {
      ownerId: userId,
    },
  };

  const cb = (err) => {
    if (err) {
      return Logger.error(`Updating captions pad: ${err}`);
    }

    return Logger.verbose(`Update captions pad=${padId} ownerId=${userId}`);
  };

  return Captions.update(selector, modifier, { multi: true }, cb);
}
