import Breakouts from '/imports/api/breakouts';
import Meetings from '/imports/api/meetings';
import { makeCall } from '/imports/ui/services/api';
import Auth from '/imports/ui/services/auth';
import HumanizeSeconds from '/imports/utils/humanizeSeconds';
import Users from '/imports/api/users';
import mapUser from '/imports/ui/services/user/mapUser';

const breakouts = () => {
  const BreakoutRooms = Breakouts.find({ parentMeetingId: Auth.meetingID }, { sort: { sequence: 1 } }).fetch();

  return BreakoutRooms;
};

const breakoutRoomUser = (breakoutId) => {
  const breakoutRooms = breakouts();
  const breakoutRoom = breakoutRooms.filter(breakout => breakout.breakoutId === breakoutId).shift();
  const breakoutUser = breakoutRoom.users.filter(user => user.userId === Auth.userID).shift();
  return breakoutUser;
};


const endAllBreakouts = () => {
  makeCall('endAllBreakouts');
};

const requestJoinURL = (breakoutId) => {
  makeCall('requestJoinURL', {
    breakoutId,
  });
};

const transferUserToMeeting = (fromMeetingId, toMeetingId) => makeCall('transferUser', fromMeetingId, toMeetingId);

const transferToBreakout = (fromMeetingId, breakoutId) => {
  const breakoutRooms = breakouts();
  const breakoutRoom = breakoutRooms.filter(breakout => breakout.breakoutId === breakoutId).shift();
  const breakoutMeeting = Meetings.findOne({
    $and: [
      { 'breakoutProps.sequence': breakoutRoom.sequence },
      { 'breakoutProps.parentId': breakoutRoom.parentMeetingId },
      { 'meetingProp.isBreakout': true },
    ],
  });
  transferUserToMeeting(fromMeetingId, breakoutMeeting.meetingId);
};

const isPresenter = () => {
  const User = Users.findOne({ intId: Auth.userID });
  const mappedUser = mapUser(User);
  return mappedUser.isPresenter;
};

export default {
  breakouts,
  endAllBreakouts,
  requestJoinURL,
  breakoutRoomUser,
  transferUserToMeeting,
  transferToBreakout,
  meetingId: Auth.meetingID,
  isPresenter,
};
