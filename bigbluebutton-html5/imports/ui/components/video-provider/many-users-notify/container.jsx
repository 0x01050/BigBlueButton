import { withTracker } from 'meteor/react-meteor-data';
import Meetings from '/imports/api/meetings/';
import Auth from '/imports/ui/services/auth';
import Users from '/imports/api/users/';
import VideoUsers from '/imports/api/video-users';
import LockViewersService from '/imports/ui/components/lock-viewers/service';
import ManyUsersComponent from './component';

const USER_CONFIG = Meteor.settings.public.user;
const ROLE_MODERATOR = USER_CONFIG.role_moderator;
const ROLE_VIEWER = USER_CONFIG.role_viewer;

export default withTracker(() => {
  const videoUsers = VideoUsers.find({ meetingId: Auth.meetingID, hasStream: true }).fetch();
  const videoUsersIds = videoUsers.map(u => u.userId);
  return {
    viewersInWebcam: Users.find({
      meetingId: Auth.meetingID,
      userId: {
        $in: videoUsersIds,
      },
      role: ROLE_VIEWER,
      presenter: false,
    }).count(),
    currentUserIsModerator: Users.findOne({ userId: Auth.userID }).role === ROLE_MODERATOR,
    lockSettings: Meetings.findOne({ meetingId: Auth.meetingID }).lockSettingsProps,
    webcamOnlyForModerator: Meetings.findOne({
      meetingId: Auth.meetingID,
    }).usersProp.webcamsOnlyForModerator,
    limitOfViewersInWebcam: Meteor.settings.public.app.viewersInWebcam,
    limitOfViewersInWebcamIsEnable: Meteor.settings.public.app.enableLimitOfViewersInWebcam,
    toggleWebcamsOnlyForModerator: LockViewersService.toggleWebcamsOnlyForModerator,
  };
})(ManyUsersComponent);
