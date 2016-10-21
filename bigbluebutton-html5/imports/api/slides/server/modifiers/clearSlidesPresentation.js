import Slides from '/imports/api/slides';
import Logger from '/imports/startup/server/logger';
import { check } from 'meteor/check';

export default function clearSlidesPresentation(presentationId) {
  check(presentationId, String);

  const selector = {
    presentationId,
  };

  const cb = (err) => {
    if (err) {
      return Logger.error(`Removing Slides from collection: ${err}`);
    }

    return Logger.info(`Removed Slides where presentationId=${presentationId}`);
  };

  return Slides.remove(selector, cb);
};
