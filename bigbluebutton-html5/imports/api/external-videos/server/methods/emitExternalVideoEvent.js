import Users from '/imports/api/users';
import Logger from '/imports/startup/server/logger';
import { extractCredentials } from '/imports/api/common/server/helpers';

export default function emitExternalVideoEvent(messageName, ...rest) {
  const { meetingId, userId } = extractCredentials(this.userId);

  const user = Users.findOne({ userId });

  if (user && user.presenter) {
    const streamerName = `external-videos-${meetingId}`;
    const streamer = Meteor.StreamerCentral.instances[streamerName];

    if (streamer) {
      streamer.emit(messageName, ...rest)
    } else {
      Logger.info("streamer not found")
    }
  }
}

