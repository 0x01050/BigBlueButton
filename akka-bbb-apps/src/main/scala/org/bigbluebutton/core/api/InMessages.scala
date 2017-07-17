package org.bigbluebutton.core.api

import org.bigbluebutton.common2.msgs.BreakoutUserVO
import org.bigbluebutton.core.apps.Presentation
import spray.json.JsObject
case class InMessageHeader(name: String)
case class InHeaderAndJsonPayload(header: InMessageHeader, payload: JsObject)
case class MessageProcessException(message: String) extends Exception(message)

trait InMessage

//////////////////////////////////////////////////////////////////////////////
// System
/////////////////////////////////////////////////////////////////////////////

case class PubSubPing(system: String, timestamp: Long) extends InMessage
case class IsMeetingActorAliveMessage(meetingId: String) extends InMessage
case class KeepAliveMessage(aliveID: String) extends InMessage

//////////////////////////////////////////////////////////////////////////////
// Meeting
/////////////////////////////////////////////////////////////////////////////

case class MonitorNumberOfUsers(meetingID: String) extends InMessage
case class SendTimeRemainingUpdate(meetingId: String) extends InMessage
case class ExtendMeetingDuration(meetingId: String, userId: String) extends InMessage
case class DestroyMeeting(meetingID: String) extends InMessage
case class StartMeeting(meetingID: String) extends InMessage
case class EndMeeting(meetingId: String) extends InMessage
case class LockSetting(meetingID: String, locked: Boolean, settings: Map[String, Boolean]) extends InMessage
case class UpdateMeetingExpireMonitor(meetingID: String, hasUser: Boolean) extends InMessage

////////////////////////////////////////////////////////////////////////////////////
// Lock
///////////////////////////////////////////////////////////////////////////////////

case class LockUser(meetingID: String, userId: String, lock: Boolean) extends InMessage
case class InitLockSettings(meetingID: String, settings: Permissions) extends InMessage
case class SetLockSettings(meetingID: String, setByUser: String, settings: Permissions) extends InMessage
case class GetLockSettings(meetingID: String, userId: String) extends InMessage

//////////////////////////////////////////////////////////////////////////////////
// Users
/////////////////////////////////////////////////////////////////////////////////

case class ValidateAuthToken(meetingID: String, userId: String, token: String,
                             correlationId: String, sessionId: String) extends InMessage
case class RegisterUser(meetingID: String, userID: String, name: String, role: String,
                        extUserID: String, authToken: String, avatarURL: String, guest: Boolean, authed: Boolean) extends InMessage
case class UserJoining(meetingID: String, userID: String, authToken: String) extends InMessage
case class UserLeaving(meetingID: String, userID: String, sessionId: String) extends InMessage
case class GetUsers(meetingID: String, requesterID: String) extends InMessage
case class UserEmojiStatus(meetingID: String, userId: String, emojiStatus: String) extends InMessage

case class UserShareWebcam(meetingID: String, userId: String, stream: String) extends InMessage
case class UserUnshareWebcam(meetingID: String, userId: String, stream: String) extends InMessage
case class ChangeUserStatus(meetingID: String, userID: String, status: String, value: Object) extends InMessage

case class AssignPresenter(meetingID: String, newPresenterID: String, newPresenterName: String, assignedBy: String) extends InMessage
case class SetRecordingStatus(meetingID: String, userId: String, recording: Boolean) extends InMessage
case class GetRecordingStatus(meetingID: String, userId: String) extends InMessage
case class AllowUserToShareDesktop(meetingID: String, userID: String) extends InMessage
case class ActivityResponse(meetingID: String) extends InMessage
case class LogoutEndMeeting(meetingID: String, userID: String) extends InMessage

///////////////////////////////////////////////////////////////////////////////////////
// Guest support
///////////////////////////////////////////////////////////////////////////////////////

case class GetGuestPolicy(meetingID: String, requesterID: String) extends InMessage
case class SetGuestPolicy(meetingID: String, policy: String, setBy: String) extends InMessage
case class RespondToGuest(meetingID: String, userId: String, response: Boolean, requesterID: String) extends InMessage

// No idea what part this is for
case class GetAllMeetingsRequest(meetingID: String /** Not used. Just to satisfy trait **/ ) extends InMessage

// DeskShare
case class DeskShareStartedRequest(conferenceName: String, callerId: String, callerIdName: String) extends InMessage
case class DeskShareStoppedRequest(conferenceName: String, callerId: String, callerIdName: String) extends InMessage
case class DeskShareRTMPBroadcastStartedRequest(conferenceName: String, streamname: String, videoWidth: Int, videoHeight: Int, timestamp: String) extends InMessage
case class DeskShareRTMPBroadcastStoppedRequest(conferenceName: String, streamname: String, videoWidth: Int, videoHeight: Int, timestamp: String) extends InMessage
case class DeskShareGetDeskShareInfoRequest(conferenceName: String, requesterID: String, replyTo: String) extends InMessage
