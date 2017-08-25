import Acl from '/imports/startup/acl';
import { getMultiUserStatus } from '/imports/api/common/server/helpers';
import RedisPubSub from '/imports/startup/server/redis2x';
import { Meteor } from 'meteor/meteor';
import { check } from 'meteor/check';
import Annotations from '/imports/api/2.0/annotations';

function isLastMessage(annotation, userId) {
  if (annotation.status === 'DRAW_END') {
    const selector = {
      id: annotation.id,
      userId,
    };

    const _annotation = Annotations.findOne(selector);
    if (_annotation != null) {
      return true;
    }
    return false;
  }

  return false;
}

export default function sendAnnotation(credentials, annotation) {
  const REDIS_CONFIG = Meteor.settings.redis;
  const CHANNEL = REDIS_CONFIG.channels.toAkkaApps;
  const EVENT_NAME = 'SendWhiteboardAnnotationPubMsg';

  const { meetingId, requesterUserId, requesterToken } = credentials;

  check(meetingId, String);
  check(requesterUserId, String);
  check(requesterToken, String);
  check(annotation, Object);

  // We allow messages to pass through in 3 cases:
  // 1. When it's a standard message in presenter mode (Acl check)
  // 2. When it's a standard message in multi-user mode (getMultUserStatus check)
  // 3. When it's the last message, happens when the user is currently drawing
  // and then slide/presentation changes, the user lost presenter rights,
  // or multi-user whiteboard gets turned off
  // So we allow the last "DRAW_END" message to pass through, to finish the shape.
  if (Acl.can('methods.sendAnnotation', credentials) ||
    getMultiUserStatus(meetingId) ||
    isLastMessage(annotation, requesterUserId)) {
    const header = {
      name: EVENT_NAME,
      meetingId,
      userId: requesterUserId,
    };

    const payload = {
      annotation,
    };

    return RedisPubSub.publish(CHANNEL, EVENT_NAME, meetingId, payload, header);
  }

  throw new Meteor.Error(
    'not-allowed', `User ${requesterUserId} is not allowed to send an annotation`,
  );
}
