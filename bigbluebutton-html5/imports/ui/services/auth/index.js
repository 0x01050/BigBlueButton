import { Tracker } from 'meteor/tracker';

import Storage from '/imports/ui/services/storage/session';

import Users from '/imports/api/users';
import { callServer } from '/imports/ui/services/api';

class Auth {
  constructor() {
    this._meetingID = Storage.getItem('meetingID');
    this._userID = Storage.getItem('userID');
    this._authToken = Storage.getItem('authToken');
    this._loggedIn = {
      value: false,
      tracker: new Tracker.Dependency,
    };

    this._addObserverToReconnection();
  }

  get meetingID() {
    return this._meetingID;
  }

  set meetingID(meetingID) {
    this._meetingID = meetingID;
    Storage.setItem('meetingID', this._meetingID);
  }

  get userID() {
    return this._userID;
  }

  set userID(userID) {
    this._userID = userID;
    Storage.setItem('userID', this._userID);
  }

  get token() {
    return this._authToken;
  }

  set token(authToken) {
    this._authToken = authToken;
    Storage.setItem('authToken', this._authToken);
  }

  get loggedIn() {
    this._loggedIn.tracker.depend();
    return this._loggedIn.value;
  }

  set loggedIn(value) {
    this._loggedIn.value = value;
    this._loggedIn.tracker.changed();
  }

  get credentials() {
    return {
      meetingId: this.meetingID,
      requesterUserId: this.userID,
      requesterToken: this.token,
    };
  }

  set credentials(value) {
    throw 'Credentials are read-only';
  }

  clearCredentials() {
    this.meetingID = null;
    this.userID = null;
    this.token = null;
    this.loggedIn = false;

    return Promise.resolve(...arguments);
  };

  logout() {
    if (!this.loggedIn) {
      return Promise.resolve();
    }

    return new Promise((resolve, reject) => {
      callServer('userLogout', () => {
        this.fetchLogoutUrl()
          .then(this.clearCredentials)
          .then(resolve);
      });
    });
  };

  authenticate(meetingID, userID, token) {
    if (arguments.length) {
      this.meetingID = meetingID;
      this.userID = userID;
      this.token = token;
    }

    return this._subscribeToCurrentUser()
      .then(this._addObserverToValidatedField.bind(this));
  }

  _subscribeToCurrentUser() {
    const credentials = this.credentials;

    return new Promise((resolve, reject) => {
      Tracker.autorun((c) => {
        setTimeout(() => {
          c.stop();
          reject('Authentication subscription timeout.');
        }, 2000);

        const subscription = Meteor.subscribe('current-user', credentials);
        if (!subscription.ready()) return;

        resolve(c);
      });
    });
  }

  /**
   * Add an observer to keep tracking whatever the user need an reconection.
   * 
   * It track when the user lost connection or reopen a closed tab.
   * 
   * @return {Promise}
   */
  _addObserverToReconnection() {
    return new Promise((resolve, reject) => {
      Tracker.autorun((c) => {
        if (Meteor.status().connected) {
          this.authenticate();
        }
        resolve();
      });
    });
  }

  _addObserverToValidatedField(prevComp) {
    return new Promise((resolve, reject) => {
      const validationTimeout = setTimeout(() => {
        this.clearCredentials();
        reject('Authentication timeout.');
      }, 2500);

      const didValidate = () => {
        this.loggedIn = true;
        clearTimeout(validationTimeout);
        prevComp.stop();
        resolve();
      };

      Tracker.autorun((c) => {
        const selector = { meetingId: this.meetingID, userId: this.userID };
        const query = Users.find(selector);

        if (query.count() && query.fetch()[0].validated) {
          c.stop();
          didValidate();
        }

        const handle = query.observeChanges({
          changed: (id, fields) => {
            if (id !== this.userID) return;

            if (fields.validated === true) {
              c.stop();
              didValidate();
            }

            if (fields.validated === false) {
              c.stop();
              this.clearCredentials();
              reject('Authentication failed.');
            }
          },
        });
      });

      const credentials = this.credentials;
      callServer('validateAuthToken', credentials);
    });
  }

  fetchLogoutUrl() {
    const url = `/bigbluebutton/api/enter`;

    return fetch(url)
      .then(response => response.json())
      .then(data => Promise.resolve(data.response.logoutURL));
  }
};

let AuthSingleton = new Auth();
export default AuthSingleton;
