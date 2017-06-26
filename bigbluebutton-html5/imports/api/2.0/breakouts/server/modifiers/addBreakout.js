import Breakouts from './../../';
import Logger from '/imports/startup/server/logger';
import { check } from 'meteor/check';

export default function addBreakout(payload) {
  const {
    breakoutMeetingId,
    parentId,
    name,
  } = payload;

  check(breakoutMeetingId, String);
  check(parentId, String);
  check(name, String);

  const selector = { breakoutMeetingId };

  const modifier = {
    $set: {
      breakoutMeetingId,
      parentId,
      name,
    },
  };

  const cb = (err, numChanged) => {
    if (err) {
      return Logger.error(`Adding breakout to collection: ${err}`);
    }

    const {
      insertedId,
    } = numChanged;
    if (insertedId) {
      return Logger.info(`Added breakout id=${breakoutMeetingId}`);
    }

    return Logger.info(`Upserted breakout id=${breakoutMeetingId}`);
  };

  return Breakouts.upsert(selector, modifier, cb);
}
