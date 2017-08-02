import Logger from '/imports/startup/server/logger';
import Breakouts from '/imports/api/2.0/breakouts';

export default function clearBreakouts(breakoutMeetingId) {
  if (breakoutMeetingId) {
    const selector = {
      breakoutMeetingId,
    };

    return Breakouts.remove(selector);
  }

  return Breakouts.remove({}, Logger.info('Cleared Breakouts (all)'));
}
