package org.bigbluebutton.endpoint

object InMsgNameConst {
  val CreateMeetingRequest = "create_meeting_request"
  val CreateMeetingResponse = "create_meeting_response"
  val MeetingCreatedEvent = "meeting_created_event"
  val EndMeetingRequest = "end_meeting_request"
  val EndMeetingResponse = "end_meeting_response"
  val EndMeetingWarningEvent = "end_meeting_warning_event"
  val MeetingEndEvent = "meeting_end_event"
  val MeetingEndedEvent = "meeting_ended_event"
  val ExtendMeetingRequest = "extend_meeting_request"
  val ExtendMeetingResponse = "extend_meeting_response"
  val MeetingExtendedEvent = "meeting_extended_event"

  val RegisterUserRequest = "register_user_request"
  val RegisterUserResponse = "register_user_response"
  val UserRegisteredEvent = "user_registered_event"
  val UserJoinRequest = "user_join_request"
  val UserJoinResponse = "user_join_response"
  val UserJoinedEvent = "user_joined_event"
  val UserLeaveEvent = "user_leave_event"
  val UserLeftEvent = "user_left_event"
  val GetUsersRequest = "get_users_request"
  val GetUsersResponse = "get_users_response"
  val RaiseUserHandRequest = "raise_user_hand_request"
  val UserRaisedHandEvent = "user_raised_hand_event"
  val AssignPresenterRequest = "assign_presenter_request"
  val PresenterAssignedEvent = "presenter_assigned_event"
  val MuteUserRequest = "mute_user_request"
  val MuteUserRequestEvent = "mute_user_request_event"
  val MuteVoiceUserRequest = "mute_voice_user_request"
  val VoiceUserMutedEvent = "voice_user_muted_event"
  val UserMutedEvent = "user_muted_event"
  val EjectUserRequest = "eject_user_request"
  val UserEjectedEvent = "user_ejected_event"
  val EjectUserFromVoiceRequest = "eject_user_from_voice_request"
  val EjectVoiceUserRequest = "eject_voice_user_request"
  val VoiceUserEjectedEvent = "voice_user_ejected_event"
  val LockUserRequest = "lock_user_request"
  val LockVoiceUserRequest = "lock_voice_user_request"
  val VoiceUserLockedEvent = "voice_user_locked_event"
  val UserLockedEvent = "user_locked_event"
  val LockAllUsersRequest = "lock_all_users_request"
  val AllUsersLockedEvent = "all_users_locked_event"
}

