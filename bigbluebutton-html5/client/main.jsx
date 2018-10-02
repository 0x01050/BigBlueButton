/* eslint no-unused-vars: 0 */
import React from 'react';
import { Meteor } from 'meteor/meteor';
import { render } from 'react-dom';
import renderRoutes from '/imports/startup/client/routes';
import logger from '/imports/startup/client/logger';
import LoadingScreen from '/imports/ui/components/loading-screen/component';
import { joinRouteHandler_2 } from '/imports/startup/client/auth';
import Base from '/imports/startup/client/base';

Meteor.startup(() => {
  render(<LoadingScreen />, document.getElementById('app'));

  // Logs all uncaught exceptions to the client logger
  window.addEventListener('error', (e) => {
    const stack = e.error.stack;
    let message = e.error.toString();

    // Checks if stack includes the message, if not add the two together.
    (stack.includes(message)) ? message = stack : message += `\n${stack}`;
    logger.error(message);
  });

  console.log('a');

  // TODO make this a Promise
  joinRouteHandler_2((value, error) => {
    console.error('__' + value);
    render(<Base />, document.getElementById('app'));
  });
});
