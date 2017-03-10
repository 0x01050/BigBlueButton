import React from 'react';
import { Router, Route, Redirect, IndexRoute, useRouterHistory } from 'react-router';
import { createHistory } from 'history';

import { joinRouteHandler, authenticatedRouteHandler } from './auth';
import Base from './base';

import LoadingScreen from '/imports/ui/components/loading-screen/component';
import ChatContainer from '/imports/ui/components/chat/container';
import UserListContainer from '/imports/ui/components/user-list/container';

const browserHistory = useRouterHistory(createHistory)({
  basename: Meteor.settings.public.app.basename,
});

export const renderRoutes = () => (
  <Router history={browserHistory}>
    <Route path="/join/:meetingID/:userID/:authToken"
           component={LoadingScreen} onEnter={joinRouteHandler} />
    <Route path="/" component={Base} onEnter={authenticatedRouteHandler} >
      <IndexRoute components={{}} />
      <Route name="users" path="users" components={{ userList: UserListContainer }} />
      <Route name="chat" path="users/chat/:chatID" components={{
        userList: UserListContainer,
        chat: ChatContainer,
      }} />
      <Redirect from="users/chat" to="/users/chat/public" />
    </Route>
    <Redirect from="*" to="/" />
  </Router>
);
