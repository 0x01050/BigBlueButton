import RedisPubSub from '/imports/startup/server/redis';
import { check } from 'meteor/check';
import Presentations from '/imports/api/presentations';

export default function setPresentation(credentials, presentationId, podId) {
  const REDIS_CONFIG = Meteor.settings.private.redis;
  const CHANNEL = REDIS_CONFIG.channels.toAkkaApps;
  const EVENT_NAME = 'SetCurrentPresentationPubMsg';

  const { meetingId, requesterUserId } = credentials;

  check(meetingId, String);
  check(requesterUserId, String);
  check(presentationId, String);
  check(podId, String);

  const currentPresentation = Presentations.findOne({
    meetingId,
    id: presentationId,
    podId,
    current: true,
  });

  if (currentPresentation && currentPresentation.id === presentationId) {
    return Promise.resolve();
  }

  const payload = {
    presentationId,
    podId,
  };

  return RedisPubSub.publishUserMessage(CHANNEL, EVENT_NAME, meetingId, requesterUserId, payload);
}
