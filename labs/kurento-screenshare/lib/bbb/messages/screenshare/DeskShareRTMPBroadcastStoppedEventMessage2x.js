/*
 * 
 */

var inherits = require('inherits');
var OutMessage2x = require('../OutMessage2x');

module.exports = function (C) {
  function DeskShareRTMPBroadcastStoppedEventMessage2x (conferenceName, screenshareConf,
      streamUrl, vw, vh, timestamp) {
    DeskShareRTMPBroadcastStoppedEventMessage2x.super_.call(this, C.DESKSHARE_RTMP_BROADCAST_STOPPED_2x,
        {voiceConf: conferenceName}, {voiceConf: conferenceName});

    this.core.body = {};
    this.core.body[C.CONFERENCE_NAME] = conferenceName;
    this.core.body[C.SCREENSHARE_CONF] = screenshareConf; 
    this.core.body[C.STREAM_URL] = streamUrl;
    this.core.body[C.VIDEO_WIDTH] = vw;
    this.core.body[C.VIDEO_HEIGHT] = vh;
    this.core.body[C.TIMESTAMP] = timestamp;
  };

  inherits(DeskShareRTMPBroadcastStoppedEventMessage2x, OutMessage2x);
  return DeskShareRTMPBroadcastStoppedEventMessage2x;
}
