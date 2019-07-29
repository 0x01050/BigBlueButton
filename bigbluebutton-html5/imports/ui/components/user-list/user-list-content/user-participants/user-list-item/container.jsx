import React from 'react';
import { withTracker } from 'meteor/react-meteor-data';
import Users from '/imports/api/users';
import Breakouts from '/imports/api/breakouts';
import Meetings from '/imports/api/meetings';
import Auth from '/imports/ui/services/auth';
import mapUser from '/imports/ui/services/user/mapUser';
import UserListItem from './component';
import service from '/imports/ui/components/user-list/service';

const UserListItemContainer = props => <UserListItem {...props} />;

export default withTracker(({ userId }) => {
  const findUserInBreakout = Breakouts.findOne({ 'joinedUsers.userId': new RegExp(`^${userId}`) });
  const breakoutSequence = (findUserInBreakout || {}).sequence;
  const Meeting = Meetings.findOne({ MeetingId: Auth.meetingID }, { fields: { meetingProp: 1 } });
  return {
    user: mapUser(Users.findOne({ userId })),
    userInBreakout: !!findUserInBreakout,
    breakoutSequence,
    currentUser: Users.findOne({ userId: Auth.userID }),
    meetignIsBreakout: Meeting && Meeting.meetingProp.isBreakout,
    isMeteorConnected: Meteor.status().connected,
  };
})(UserListItemContainer);
