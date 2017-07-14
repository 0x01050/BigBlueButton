package org.bigbluebutton.core

import org.bigbluebutton.core.bus._
import org.bigbluebutton.core.api._

import scala.collection.JavaConversions._
import akka.actor.ActorSystem
import org.bigbluebutton.common.messages.IBigBlueButtonMessage
import org.bigbluebutton.common.messages.PubSubPingMessage
import org.bigbluebutton.messages._
import akka.event.Logging
import org.bigbluebutton.SystemConfiguration
import org.bigbluebutton.core.models.{ GuestPolicyType, Roles }

import scala.collection.JavaConverters

class BigBlueButtonInGW(
    val system: ActorSystem,
    eventBus: IncomingEventBus,
    bbbMsgBus: BbbMsgRouterEventBus,
    outGW: OutMessageGateway) extends IBigBlueButtonInGW with SystemConfiguration {

  val log = Logging(system, getClass)
  val bbbActor = system.actorOf(BigBlueButtonActor.props(system, eventBus, bbbMsgBus, outGW), "bigbluebutton-actor")
  eventBus.subscribe(bbbActor, meetingManagerChannel)

  /** For OLD Messaged **/
  eventBus.subscribe(bbbActor, "meeting-manager")

  def handleBigBlueButtonMessage(message: IBigBlueButtonMessage) {
    message match {
      case msg: PubSubPingMessage => {
        eventBus.publish(
          BigBlueButtonEvent("meeting-manager", new PubSubPing(msg.payload.system, msg.payload.timestamp)))
      }

      case msg: CreateMeetingRequest => {
        val policy = msg.payload.guestPolicy.toUpperCase() match {
          case "ALWAYS_ACCEPT" => GuestPolicyType.ALWAYS_ACCEPT
          case "ALWAYS_DENY" => GuestPolicyType.ALWAYS_DENY
          case "ASK_MODERATOR" => GuestPolicyType.ASK_MODERATOR
          //default
          case undef => GuestPolicyType.ASK_MODERATOR
        }
        /*
        val mProps = new MeetingProperties(
          msg.payload.id,
          msg.payload.externalId,
          msg.payload.parentId,
          msg.payload.name,
          msg.payload.record,
          msg.payload.voiceConfId,
          msg.payload.voiceConfId + "-DESKSHARE", // WebRTC Desktop conference id
          msg.payload.durationInMinutes,
          msg.payload.autoStartRecording,
          msg.payload.allowStartStopRecording,
          msg.payload.webcamsOnlyForModerator,
          msg.payload.moderatorPassword,
          msg.payload.viewerPassword,
          msg.payload.createTime,
          msg.payload.createDate,
          red5DeskShareIP, red5DeskShareApp,
          msg.payload.isBreakout,
          msg.payload.sequence,
          mapAsScalaMap(msg.payload.metadata).toMap, // Convert to scala immutable map
          policy
        )

        eventBus.publish(BigBlueButtonEvent("meeting-manager", new CreateMeeting(msg.payload.id, mProps)))
        */
      }
    }
  }

  def handleJsonMessage(json: String) {
    JsonMessageDecoder.decode(json) match {
      case Some(validMsg) => forwardMessage(validMsg)
      case None => log.error("Unhandled json message: {}", json)
    }
  }

  def forwardMessage(msg: InMessage) = {
    msg match {
      case m: BreakoutRoomsListMessage => eventBus.publish(BigBlueButtonEvent(m.meetingId, m))
      case m: CreateBreakoutRooms => eventBus.publish(BigBlueButtonEvent(m.meetingId, m))
      case m: RequestBreakoutJoinURLInMessage => eventBus.publish(BigBlueButtonEvent(m.meetingId, m))
      case m: TransferUserToMeetingRequest => eventBus.publish(BigBlueButtonEvent(m.meetingId, m))
      case m: EndAllBreakoutRooms => eventBus.publish(BigBlueButtonEvent(m.meetingId, m))
      case _ => log.error("Unhandled message: {}", msg)
    }
  }

  def destroyMeeting(meetingID: String) {
    forwardMessage(new EndAllBreakoutRooms(meetingID))
    eventBus.publish(
      BigBlueButtonEvent(
        "meeting-manager",
        new DestroyMeeting(
          meetingID)))
  }

  def getAllMeetings(meetingID: String) {
    eventBus.publish(BigBlueButtonEvent("meeting-manager", new GetAllMeetingsRequest("meetingId")))
  }

  def isAliveAudit(aliveId: String) {
    eventBus.publish(BigBlueButtonEvent("meeting-manager", new KeepAliveMessage(aliveId)))
  }

  def lockSettings(meetingID: String, locked: java.lang.Boolean,
    lockSettings: java.util.Map[String, java.lang.Boolean]) {
  }

  def statusMeetingAudit(meetingID: String) {

  }

  def endMeeting(meetingId: String) {
    eventBus.publish(BigBlueButtonEvent(meetingId, new EndMeeting(meetingId)))
  }

  def endAllMeetings() {

  }

  def activityResponse(meetingId: String) {
    eventBus.publish(BigBlueButtonEvent(meetingId, new ActivityResponse(meetingId)))
  }

  /**
   * ***********************************************************
   * Message Interface for Users
   * ***********************************************************
   */
  def validateAuthToken(meetingId: String, userId: String, token: String, correlationId: String, sessionId: String) {
    eventBus.publish(BigBlueButtonEvent(meetingId, new ValidateAuthToken(meetingId, userId, token, correlationId, sessionId)))
  }

  def registerUser(meetingID: String, userID: String, name: String, role: String, extUserID: String,
    authToken: String, avatarURL: String, guest: java.lang.Boolean, authed: java.lang.Boolean): Unit = {
    val userRole = if (role == "MODERATOR") Roles.MODERATOR_ROLE else Roles.VIEWER_ROLE
    eventBus.publish(BigBlueButtonEvent(meetingID, new RegisterUser(meetingID, userID, name, userRole,
      extUserID, authToken, avatarURL, guest, authed)))
  }

  def sendLockSettings(meetingID: String, userId: String, settings: java.util.Map[String, java.lang.Boolean]) {
    // Convert java.util.Map to scala.collection.immutable.Map
    // settings.mapValues -> convaert java Map to scala mutable Map
    // v => v.booleanValue() -> convert java Boolean to Scala Boolean
    // toMap -> converts from scala mutable map to scala immutable map
    val s = settings.mapValues(v => v.booleanValue() /* convert java Boolean to Scala Boolean */ ).toMap
    val disableCam = s.getOrElse("disableCam", false)
    val disableMic = s.getOrElse("disableMic", false)
    val disablePrivChat = s.getOrElse("disablePrivateChat", false)
    val disablePubChat = s.getOrElse("disablePublicChat", false)
    val lockedLayout = s.getOrElse("lockedLayout", false)
    val lockOnJoin = s.getOrElse("lockOnJoin", false)
    val lockOnJoinConfigurable = s.getOrElse("lockOnJoinConfigurable", false)

    val permissions = new Permissions(disableCam = disableCam,
      disableMic = disableMic,
      disablePrivChat = disablePrivChat,
      disablePubChat = disablePubChat,
      lockedLayout = lockedLayout,
      lockOnJoin = lockOnJoin,
      lockOnJoinConfigurable = lockOnJoinConfigurable)

    eventBus.publish(BigBlueButtonEvent(meetingID, new SetLockSettings(meetingID, userId, permissions)))
  }

  def initLockSettings(meetingID: String, settings: java.util.Map[String, java.lang.Boolean]) {
    // Convert java.util.Map to scala.collection.immutable.Map
    // settings.mapValues -> convert java Map to scala mutable Map
    // v => v.booleanValue() -> convert java Boolean to Scala Boolean
    // toMap -> converts from scala mutable map to scala immutable map
    val s = settings.mapValues(v => v.booleanValue() /* convert java Boolean to Scala Boolean */ ).toMap
    val disableCam = s.getOrElse("disableCam", false)
    val disableMic = s.getOrElse("disableMic", false)
    val disablePrivChat = s.getOrElse("disablePrivateChat", false)
    val disablePubChat = s.getOrElse("disablePublicChat", false)
    val lockedLayout = s.getOrElse("lockedLayout", false)
    val lockOnJoin = s.getOrElse("lockOnJoin", false)
    val lockOnJoinConfigurable = s.getOrElse("lockOnJoinConfigurable", false)
    val permissions = new Permissions(disableCam = disableCam,
      disableMic = disableMic,
      disablePrivChat = disablePrivChat,
      disablePubChat = disablePubChat,
      lockedLayout = lockedLayout,
      lockOnJoin = lockOnJoin,
      lockOnJoinConfigurable = lockOnJoinConfigurable)

    eventBus.publish(BigBlueButtonEvent(meetingID, new InitLockSettings(meetingID, permissions)))
  }

  def initAudioSettings(meetingID: String, requesterID: String, muted: java.lang.Boolean) {
    eventBus.publish(BigBlueButtonEvent(meetingID, new InitAudioSettings(meetingID, requesterID, muted.booleanValue())))
  }

  def getLockSettings(meetingId: String, userId: String) {
    eventBus.publish(BigBlueButtonEvent(meetingId, new GetLockSettings(meetingId, userId)))
  }

  def lockUser(meetingId: String, requesterID: String, lock: Boolean, userId: String) {
    eventBus.publish(BigBlueButtonEvent(meetingId, new LockUserRequest(meetingId, requesterID, userId, lock)))
  }

  def setRecordingStatus(meetingId: String, userId: String, recording: java.lang.Boolean) {
    eventBus.publish(BigBlueButtonEvent(meetingId, new SetRecordingStatus(meetingId, userId, recording.booleanValue())))
  }

  def getRecordingStatus(meetingId: String, userId: String) {
    eventBus.publish(BigBlueButtonEvent(meetingId, new GetRecordingStatus(meetingId, userId)))
  }

  // Users
  def userEmojiStatus(meetingId: String, userId: String, emojiStatus: String) {
    eventBus.publish(BigBlueButtonEvent(meetingId, new UserEmojiStatus(meetingId, userId, emojiStatus)))
  }

  def ejectUserFromMeeting(meetingId: String, userId: String, ejectedBy: String) {

  }

  def logoutEndMeeting(meetingId: String, userId: String) {
    eventBus.publish(BigBlueButtonEvent(meetingId, new LogoutEndMeeting(meetingId, userId)))
  }

  def shareWebcam(meetingId: String, userId: String, stream: String) {
    eventBus.publish(BigBlueButtonEvent(meetingId, new UserShareWebcam(meetingId, userId, stream)))
  }

  def unshareWebcam(meetingId: String, userId: String, stream: String) {
    eventBus.publish(BigBlueButtonEvent(meetingId, new UserUnshareWebcam(meetingId, userId, stream)))
  }

  def setUserStatus(meetingID: String, userID: String, status: String, value: Object) {
    eventBus.publish(BigBlueButtonEvent(meetingID, new ChangeUserStatus(meetingID, userID, status, value)))
  }

  def setUserRole(meetingID: String, userID: String, role: String) {

  }

  def getUsers(meetingID: String, requesterID: String) {
    eventBus.publish(BigBlueButtonEvent(meetingID, new GetUsers(meetingID, requesterID)))
  }

  def userLeft(meetingID: String, userID: String, sessionId: String): Unit = {
    eventBus.publish(BigBlueButtonEvent(meetingID, new UserLeaving(meetingID, userID, sessionId)))
  }

  def userJoin(meetingID: String, userID: String, authToken: String): Unit = {
    eventBus.publish(BigBlueButtonEvent(meetingID, new UserJoining(meetingID, userID, authToken)))
  }

  def checkIfAllowedToShareDesktop(meetingID: String, userID: String): Unit = {
    eventBus.publish(BigBlueButtonEvent(meetingID, AllowUserToShareDesktop(meetingID: String,
      userID: String)))
  }

  def assignPresenter(meetingID: String, newPresenterID: String, newPresenterName: String, assignedBy: String): Unit = {
    eventBus.publish(BigBlueButtonEvent(meetingID, new AssignPresenter(meetingID, newPresenterID, newPresenterName, assignedBy)))
  }

  def getCurrentPresenter(meetingID: String, requesterID: String): Unit = {
    // do nothing
  }

  def userConnectedToGlobalAudio(voiceConf: String, userid: String, name: String) {
    // we are required to pass the meeting_id as first parameter (just to satisfy trait)
    // but it's not used anywhere. That's why we pass voiceConf twice instead
    eventBus.publish(BigBlueButtonEvent(voiceConf, new UserConnectedToGlobalAudio(voiceConf, voiceConf, userid, name)))
  }

  def userDisconnectedFromGlobalAudio(voiceConf: String, userid: String, name: String) {
    // we are required to pass the meeting_id as first parameter (just to satisfy trait)
    // but it's not used anywhere. That's why we pass voiceConf twice instead
    eventBus.publish(BigBlueButtonEvent(voiceConf, new UserDisconnectedFromGlobalAudio(voiceConf, voiceConf, userid, name)))
  }

  /**
   * ***********************************************************************
   * Message Interface for Guest
   * *******************************************************************
   */

  def getGuestPolicy(meetingId: String, requesterId: String) {
    eventBus.publish(BigBlueButtonEvent(meetingId, new GetGuestPolicy(meetingId, requesterId)))
  }

  def setGuestPolicy(meetingId: String, guestPolicy: String, requesterId: String) {
    val policy = guestPolicy.toUpperCase() match {
      case "ALWAYS_ACCEPT" => GuestPolicyType.ALWAYS_ACCEPT
      case "ALWAYS_DENY" => GuestPolicyType.ALWAYS_DENY
      case "ASK_MODERATOR" => GuestPolicyType.ASK_MODERATOR
      //default
      case undef => GuestPolicyType.ASK_MODERATOR
    }
    eventBus.publish(BigBlueButtonEvent(meetingId, new SetGuestPolicy(meetingId, policy, requesterId)))
  }

  def responseToGuest(meetingId: String, userId: String, response: java.lang.Boolean, requesterId: String) {
    eventBus.publish(BigBlueButtonEvent(meetingId, new RespondToGuest(meetingId, userId, response, requesterId)))
  }

  /**
   * ***********************************************************************
   * Message Interface for Layout
   * *******************************************************************
   */

  def getCurrentLayout(meetingID: String, requesterID: String) {
    eventBus.publish(BigBlueButtonEvent(meetingID, new GetCurrentLayoutRequest(meetingID, requesterID)))
  }

  def broadcastLayout(meetingID: String, requesterID: String, layout: String) {
    eventBus.publish(BigBlueButtonEvent(meetingID, new BroadcastLayoutRequest(meetingID, requesterID, layout)))
  }

  def lockLayout(meetingId: String, setById: String, lock: Boolean, viewersOnly: Boolean, layout: String) {
    if (layout != null) {
      eventBus.publish(BigBlueButtonEvent(meetingId, new LockLayoutRequest(meetingId, setById, lock, viewersOnly, Some(layout))))
    } else {
      eventBus.publish(BigBlueButtonEvent(meetingId, new LockLayoutRequest(meetingId, setById, lock, viewersOnly, None)))
    }

  }

  /**
   * *******************************************************************
   * Message Interface for Voice
   * *****************************************************************
   */

  def muteAllExceptPresenter(meetingID: String, requesterID: String, mute: java.lang.Boolean) {
    eventBus.publish(BigBlueButtonEvent(meetingID, new MuteAllExceptPresenterRequest(meetingID, requesterID, mute)))
  }

  def muteAllUsers(meetingID: String, requesterID: String, mute: java.lang.Boolean) {
    eventBus.publish(BigBlueButtonEvent(meetingID, new MuteMeetingRequest(meetingID, requesterID, mute)))
  }

  def isMeetingMuted(meetingID: String, requesterID: String) {
    eventBus.publish(BigBlueButtonEvent(meetingID, new IsMeetingMutedRequest(meetingID, requesterID)))
  }

  def muteUser(meetingID: String, requesterID: String, userID: String, mute: java.lang.Boolean) {
    eventBus.publish(BigBlueButtonEvent(meetingID, new MuteUserRequest(meetingID, requesterID, userID, mute)))
  }

  def lockMuteUser(meetingID: String, requesterID: String, userID: String, lock: java.lang.Boolean) {
    eventBus.publish(BigBlueButtonEvent(meetingID, new LockUserRequest(meetingID, requesterID, userID, lock)))
  }

  def ejectUserFromVoice(meetingId: String, userId: String, ejectedBy: String) {
    eventBus.publish(BigBlueButtonEvent(meetingId, new EjectUserFromVoiceRequest(meetingId, userId, ejectedBy)))
  }

  def voiceUserJoined(voiceConfId: String, voiceUserId: String, userId: String, callerIdName: String,
    callerIdNum: String, muted: java.lang.Boolean, avatarURL: String, talking: java.lang.Boolean) {
    eventBus.publish(BigBlueButtonEvent(voiceConfId, new UserJoinedVoiceConfMessage(voiceConfId, voiceUserId, userId, userId, callerIdName,
      callerIdNum, muted, talking, avatarURL, false /*hardcode listenOnly to false as the message for listenOnly is ConnectedToGlobalAudio*/ )))
  }

  def voiceUserLeft(voiceConfId: String, voiceUserId: String) {
    eventBus.publish(BigBlueButtonEvent(voiceConfId, new UserLeftVoiceConfMessage(voiceConfId, voiceUserId)))
  }

  def voiceUserLocked(voiceConfId: String, voiceUserId: String, locked: java.lang.Boolean) {
    eventBus.publish(BigBlueButtonEvent(voiceConfId, new UserLockedInVoiceConfMessage(voiceConfId, voiceUserId, locked)))
  }

  def voiceUserMuted(voiceConfId: String, voiceUserId: String, muted: java.lang.Boolean) {
    eventBus.publish(BigBlueButtonEvent(voiceConfId, new UserMutedInVoiceConfMessage(voiceConfId, voiceUserId, muted)))
  }

  def voiceUserTalking(voiceConfId: String, voiceUserId: String, talking: java.lang.Boolean) {
    eventBus.publish(BigBlueButtonEvent(voiceConfId, new UserTalkingInVoiceConfMessage(voiceConfId, voiceUserId, talking)))
  }

  def voiceRecording(voiceConfId: String, recordingFile: String, timestamp: String, recording: java.lang.Boolean) {
    eventBus.publish(BigBlueButtonEvent(voiceConfId, new VoiceConfRecordingStartedMessage(voiceConfId, recordingFile, recording, timestamp)))
  }

  /**
   * *******************************************************************
   * Message Interface for DeskShare
   * *****************************************************************
   */
  def deskShareStarted(confId: String, callerId: String, callerIdName: String) {
    println("____BigBlueButtonInGW::deskShareStarted " + confId + callerId + "    " +
      callerIdName)
    eventBus.publish(BigBlueButtonEvent(confId, new DeskShareStartedRequest(confId, callerId,
      callerIdName)))
  }

  def deskShareStopped(meetingId: String, callerId: String, callerIdName: String) {
    eventBus.publish(BigBlueButtonEvent(meetingId, new DeskShareStoppedRequest(meetingId, callerId, callerIdName)))
  }

  def deskShareRTMPBroadcastStarted(meetingId: String, streamname: String, videoWidth: Int, videoHeight: Int, timestamp: String) {
    eventBus.publish(BigBlueButtonEvent(meetingId, new DeskShareRTMPBroadcastStartedRequest(meetingId, streamname, videoWidth, videoHeight, timestamp)))
  }

  def deskShareRTMPBroadcastStopped(meetingId: String, streamname: String, videoWidth: Int, videoHeight: Int, timestamp: String) {
    eventBus.publish(BigBlueButtonEvent(meetingId, new DeskShareRTMPBroadcastStoppedRequest(meetingId, streamname, videoWidth, videoHeight, timestamp)))
  }

  def deskShareGetInfoRequest(meetingId: String, requesterId: String, replyTo: String): Unit = {
    eventBus.publish(BigBlueButtonEvent(meetingId, new DeskShareGetDeskShareInfoRequest(meetingId, requesterId, replyTo)))
  }
}
