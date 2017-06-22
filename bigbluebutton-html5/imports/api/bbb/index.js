import AudioManager from '/imports/api/audio/client/manager';
import Auth from '/imports/ui/services/auth';
import Users from '/imports/api/users';
import Meetings from '/imports/api/meetings';

class htmlBBB {

  getUserId() {
    let userID = Auth.userID;
    return userID;
  }

  getUsername() {
    return Users.findOne({userId: this.getUserId()}).user.name;
  }

  getExtension() {
    let extension = Meetings.findOne().voiceConf;
    return extension;
  }

  getMyUserInfo(callback) {
    const result = {
      myUserID: this.getUserId(),
      myUsername: this.getUsername(),
      myInternalUserID: this.getUserId(),
      myAvatarURL: null,
      myRole: 'getMyRole',
      amIPresenter: 'false',
      voiceBridge: this.getExtension(),
      dialNumber: null,
    };
    return callback(result);
  }

  webRTCCallFailed(inEchoTest, errorcode, cause) {
    AudioManager.webRTCCallFailed(inEchoTest, errorcode, cause);
  }

  webRTCCallStarted(inEchoTest) {
    console.log("FUNCTION WAS CALLED");
    AudioManager.webRTCCallStarted(inEchoTest);
  }


}

window.BBB = new htmlBBB();
