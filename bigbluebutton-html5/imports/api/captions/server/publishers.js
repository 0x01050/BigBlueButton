import Captions from '/imports/api/captions';
import { Meteor } from 'meteor/meteor';
import { check } from 'meteor/check';
import Logger from '/imports/startup/server/logger';

import mapToAcl from '/imports/startup/mapToAcl';

Meteor.publish('captions', function() {
  captions = captions.bind(this);
  return mapToAcl('captions',captions)(arguments);
});

function captions(credentials) {
  const { meetingId, requesterUserId, requesterToken } = credentials;

  check(meetingId, String);
  check(requesterUserId, String);
  check(requesterToken, String);

  Logger.verbose(`Publishing Captions for ${meetingId} ${requesterUserId} ${requesterToken}`);

  return Captions.find({ meetingId });
};
