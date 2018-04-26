/*
 * Lucas Fialho Zawacki
 * Paulo Renato Lanzarin
 * (C) Copyright 2017 Bigbluebutton
 *
 */

'use strict'

const C = require('../bbb/messages/Constants');
const MediaHandler = require('../media-handler');
const Messaging = require('../bbb/messages/Messaging');
const moment = require('moment');
const h264_sdp = require('../h264-sdp');
const now = moment();
const MCSApi = require('../mcs-core/lib/media/MCSApiStub');
const config = require('config');
const kurentoIp = config.get('kurentoIp');
const localIpAddress = config.get('localIpAddress');
const FORCE_H264 = config.get('screenshare-force-h264');
const EventEmitter = require('events').EventEmitter;
const Logger = require('../utils/Logger');

// Global MCS endpoints mapping. These hashes maps IDs generated by the mcs-core
// lib to the ones generate in the ScreenshareManager
var sharedScreens = {};
var rtpEndpoints = {};

module.exports = class Screenshare extends EventEmitter {
  constructor(id, bbbgw, voiceBridge, caller = 'caller', vh, vw, meetingId) {
    super();
    this.mcs = new MCSApi();
    this._id = id;
    this._BigBlueButtonGW = bbbgw;
    this._presenterEndpoint = null;
    this._ffmpegEndpoint = null;
    this._voiceBridge = voiceBridge;
    this._meetingId = meetingId;
    this._caller = caller;
    this._streamUrl = "";
    this._vw = vw;
    this._vh = vh;
    this._presenterCandidatesQueue = [];
    this._viewersEndpoint = [];
    this._viewersCandidatesQueue = [];
    this._rtmpBroadcastStarted = false;
  }

  onIceCandidate (candidate, role, callerName) {
    Logger.debug("[screenshare] onIceCandidate", role, callerName, candidate);
    switch (role) {
      case C.SEND_ROLE:
        if (this._presenterEndpoint) {
          try {
            this.flushCandidatesQueue(this._presenterEndpoint, this._presenterCandidatesQueue);
            this.mcs.addIceCandidate(this._presenterEndpoint, candidate);
          } catch (err) {
            Logger.error("[screenshare] ICE candidate could not be added to media controller.", err);
          }
        } else {
          Logger.debug("[screenshare] Pushing ICE candidate to presenter queue");
          this._presenterCandidatesQueue.push(candidate);
        }
      case C.RECV_ROLE:
        let endpoint = this._viewersEndpoint[callerName];
        if (endpoint) {
          try {
            this.flushCandidatesQueue(endpoint, this._viewersCandidatesQueue[callerName]);
            this.mcs.addIceCandidate(endpoint, candidate);
          } catch (err) {
            Logger.error("[screenshare] Viewer ICE candidate could not be added to media controller.", err);
          }
        } else {
          this._viewersCandidatesQueue[callerName] = [];
          Logger.debug("[screenshare] Pushing ICE candidate to viewer queue", callerName);
          this._viewersCandidatesQueue[callerName].push(candidate);
        }
        break;
      default:
        Logger.warn("[screenshare] Unknown role", role);
      }
  }

  flushCandidatesQueue (mediaId, queue) {
    Logger.debug("[screenshare] flushCandidatesQueue", queue);
    if (mediaId) {
      try {
        while(queue.length) {
          let candidate = queue.shift();
          this.mcs.addIceCandidate(mediaId, candidate);
        }
      } catch (err) {
        Logger.error("[screenshare] ICE candidate could not be added to media controller.", err);
      }
    } else {
      Logger.error("[screenshare] No mediaId");
    }
  }

  mediaStateRtp (event) {
    let msEvent = event.event;

    switch (event.eventTag) {
      case "MediaStateChanged":
        break;

      case "MediaFlowOutStateChange":
        Logger.info('[screenshare]', msEvent.type, '[' + msEvent.state? msEvent.state : 'UNKNOWN_STATE' + ']', 'for media session ',  event.id);
        break;

      case "MediaFlowInStateChange":
        Logger.info('[screenshare]', msEvent.type, '[' + msEvent.state? msEvent.state : 'UNKNOWN_STATE' + ']', 'for media session ',  event.id);
        if (msEvent.state === 'FLOWING') {
          this._onRtpMediaFlowing();
        }
        else {
          this._onRtpMediaNotFlowing();
        }
        break;

      default: Logger.warn("[screenshare] Unrecognized event", event);
    }
  }

  mediaStateWebRtc (event, id) {
    let msEvent = event.event;

    switch (event.eventTag) {
      case "OnIceCandidate":
        let candidate = msEvent.candidate;
        Logger.debug('[screenshare] Received ICE candidate from mcs-core for media session', event.id, '=>', candidate, "for connection", id);

        this._BigBlueButtonGW.publish(JSON.stringify({
          connectionId: id,
          type: C.SCREENSHARE_APP,
          id : 'iceCandidate',
          cameraId: this._id,
          candidate : candidate
        }), C.FROM_SCREENSHARE);

        break;

      case "MediaStateChanged":
        break;

      case "MediaFlowOutStateChange":
      case "MediaFlowInStateChange":
        Logger.info('[screenshare]', msEvent.type, '[' + msEvent.state? msEvent.state : 'UNKNOWN_STATE' + ']', 'for media session',  event.id);
        break;

      default: Logger.warn("[screenshare] Unrecognized event", event);
    }
  }

  serverState (event) {
    switch (event && event.eventTag) {
      case C.MEDIA_SERVER_OFFLINE:
        Logger.error("[screenshare] Screenshare provider received MEDIA_SERVER_OFFLINE event");
        this.emit(C.MEDIA_SERVER_OFFLINE, event);
        break;
      default:
        Logger.warn("[screenshare] Unknown server state", event);
    }
  }

  async start (sessionId, connectionId, sdpOffer, callerName, role, callback) {
    // Force H264 on Firefox and Chrome
    if (FORCE_H264) {
      sdpOffer = h264_sdp.transform(sdpOffer);
    }

    Logger.info("[screenshare] Starting session", this._voiceBridge + '-' + role);
    if (!this.userId) {
      try {
        this.userId = await this.mcs.join(this._meetingId, 'SFU', {});
        Logger.info("[screenshare] MCS Join for", this._id, "returned", this.userId);

      }
      catch (error) {
        Logger.error("[screenshare] MCS Join returned error =>", error);
        return callback(error);
      }
    }

    if (role === C.RECV_ROLE) {
      this._startViewer(connectionId, this._voiceBridge, sdpOffer, callerName, this._presenterEndpoint, callback)
      return;
    }

    if (role === C.SEND_ROLE) {
      try {
        const retSource = await this.mcs.publish(this.userId, this._meetingId, 'WebRtcEndpoint', {descriptor: sdpOffer});

        this._presenterEndpoint = retSource.sessionId;
        sharedScreens[this._voiceBridge] = this._presenterEndpoint;
        let presenterSdpAnswer = retSource.answer;
        this.flushCandidatesQueue(this._presenterEndpoint, this._presenterCandidatesQueue);

        this.mcs.on('MediaEvent' + this._presenterEndpoint, (event) => {
          this.mediaStateWebRtc(event, this._id)
        });

        Logger.info("[screenshare] MCS publish for user", this.userId, "returned", this._presenterEndpoint);

        let sendVideoPort = MediaHandler.getVideoPort();
        let rtpSdpOffer = MediaHandler.generateVideoSdp(localIpAddress, sendVideoPort);

        const retRtp = await this.mcs.subscribe(this.userId, sharedScreens[this._voiceBridge], 'RtpEndpoint', {descriptor: rtpSdpOffer});

        this._ffmpegEndpoint = retRtp.sessionId;
        rtpEndpoints[this._voiceBridge] = this._ffmpegEndpoint;

        let recvVideoPort = retRtp.answer.match(/m=video\s(\d*)/)[1];
        this._rtpParams = MediaHandler.generateTranscoderParams(kurentoIp, localIpAddress,
          sendVideoPort, recvVideoPort, this._meetingId, "stream_type_video", C.RTP_TO_RTMP, "copy", this._caller, this._voiceBridge);

        this.mcs.on('MediaEvent' + this._ffmpegEndpoint, this.mediaStateRtp.bind(this));

        Logger.info("[screenshare] MCS subscribe for user", this.userId, "returned", this._ffmpegEndpoint);

        return callback(null, presenterSdpAnswer);

      }
      catch (err) {
        Logger.error("[screenshare] MCS publish returned error =>", err);
        return callback(err);
      }
      finally {
        this.mcs.once('ServerState' + this._presenterEndpoint, this.serverState.bind(this));
      }
    }
  }

  async _startViewer(connectionId, voiceBridge, sdpOffer, callerName, presenterEndpoint, callback) {
    Logger.info("[screenshare] Starting viewer", callerName, "for voiceBridge", this._voiceBridge);
    // TODO refactor the callback handling
    let _callback = function(){};
    let sdpAnswer;

    if (FORCE_H264) {
      sdpOffer = h264_sdp.transform(sdpOffer);
    }

    this._viewersCandidatesQueue[callerName] = [];

    try {
      const retSource = await this.mcs.subscribe(this.userId, sharedScreens[voiceBridge], 'WebRtcEndpoint', {descriptor: sdpOffer});

      this._viewersEndpoint[callerName] = retSource.sessionId;
      sdpAnswer = retSource.answer;
      this.flushCandidatesQueue(this._viewersEndpoint[callerName], this._viewersCandidatesQueue[callerName]);

      this.mcs.on('MediaEvent' + this._viewersEndpoint[callerName], (event) => {
        this.mediaStateWebRtc(event, connectionId);
      });

      this._BigBlueButtonGW.publish(JSON.stringify({
        connectionId: connectionId,
        id: "startResponse",
        type: C.SCREENSHARE_APP,
        role: C.RECV_ROLE,
        sdpAnswer: sdpAnswer,
        response: "accepted"
      }), C.FROM_SCREENSHARE);

      Logger.info("[screenshare] MCS subscribe returned for user", this.userId, "returned", this._viewersEndpoint[callerName], "at callername", callerName);
    }
    catch (err) {
      Logger.error("[screenshare] MCS publish returned error =>", err);
      return _callback(err);
    }
  }

  stop () {
    return new Promise(async (resolve, reject) => {
      try {
        Logger.info('[screnshare] Stopping and releasing endpoints for MCS user', this.userId);

        if (this._presenterEndpoint) {
          await this._stopScreensharing();
          Logger.info("[screenshare] Leaving mcs room");
          await this.mcs.leave(this._meetingId, this.userId);
          delete sharedScreens[this._presenterEndpoint];
          this._candidatesQueue = null;
          this._presenterEndpoint = null;
          this._ffmpegEndpoint = null;
          resolve();
        }
      }
      catch (err) {
        Logger.error('[screenshare] MCS returned an error when trying to leave =>', err);
        resolve();
      }
    });
  }

  _stopScreensharing() {
    return new Promise((resolve, reject) => {
      try {
        let strm = Messaging.generateStopTranscoderRequestMessage(this._meetingId, this._meetingId);

        this._BigBlueButtonGW.publish(strm, C.TO_BBB_TRANSCODE_SYSTEM_CHAN, function(error) {});

        // Interoperability: capturing 1.1 stop_transcoder_reply messages
        this._BigBlueButtonGW.on(C.STOP_TRANSCODER_REPLY, async (payload) => {
          let meetingId = payload[C.MEETING_ID];
          if(this._meetingId === meetingId) {
            await this._stopRtmpBroadcast(meetingId);
            return resolve();
          }
        });

        // Capturing stop transcoder responses from the 2x model
        this._BigBlueButtonGW.on(C.STOP_TRANSCODER_RESP_2x, async (payload) => {
          Logger.info(payload);
          let meetingId = payload[C.MEETING_ID_2x];
          if(this._meetingId === meetingId) {
            await this._stopRtmpBroadcast(meetingId);
            return resolve();
          }
        });
      }
      catch (err) {
        Logger.error(err);
        resolve();
      }
    });
  }

  _onRtpMediaFlowing() {
    if (!this._rtmpBroadcastStarted) {
      Logger.info("[screenshare] RTP Media FLOWING for meeting", this._meetingId);
      let strm = Messaging.generateStartTranscoderRequestMessage(this._meetingId, this._meetingId, this._rtpParams);

      // Interoperability: capturing 1.1 start_transcoder_reply messages
      this._BigBlueButtonGW.once(C.START_TRANSCODER_REPLY, (payload) => {
        let meetingId = payload[C.MEETING_ID];
        let output = payload["params"].output;
        this._startRtmpBroadcast(meetingId, output);
      });

      // Capturing stop transcoder responses from the 2x model
      this._BigBlueButtonGW.once(C.START_TRANSCODER_RESP_2x, (payload) => {
        let meetingId = payload[C.MEETING_ID_2x];
        let output = payload["params"].output;
        this._startRtmpBroadcast(meetingId, output);
      });

      this._BigBlueButtonGW.publish(strm, C.TO_BBB_TRANSCODE_SYSTEM_CHAN, function(error) {});
    }
  };

  _stopRtmpBroadcast (meetingId) {
    return new Promise((resolve, reject) => {
      Logger.info("[screenshare] _stopRtmpBroadcast for meeting", meetingId);
      let timestamp = now.format('hhmmss');
      let dsrstom = Messaging.generateScreenshareRTMPBroadcastStoppedEvent2x(this._voiceBridge,
        this._voiceBridge, this._streamUrl, this._vw, this._vh, timestamp);
      this._BigBlueButtonGW.publish(dsrstom, C.FROM_VOICE_CONF_SYSTEM_CHAN);
      resolve();
    });
  }

  _startRtmpBroadcast (meetingId, output) {
    Logger.info("[screenshare] _startRtmpBroadcast for meeting", + meetingId);
    if (this._meetingId === meetingId) {
      let timestamp = now.format('hhmmss');
      this._streamUrl = MediaHandler.generateStreamUrl(localIpAddress, meetingId, output);
      let dsrbstam = Messaging.generateScreenshareRTMPBroadcastStartedEvent2x(this._voiceBridge,
          this._voiceBridge, this._streamUrl, this._vw, this._vh, timestamp);

      this._BigBlueButtonGW.publish(dsrbstam, C.FROM_VOICE_CONF_SYSTEM_CHAN, function(error) {});
      this._rtmpBroadcastStarted = true;
    }
  }

  _onRtpMediaNotFlowing() {
    Logger.warn("[screenshare] TODO RTP NOT_FLOWING");
  }

  async stopViewer(id) {
    let viewer = this._viewersEndpoint[id];
    Logger.info('[screenshare] Releasing endpoints for', viewer);

    if (viewer) {
      try {
        await this.mcs.unsubscribe(this.userId, this.viewer);
        this._viewersCandidatesQueue[id] = null;
        this._viewersEndpoint[id] = null;
        return;
      }
      catch (err) {
        Logger.error('[screenshare] MCS returned error when trying to unsubscribe', err);
        return;
      }
    }
  }
};
