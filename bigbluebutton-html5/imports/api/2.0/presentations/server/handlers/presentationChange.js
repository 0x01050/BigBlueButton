import { check } from 'meteor/check';
import Logger from '/imports/startup/server/logger';
import Presentations from '/imports/api/2.0/presentations';

import addPresentation from '../modifiers/addPresentation';

const clearCurrentPresentation = (meetingId, presentationId) => {
  const selector = {
    meetingId,
    presentationId: { $ne: presentationId },
    'presentation.current': true,
  };

  const modifier = {
    $set: { 'presentation.current': false },
  };

  const cb = (err, numChanged) => {
    if (err) {
      return Logger.error(`Unsetting the current presentation: ${err}`);
    }

    if (numChanged) {
      return Logger.info('Unset as current presentation');
    }
  };

  return Presentations.update(selector, modifier, cb);
};

export default function handlePresentationChange({ header, body }) {
  const { meetingId } = header;
  const { presentation } = body;

  check(meetingId, String);
  check(presentation, Object);

  // We need to clear the flag of the older current presentation ¯\_(ツ)_/¯
  if (presentation.current) {
    clearCurrentPresentation(meetingId, presentation.id);
  }

  return addPresentation(meetingId, presentation);
}
