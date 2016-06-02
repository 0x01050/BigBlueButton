import Storage from '/imports/ui/services/storage';

setCredentials = (meeting, user, token) => {
  Storage.set('meetingID', meeting);
  Storage.set('userID', user);
  Storage.set('authToken', token);
};

getCredentials = () => ({
  meetingId: Storage.get('meetingID'),
  requesterUserId: Storage.get('userID'),
  requesterToken: Storage.get('authToken'),
});

getMeeting = () => getCredentials().requesterUserId;

getUser = () => getCredentials().requesterUserId;

getToken = () => getCredentials().requesterUserId;

export default {
  setCredentials,
  getCredentials,
  getMeeting,
  getUser,
  getToken,
};
