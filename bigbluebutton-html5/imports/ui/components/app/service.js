import { Meteor } from 'meteor/meteor';
import Auth from '/imports/ui/services/auth';
import Users from '/imports/api/users';
import Breakouts from '/imports/api/breakouts';
import Storage from '/imports/ui/services/storage/session';
import SettingsService from '/imports/ui/components/settings/service';

function redirectToLogoutUrl(reason) {
  console.error(reason);
  console.log('Redirecting user to the logoutURL...');
  document.location.href = Auth.logoutURL;
}

let wasKicked = false;
const wasKickedDep = new Tracker.Dependency;
// TODO: readd this observers
function observeUserKick() {
  Users.find().observe({
    removed(old) {
      if (old.userId === Auth.userID) {
        Auth.clearCredentials(() => {
          wasKicked = true;
          wasKickedDep.changed();
        });
      }
    },
  });
}
function observeBreakoutEnd() {
  Breakouts.find().observe({
    removed(old) {
      if (old.breakoutMeetingId === Auth.meetingID) {
        // The breakout room expired. Closing the browser tab to return to the main room
        window.close();
      }
    },
  });
}

function wasUserKicked() {
  wasKickedDep.depend();
  return wasKicked;
}

let modal = null;
const modalDep = new Tracker.Dependency;

const getModal = () => {
  modalDep.depend();
  return modal;
};

const showModal = (val) => {
  if (val !== modal) {
    modal = val;
    modalDep.changed();
  }
};

const clearModal = () => {
  showModal(null);
};

const getCaptionsStatus = () => {
  const settings = Storage.getItem('settings_cc');
  return settings ? settings.closedCaptions : false;
};

const getFontSize = () => {
  const settings = SettingsService.getSettingsFor('application');
  return settings ? settings.fontSize : '14px';
};

export {
  subscribeForData,
  setCredentials,
  subscribeFor,
  subscribeToCollections,
  wasUserKicked,
  redirectToLogoutUrl,
  getModal,
  showModal,
  clearModal,
  getCaptionsStatus,
  getFontSize,
};
