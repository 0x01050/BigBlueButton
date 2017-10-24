import Auth from '/imports/ui/services/auth';
import { check } from 'meteor/check';
import NotificationService from '/imports/ui/services/notification/notificationService';

/**
 * Send the request to the server via Meteor.call and don't treat errors.
 *
 * @param {string} name
 * @param {any} args
 * @see https://docs.meteor.com/api/methods.html#Meteor-call
 * @return {Promise}
 */
export function makeCall(name, ...args) {
  check(name, String);

  const credentials = Auth.credentials;

  return new Promise((resolve, reject) => {
    Meteor.call(name, credentials, ...args, (error, result) => {
      if (error) {
        reject(error);
      }

      resolve(result);
    });
  });
}

/**
 * Send the request to the server via Meteor.call and treat the error to a default callback.
 *
 * @param {string} name
 * @param {any} args
 * @see https://docs.meteor.com/api/methods.html#Meteor-call
 * @return {Promise}
 */
export function call(name, ...args) {
  return makeCall(name, ...args).catch((e) => {
    NotificationService.add({ notification: `Error while executing ${name}` });
    throw e;
  });
}

export function log(type = 'error', message, ...args) {
  const credentials = Auth.credentials;
  const userInfo = window.navigator;
  const clientInfo = {
    language: userInfo.language,
    userAgent: userInfo.userAgent,
    screenSize: { width: screen.width, height: screen.height },
    windowSize: { width: window.innerWidth, height: window.innerHeight },
    bbbVersion: Meteor.settings.public.app.bbbServerVersion,
    location: window.location.href,
  };

  const messageOrStack = message.stack || message.message || message.toString();

  console.debug(`CLIENT LOG (${type.toUpperCase()}): `, messageOrStack, ...args);

  Meteor.call('logClient', type, messageOrStack, {
    clientInfo,
    credentials,
    ...args,
  });
}
