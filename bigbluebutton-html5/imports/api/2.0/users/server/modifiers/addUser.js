import { check } from 'meteor/check';
import Logger from '/imports/startup/server/logger';
import Users from '/imports/api/2.0/users';

import stringHash from 'string-hash';
import flat from 'flat';

import addVoiceUser from '/imports/api/2.0/voice-users/server/modifiers/addVoiceUser';

const COLOR_LIST = [
  '#d32f2f', '#c62828', '#b71c1c', '#d81b60', '#c2185b', '#ad1457', '#880e4f',
  '#8e24aa', '#7b1fa2', '#6a1b9a', '#4a148c', '#5e35b1', '#512da8', '#4527a0',
  '#311b92', '#3949ab', '#303f9f', '#283593', '#1a237e', '#1976d2', '#1565c0',
  '#0d47a1', '#0277bd', '#01579b', '#00838f', '#006064', '#00796b', '#00695c',
  '#004d40', '#2e7d32', '#1b5e20', '#33691e', '#827717', '#bf360c', '#6d4c41',
  '#5d4037', '#4e342e', '#3e2723', '#757575', '#616161', '#424242', '#212121',
  '#546e7a', '#455a64', '#37474f', '#263238',
];

export default function addUser(meetingId, user) {
  check(meetingId, String);

  check(user, {
    intId: String,
    extId: String,
    name: String,
    role: String,
    guest: Boolean,
    authed: Boolean,
    waitingForAcceptance: Boolean,
    emoji: String,
    presenter: Boolean,
    locked: Boolean,
    avatar: String,
  });

  const userId = user.intId;
  check(userId, String);

  const selector = {
    meetingId,
    userId,
  };

  const USER_CONFIG = Meteor.settings.public.user;
  const ROLE_MODERATOR = USER_CONFIG.role_moderator;
  const ROLE_VIEWER = USER_CONFIG.role_viewer;
  const APP_CONFIG = Meteor.settings.public.app;
  const ALLOW_HTML5_MODERATOR = APP_CONFIG.allowHTML5Moderator;

  // override moderator status of html5 client users, depending on a system flag
  const dummyUser = Users.findOne(selector);
  let userRole = user.role;

  if (
    dummyUser &&
    dummyUser.clientType === 'HTML5' &&
    userRole === ROLE_MODERATOR &&
    !ALLOW_HTML5_MODERATOR
  ) {
    userRole = ROLE_VIEWER;
  }

  const userRoles = [
    'viewer',
    user.presenter ? 'presenter' : false,
    userRole === ROLE_MODERATOR ? 'moderator' : false,
  ].filter(Boolean);

  /* While the akka-apps dont generate a color we just pick one
    from a list based on the userId */
  const color = COLOR_LIST[stringHash(user.intId) % COLOR_LIST.length];

  const modifier = {
    $set: Object.assign(
      {
        meetingId,
        connectionStatus: 'online',
        roles: userRoles,
        sortName: user.name.trim().toLowerCase(),
        color,
      },
      flat(user),
    ),
  };

  addVoiceUser(meetingId, {
    voiceUserId: '',
    intId: userId,
    callerName: user.name,
    callerNum: '',
    muted: false,
    talking: false,
    callingWith: '',
    listenOnly: false,
    voiceConf: '',
  });

  const cb = (err, numChanged) => {
    if (err) {
      return Logger.error(`Adding user to collection: ${err}`);
    }

    // TODO: Do we really need to request the stun/turn everytime?
    // requestStunTurn(meetingId, userId);

    const { insertedId } = numChanged;
    if (insertedId) {
      return Logger.info(`Added user id=${userId} meeting=${meetingId}`);
    }

    return Logger.info(`Upserted user id=${userId} meeting=${meetingId}`);
  };

  return Users.upsert(selector, modifier, cb);
}
