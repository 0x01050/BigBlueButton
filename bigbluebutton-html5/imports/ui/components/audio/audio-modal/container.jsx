import React from 'react';
import { withTracker } from 'meteor/react-meteor-data';
import { withModalMounter } from '/imports/ui/components/modal/service';
import browser from 'browser-detect';
import getFromUserSettings from '/imports/ui/services/users-settings';
import AudioModal from './component';
import Meetings from '/imports/api/meetings';
import Auth from '/imports/ui/services/auth';
import Service from '../service';

const AudioModalContainer = props => <AudioModal {...props} />;

const APP_CONFIG = Meteor.settings.public.app;


export default withModalMounter(withTracker(({ mountModal }) => {
  const listenOnlyMode = getFromUserSettings('listenOnlyMode', APP_CONFIG.listenOnlyMode);
  const forceListenOnly = getFromUserSettings('forceListenOnly', APP_CONFIG.forceListenOnly);
  const skipCheck = getFromUserSettings('skipCheck', APP_CONFIG.skipCheck);
  const { dialNumber, telVoice } = Meetings.findOne({ meetingId: Auth.meetingID }).voiceProp;
  let formattedDialNum = dialNumber || '';
  formattedDialNum = formattedDialNum.replace(/-/g, '');
  formattedDialNum = `(${formattedDialNum.substring(0, 3)}) ${formattedDialNum.substring(3, 6)}-${formattedDialNum.substring(6, 11)}`;

  return ({
    closeModal: () => {
      if (!Service.isConnecting()) mountModal(null);
    },
    joinMicrophone: () => {
      const call = new Promise((resolve, reject) => {
        if (skipCheck) {
          resolve(Service.joinMicrophone());
        } else {
          resolve(Service.transferCall());
        }
        reject(() => {
          Service.exitAudio();
        });
      });

      return call.then(() => {
        mountModal(null);
      }).catch((error) => {
        throw error;
      });
    },
    joinListenOnly: () => Service.joinListenOnly().then(() => mountModal(null)),
    leaveEchoTest: () => {
      if (!Service.isEchoTest()) {
        return Promise.resolve();
      }
      return Service.exitAudio();
    },
    changeInputDevice: inputDeviceId => Service.changeInputDevice(inputDeviceId),
    changeOutputDevice: outputDeviceId => Service.changeOutputDevice(outputDeviceId),
    joinEchoTest: () => Service.joinEchoTest(),
    exitAudio: () => Service.exitAudio(),
    isConnecting: Service.isConnecting(),
    isConnected: Service.isConnected(),
    isEchoTest: Service.isEchoTest(),
    inputDeviceId: Service.inputDeviceId(),
    outputDeviceId: Service.outputDeviceId(),
    showPermissionsOvelay: Service.isWaitingPermissions(),
    listenOnlyMode,
    skipCheck,
    dialNumber,
    telVoice,
    formattedDialNum,
    audioLocked: Service.audioLocked(),
    joinFullAudioImmediately: !listenOnlyMode && skipCheck,
    joinFullAudioEchoTest: !listenOnlyMode && !skipCheck,
    forceListenOnlyAttendee: listenOnlyMode && forceListenOnly && !Service.isUserModerator(),
    isIOSChrome: browser().name === 'crios',
    isMobileNative: navigator.userAgent.toLowerCase().includes('bbbnative'),
    isIEOrEdge: browser().name === 'edge' || browser().name === 'ie',
  });
})(AudioModalContainer));
