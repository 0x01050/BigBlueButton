import React from 'react';
import { withTracker } from 'meteor/react-meteor-data';
import { Session } from 'meteor/session';
import getFromUserSettings from '/imports/ui/services/users-settings';
import ActionsBar from './component';
import Service from './service';
import VideoService from '../video-provider/service';
import { shareScreen, unshareScreen, isVideoBroadcasting } from '../screenshare/service';
import { withRouter } from 'react-router';

const ActionsBarContainer = props => <ActionsBar {...props} />;

export default withRouter(withTracker(({ }) => {
  const togglePollMenu = () => {
    if (Session.equals('isChatOpen', true)) Session.set('isChatOpen', false);

    if (Session.equals('isPollOpen', false)) {
      Session.set('isUserListOpen', true);
      return Session.set('isPollOpen', true);
    }

    Session.set('isUserListOpen', true);
    return Session.set('isPollOpen', false);
  };

  return {
    isUserPresenter: Service.isUserPresenter(),
    isUserModerator: Service.isUserModerator(),
    handleExitVideo: () => VideoService.exitVideo(),
    handleJoinVideo: () => VideoService.joinVideo(),
    handleShareScreen: onFail => shareScreen(onFail),
    handleUnshareScreen: () => unshareScreen(),
    isVideoBroadcasting: isVideoBroadcasting(),
    recordSettingsList: Service.recordSettingsList(),
    toggleRecording: Service.toggleRecording,
    screenSharingCheck: getFromUserSettings('enableScreensharing', Meteor.settings.public.kurento.enableScreensharing),
    enableVideo: getFromUserSettings('enableVideo', Meteor.settings.public.kurento.enableVideo),
    togglePollMenu,
  };
})(ActionsBarContainer));
