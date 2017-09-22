import React from 'react';
import { createContainer } from 'meteor/react-meteor-data';
import { meetingIsBreakout } from '/imports/ui/components/app/service';
import { makeCall } from '/imports/ui/services/api';
import Meetings from '/imports/api/2.0/meetings';
import Service from './service';
import UserList from './component';

const UserListContainer = (props) => {
  const {
    users,
    currentUser,
    openChats,
    openChat,
    userActions,
    isBreakoutRoom,
    children,
    meeting,
    getAvailableActions,
    normalizeEmojiName,
    isMeetingLocked,
    isPublicChat,
    } = props;

  return (
    <UserList
      users={users}
      meeting={meeting}
      currentUser={currentUser}
      openChats={openChats}
      openChat={openChat}
      isBreakoutRoom={isBreakoutRoom}
      makeCall={makeCall}
      userActions={userActions}
      getAvailableActions={getAvailableActions}
      normalizeEmojiName={normalizeEmojiName}
      isMeetingLocked={isMeetingLocked}
      isPublicChat={isPublicChat}
    >
      {children}
    </UserList>
  );
};

export default createContainer(({ params }) => ({
  users: Service.getUsers(),
  meeting: Meetings.findOne({}),
  currentUser: Service.getCurrentUser(),
  openChats: Service.getOpenChats(params.chatID),
  openChat: params.chatID,
  userActions: Service.userActions,
  isBreakoutRoom: meetingIsBreakout(),
  getAvailableActions: Service.getAvailableActions,
  normalizeEmojiName: Service.normalizeEmojiName,
  isMeetingLocked: Service.isMeetingLocked,
  isPublicChat: Service.isPublicChat,
}), UserListContainer);
