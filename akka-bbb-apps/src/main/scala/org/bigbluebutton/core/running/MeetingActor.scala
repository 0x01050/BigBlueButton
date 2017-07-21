package org.bigbluebutton.core.running

import java.io.{ PrintWriter, StringWriter }

import org.bigbluebutton.core.apps.users.UsersApp
import org.bigbluebutton.core.domain.{ MeetingExpiryTracker, MeetingInactivityTracker }
import org.bigbluebutton.core.util.TimeUtil
//import java.util.concurrent.TimeUnit

import akka.actor._
import akka.actor.SupervisorStrategy.Resume
import org.bigbluebutton.common2.domain.DefaultProps
import org.bigbluebutton.core._
import org.bigbluebutton.core.api._
import org.bigbluebutton.core.apps._
import org.bigbluebutton.core.apps.caption.CaptionApp2x
import org.bigbluebutton.core.apps.chat.ChatApp2x
import org.bigbluebutton.core.apps.screenshare.ScreenshareApp2x
import org.bigbluebutton.core.apps.presentation.PresentationApp2x
import org.bigbluebutton.core.apps.meeting._
import org.bigbluebutton.core.apps.users.UsersApp2x
import org.bigbluebutton.core.apps.sharednotes.SharedNotesApp2x
import org.bigbluebutton.core.apps.whiteboard.WhiteboardApp2x
import org.bigbluebutton.core.bus._
import org.bigbluebutton.core.models._
import org.bigbluebutton.core2.MeetingStatus2x
import org.bigbluebutton.core2.message.handlers._
import org.bigbluebutton.core2.message.handlers.users._
import org.bigbluebutton.core2.message.handlers.meeting._
import org.bigbluebutton.common2.msgs._
import org.bigbluebutton.core.apps.breakout._
import org.bigbluebutton.core.apps.polls._
import org.bigbluebutton.core.apps.voice._

import scala.concurrent.duration._
import org.bigbluebutton.core2.testdata.FakeTestData
import org.bigbluebutton.core.apps.layout.LayoutApp2x
import org.bigbluebutton.core.apps.meeting.SyncGetMeetingInfoRespMsgHdlr

object MeetingActor {
  def props(
    props:       DefaultProps,
    eventBus:    IncomingEventBus,
    outGW:       OutMessageGateway,
    liveMeeting: LiveMeeting
  ): Props =
    Props(classOf[MeetingActor], props, eventBus, outGW, liveMeeting)
}

class MeetingActor(
  val props:       DefaultProps,
  val eventBus:    IncomingEventBus,
  val outGW:       OutMessageGateway,
  val liveMeeting: LiveMeeting
)
    extends BaseMeetingActor
    with GuestsApp
    with LayoutApp2x
    with VoiceApp2x
    with PollApp2x
    with BreakoutApp2x
    with UsersApp2x
    with WhiteboardApp2x

    with PermisssionCheck
    with UserBroadcastCamStartMsgHdlr
    with UserJoinMeetingReqMsgHdlr
    with UserBroadcastCamStopMsgHdlr
    with UserConnectedToGlobalAudioMsgHdlr
    with UserDisconnectedFromGlobalAudioMsgHdlr
    with MuteAllExceptPresentersCmdMsgHdlr
    with MuteMeetingCmdMsgHdlr
    with IsMeetingMutedReqMsgHdlr
    with MuteUserCmdMsgHdlr
    with EjectUserFromVoiceCmdMsgHdlr
    with EndMeetingSysCmdMsgHdlr
    with DestroyMeetingSysCmdMsgHdlr
    with SendTimeRemainingUpdateHdlr
    with SyncGetMeetingInfoRespMsgHdlr {

  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
    case e: Exception => {
      val sw: StringWriter = new StringWriter()
      sw.write("An exception has been thrown on MeetingActor, exception message [" + e.getMessage() + "] (full stacktrace below)\n")
      e.printStackTrace(new PrintWriter(sw))
      log.error(sw.toString())
      Resume
    }
  }

  /**
   * Put the internal message injector into another actor so this
   * actor is easy to test.
   */
  var actorMonitor = context.actorOf(
    MeetingActorAudit.props(props, eventBus, outGW),
    "actorMonitor-" + props.meetingProp.intId
  )

  val presentationApp2x = new PresentationApp2x(liveMeeting, outGW)
  val screenshareApp2x = new ScreenshareApp2x(liveMeeting, outGW)
  val captionApp2x = new CaptionApp2x(liveMeeting, outGW)
  val sharedNotesApp2x = new SharedNotesApp2x(liveMeeting, outGW)
  val chatApp2x = new ChatApp2x(liveMeeting, outGW)
  val usersApp = new UsersApp(liveMeeting, outGW)

  var inactivityTracker = new MeetingInactivityTracker(
    liveMeeting.props.durationProps.maxInactivityTimeoutMinutes,
    liveMeeting.props.durationProps.warnMinutesBeforeMax,
    TimeUtil.millisToMinutes(System.currentTimeMillis()), false, 0L
  )

  var expiryTracker = new MeetingExpiryTracker(
    TimeUtil.millisToMinutes(System.currentTimeMillis()),
    false, 0L
  )

  /*******************************************************************/
  //object FakeTestData extends FakeTestData
  //FakeTestData.createFakeUsers(liveMeeting)
  /*******************************************************************/

  def receive = {
    //=============================
    // 2x messages
    case msg: BbbCommonEnvCoreMsg              => handleBbbCommonEnvCoreMsg(msg)

    // Handling RegisterUserReqMsg as it is forwarded from BBBActor and
    // its type is not BbbCommonEnvCoreMsg
    case m: RegisterUserReqMsg                 => usersApp.handleRegisterUserReqMsg(m)
    case m: GetAllMeetingsReqMsg               => handleGetAllMeetingsReqMsg(m)

    // Meeting
    case m: DestroyMeetingSysCmdMsg            => handleDestroyMeetingSysCmdMsg(m)

    //======================================

    //=======================================
    // old messages
    case msg: MonitorNumberOfUsers             => handleMonitorNumberOfUsers(msg)

    case msg: AllowUserToShareDesktop          => handleAllowUserToShareDesktop(msg)
    case msg: ExtendMeetingDuration            => handleExtendMeetingDuration(msg)
    case msg: SendTimeRemainingUpdate          => handleSendTimeRemainingUpdate(msg)

    // Screenshare
    case msg: DeskShareGetDeskShareInfoRequest => handleDeskShareGetDeskShareInfoRequest(msg)

    // Guest
    case msg: GetGuestPolicy                   => handleGetGuestPolicy(msg)
    case msg: SetGuestPolicy                   => handleSetGuestPolicy(msg)

    case _                                     => // do nothing
  }

  private def handleBbbCommonEnvCoreMsg(msg: BbbCommonEnvCoreMsg): Unit = {
    // TODO: Update meeting activity status here
    // updateActivityStatus(msg)

    msg.core match {
      // Users
      case m: ValidateAuthTokenReqMsg => usersApp.handleValidateAuthTokenReqMsg(m)
      case m: UserJoinMeetingReqMsg => handleUserJoinMeetingReqMsg(m)
      case m: UserLeaveReqMsg => handleUserLeaveReqMsg(m)
      case m: UserBroadcastCamStartMsg => handleUserBroadcastCamStartMsg(m)
      case m: UserBroadcastCamStopMsg => handleUserBroadcastCamStopMsg(m)
      case m: UserJoinedVoiceConfEvtMsg => handleUserJoinedVoiceConfEvtMsg(m)
      case m: MeetingActivityResponseCmdMsg => inactivityTracker = usersApp.handleMeetingActivityResponseCmdMsg(m, inactivityTracker)
      case m: LogoutAndEndMeetingCmdMsg => usersApp.handleLogoutAndEndMeetingCmdMsg(m)
      case m: SetRecordingStatusCmdMsg => usersApp.handleSetRecordingStatusCmdMsg(m)
      case m: GetRecordingStatusReqMsg => usersApp.handleGetRecordingStatusReqMsg(m)
      case m: ChangeUserEmojiCmdMsg => handleChangeUserEmojiCmdMsg(m)
      case m: EjectUserFromMeetingCmdMsg => usersApp.handleEjectUserFromMeetingCmdMsg(m)
      case m: GetUsersMeetingReqMsg => usersApp.handleGetUsersMeetingReqMsg(m)

      // Whiteboard
      case m: SendCursorPositionPubMsg => handleSendCursorPositionPubMsg(m)
      case m: ClearWhiteboardPubMsg => handleClearWhiteboardPubMsg(m)
      case m: UndoWhiteboardPubMsg => handleUndoWhiteboardPubMsg(m)
      case m: ModifyWhiteboardAccessPubMsg => handleModifyWhiteboardAccessPubMsg(m)
      case m: GetWhiteboardAccessReqMsg => handleGetWhiteboardAccessReqMsg(m)
      case m: SendWhiteboardAnnotationPubMsg => handleSendWhiteboardAnnotationPubMsg(m)
      case m: GetWhiteboardAnnotationsReqMsg => handleGetWhiteboardAnnotationsReqMsg(m)

      // Poll
      case m: StartPollReqMsg => handleStartPollReqMsg(m)
      case m: StartCustomPollReqMsg => handleStartCustomPollReqMsg(m)
      case m: StopPollReqMsg => handleStopPollReqMsg(m)
      case m: ShowPollResultReqMsg => handleShowPollResultReqMsg(m)
      case m: HidePollResultReqMsg => handleHidePollResultReqMsg(m)
      case m: GetCurrentPollReqMsg => handleGetCurrentPollReqMsg(m)
      case m: RespondToPollReqMsg => handleRespondToPollReqMsg(m)

      // Breakout
      case m: BreakoutRoomsListMsg => handleBreakoutRoomsListMsg(m)
      case m: CreateBreakoutRoomsCmdMsg => handleCreateBreakoutRoomsCmdMsg(m)
      case m: EndAllBreakoutRoomsMsg => handleEndAllBreakoutRoomsMsg(m)
      case m: RequestBreakoutJoinURLReqMsg => handleRequestBreakoutJoinURLReqMsg(m)
      case m: BreakoutRoomCreatedMsg => handleBreakoutRoomCreatedMsg(m)
      case m: BreakoutRoomEndedMsg => handleBreakoutRoomEndedMsg(m)
      case m: BreakoutRoomUsersUpdateMsg => handleBreakoutRoomUsersUpdateMsg(m)
      case m: SendBreakoutUsersUpdateMsg => handleSendBreakoutUsersUpdateMsg(m)
      case m: TransferUserToMeetingRequestMsg => handleTransferUserToMeetingRequestMsg(m)

      // Voice
      case m: UserLeftVoiceConfEvtMsg => handleUserLeftVoiceConfEvtMsg(m)
      case m: UserMutedInVoiceConfEvtMsg => handleUserMutedInVoiceConfEvtMsg(m)
      case m: UserTalkingInVoiceConfEvtMsg => handleUserTalkingInVoiceConfEvtMsg(m)
      case m: RecordingStartedVoiceConfEvtMsg => handleRecordingStartedVoiceConfEvtMsg(m)
      case m: MuteUserCmdMsg => handleMuteUserCmdMsg(m)
      case m: MuteAllExceptPresentersCmdMsg => handleMuteAllExceptPresentersCmdMsg(m)
      case m: EjectUserFromVoiceCmdMsg => handleEjectUserFromVoiceCmdMsg(m)
      case m: IsMeetingMutedReqMsg => handleIsMeetingMutedReqMsg(m)
      case m: MuteMeetingCmdMsg => handleMuteMeetingCmdMsg(m)
      case m: UserConnectedToGlobalAudioMsg => handleUserConnectedToGlobalAudioMsg(m)
      case m: UserDisconnectedFromGlobalAudioMsg => handleUserDisconnectedFromGlobalAudioMsg(m)

      // Layout
      case m: GetCurrentLayoutReqMsg => handleGetCurrentLayoutReqMsg(m)
      case m: LockLayoutMsg => handleLockLayoutMsg(m)
      case m: BroadcastLayoutMsg => handleBroadcastLayoutMsg(m)

      // Presentation
      case m: SetCurrentPresentationPubMsg => presentationApp2x.handleSetCurrentPresentationPubMsg(m)
      case m: GetPresentationInfoReqMsg => presentationApp2x.handleGetPresentationInfoReqMsg(m)
      case m: SetCurrentPagePubMsg => presentationApp2x.handleSetCurrentPagePubMsg(m)
      case m: ResizeAndMovePagePubMsg => presentationApp2x.handleResizeAndMovePagePubMsg(m)
      case m: RemovePresentationPubMsg => presentationApp2x.handleRemovePresentationPubMsg(m)
      case m: PreuploadedPresentationsSysPubMsg => presentationApp2x.handlePreuploadedPresentationsPubMsg(m)
      case m: PresentationConversionUpdateSysPubMsg => presentationApp2x.handlePresentationConversionUpdatePubMsg(m)
      case m: PresentationPageCountErrorSysPubMsg => presentationApp2x.handlePresentationPageCountErrorPubMsg(m)
      case m: PresentationPageGeneratedSysPubMsg => presentationApp2x.handlePresentationPageGeneratedPubMsg(m)
      case m: PresentationConversionCompletedSysPubMsg => presentationApp2x.handlePresentationConversionCompletedPubMsg(m)
      case m: AssignPresenterReqMsg => handlePresenterChange(m)

      // Caption
      case m: EditCaptionHistoryPubMsg => captionApp2x.handleEditCaptionHistoryPubMsg(m)
      case m: UpdateCaptionOwnerPubMsg => captionApp2x.handleUpdateCaptionOwnerPubMsg(m)
      case m: SendCaptionHistoryReqMsg => captionApp2x.handleSendCaptionHistoryReqMsg(m)

      // SharedNotes
      case m: GetSharedNotesPubMsg => sharedNotesApp2x.handleGetSharedNotesPubMsg(m)
      case m: SyncSharedNotePubMsg => sharedNotesApp2x.handleSyncSharedNotePubMsg(m)
      case m: UpdateSharedNoteReqMsg => sharedNotesApp2x.handleUpdateSharedNoteReqMsg(m)
      case m: CreateSharedNoteReqMsg => sharedNotesApp2x.handleCreateSharedNoteReqMsg(m)
      case m: DestroySharedNoteReqMsg => sharedNotesApp2x.handleDestroySharedNoteReqMsg(m)

      // Guests
      case m: GetGuestsWaitingApprovalReqMsg => handleGetGuestsWaitingApprovalReqMsg(m)
      case m: SetGuestPolicyMsg => handleSetGuestPolicyMsg(m)
      case m: GuestsWaitingApprovedMsg => handleGuestsWaitingApprovedMsg(m)

      // Chat
      case m: GetChatHistoryReqMsg => chatApp2x.handleGetChatHistoryReqMsg(m)
      case m: SendPublicMessagePubMsg => chatApp2x.handleSendPublicMessagePubMsg(m)
      case m: SendPrivateMessagePubMsg => chatApp2x.handleSendPrivateMessagePubMsg(m)
      case m: ClearPublicChatHistoryPubMsg => chatApp2x.handleClearPublicChatHistoryPubMsg(m)

      // Screenshare
      case m: ScreenshareStartedVoiceConfEvtMsg => screenshareApp2x.handleScreenshareStartedVoiceConfEvtMsg(m)
      case m: ScreenshareStoppedVoiceConfEvtMsg => screenshareApp2x.handleScreenshareStoppedVoiceConfEvtMsg(m)
      case m: ScreenshareRtmpBroadcastStartedVoiceConfEvtMsg => screenshareApp2x.handleScreenshareRtmpBroadcastStartedVoiceConfEvtMsg(m)
      case m: ScreenshareRtmpBroadcastStoppedVoiceConfEvtMsg => screenshareApp2x.handleScreenshareRtmpBroadcastStoppedVoiceConfEvtMsg(m)
      case m: GetScreenshareStatusReqMsg => screenshareApp2x.handleGetScreenshareStatusReqMsg(m)

      case _ => log.warning("***** Cannot handle " + msg.envelope.name)
    }
  }

  def handleGetAllMeetingsReqMsg(msg: GetAllMeetingsReqMsg): Unit = {
    // sync all meetings
    handleSyncGetMeetingInfoRespMsg(liveMeeting.props)

    // sync all users
    usersApp.handleSyncGetUsersMeetingRespMsg()

    // sync all presentations
    presentationApp2x.handleSyncGetPresentationInfoRespMsg()

    // TODO send all chat
    // TODO send all lock settings
    // TODO send all screen sharing info
  }

  def handlePresenterChange(msg: AssignPresenterReqMsg): Unit = {
    // Stop poll if one is running as presenter left
    handleStopPollReqMsg(msg.header.userId)

    // switch user presenter status for old and new presenter
    usersApp.handleAssignPresenterReqMsg(msg)

    // TODO stop current screen sharing session (initiated by the old presenter)

  }

  def handleDeskShareGetDeskShareInfoRequest(msg: DeskShareGetDeskShareInfoRequest): Unit = {

    log.info("handleDeskShareGetDeskShareInfoRequest: " + msg.conferenceName + "isBroadcasting="
      + ScreenshareModel.isBroadcastingRTMP(liveMeeting.screenshareModel) + " URL:" +
      ScreenshareModel.getRTMPBroadcastingUrl(liveMeeting.screenshareModel))

    if (ScreenshareModel.isBroadcastingRTMP(liveMeeting.screenshareModel)) {
      // if the meeting has an ongoing WebRTC Deskshare session, send a notification
      //outGW.send(new DeskShareNotifyASingleViewer(props.meetingProp.intId, msg.requesterID,
      //  DeskshareModel.getRTMPBroadcastingUrl(liveMeeting.deskshareModel),
      //  DeskshareModel.getDesktopShareVideoWidth(liveMeeting.deskshareModel),
      //  DeskshareModel.getDesktopShareVideoHeight(liveMeeting.deskshareModel), true))
    }
  }

  def handleGetGuestPolicy(msg: GetGuestPolicy) {
    //   outGW.send(new GetGuestPolicyReply(msg.meetingID, props.recordProp.record,
    //     msg.requesterID, MeetingStatus2x.getGuestPolicy(liveMeeting.status).toString()))
  }

  def handleSetGuestPolicy(msg: SetGuestPolicy) {
    //    MeetingStatus2x.setGuestPolicy(liveMeeting.status, msg.policy)
    //    MeetingStatus2x.setGuestPolicySetBy(liveMeeting.status, msg.setBy)
    //    outGW.send(new GuestPolicyChanged(msg.meetingID, props.recordProp.record,
    //      MeetingStatus2x.getGuestPolicy(liveMeeting.status).toString()))
  }

  def handleAllowUserToShareDesktop(msg: AllowUserToShareDesktop): Unit = {
    Users2x.findPresenter(liveMeeting.users2x) match {
      case Some(curPres) => {
        val allowed = msg.userID equals (curPres.intId)
        //   outGW.send(AllowUserToShareDesktopOut(msg.meetingID, msg.userID, allowed))
      }
      case None => // do nothing
    }
  }

  def handleMonitorNumberOfUsers(msg: MonitorNumberOfUsers) {
    inactivityTracker = MeetingInactivityTrackerHelper.processMeetingInactivityAudit(
      props = liveMeeting.props,
      outGW,
      eventBus,
      inactivityTracker
    )

    expiryTracker = MeetingExpiryTracker.processMeetingExpiryAudit(liveMeeting.props, expiryTracker, eventBus)

    monitorNumberOfWebUsers()
    monitorNumberOfUsers()
  }

  def monitorNumberOfWebUsers() {

    def buildEjectAllFromVoiceConfMsg(meetingId: String, voiceConf: String): BbbCommonEnvCoreMsg = {
      val routing = collection.immutable.HashMap("sender" -> "bbb-apps-akka")
      val envelope = BbbCoreEnvelope(EjectAllFromVoiceConfMsg.NAME, routing)
      val body = EjectAllFromVoiceConfMsgBody(voiceConf)
      val header = BbbCoreHeaderWithMeetingId(EjectAllFromVoiceConfMsg.NAME, meetingId)
      val event = EjectAllFromVoiceConfMsg(header, body)

      BbbCommonEnvCoreMsg(envelope, event)
    }

    if (Users2x.numUsers(liveMeeting.users2x) == 0 &&
      MeetingStatus2x.lastWebUserLeftOn(liveMeeting.status) > 0) {
      if (liveMeeting.timeNowInMinutes - MeetingStatus2x.lastWebUserLeftOn(liveMeeting.status) > 2) {
        log.info("Empty meeting. Ejecting all users from voice. meetingId={}", props.meetingProp.intId)
        val event = buildEjectAllFromVoiceConfMsg(props.meetingProp.intId, props.voiceProp.voiceConf)
        outGW.send(event)
      }
    }
  }

  def monitorNumberOfUsers() {
    val hasUsers = Users2x.numUsers(liveMeeting.users2x) != 0
    // TODO: We could use a better control over this message to send it just when it really matters :)
    eventBus.publish(BigBlueButtonEvent(props.meetingProp.intId, UpdateMeetingExpireMonitor(props.meetingProp.intId, hasUsers)))
  }

  def handleExtendMeetingDuration(msg: ExtendMeetingDuration) {

  }

  def startRecordingIfAutoStart() {
    if (props.recordProp.record && !MeetingStatus2x.isRecording(liveMeeting.status) &&
      props.recordProp.autoStartRecording && Users2x.numUsers(liveMeeting.users2x) == 1) {
      log.info("Auto start recording. meetingId={}", props.meetingProp.intId)
      MeetingStatus2x.recordingStarted(liveMeeting.status)

      def buildRecordingStatusChangedEvtMsg(meetingId: String, userId: String, recording: Boolean): BbbCommonEnvCoreMsg = {
        val routing = Routing.addMsgToClientRouting(MessageTypes.BROADCAST_TO_MEETING, meetingId, userId)
        val envelope = BbbCoreEnvelope(RecordingStatusChangedEvtMsg.NAME, routing)
        val body = RecordingStatusChangedEvtMsgBody(recording, userId)
        val header = BbbClientMsgHeader(RecordingStatusChangedEvtMsg.NAME, meetingId, userId)
        val event = RecordingStatusChangedEvtMsg(header, body)

        BbbCommonEnvCoreMsg(envelope, event)
      }

      val event = buildRecordingStatusChangedEvtMsg(
        liveMeeting.props.meetingProp.intId,
        "system", MeetingStatus2x.isRecording(liveMeeting.status)
      )
      outGW.send(event)

    }
  }

  def stopAutoStartedRecording() {
    if (props.recordProp.record && MeetingStatus2x.isRecording(liveMeeting.status) &&
      props.recordProp.autoStartRecording && Users2x.numUsers(liveMeeting.users2x) == 0) {
      log.info("Last web user left. Auto stopping recording. meetingId={}", props.meetingProp.intId)
      MeetingStatus2x.recordingStopped(liveMeeting.status)

      def buildRecordingStatusChangedEvtMsg(meetingId: String, userId: String, recording: Boolean): BbbCommonEnvCoreMsg = {
        val routing = Routing.addMsgToClientRouting(MessageTypes.BROADCAST_TO_MEETING, meetingId, userId)
        val envelope = BbbCoreEnvelope(RecordingStatusChangedEvtMsg.NAME, routing)
        val body = RecordingStatusChangedEvtMsgBody(recording, userId)
        val header = BbbClientMsgHeader(RecordingStatusChangedEvtMsg.NAME, meetingId, userId)
        val event = RecordingStatusChangedEvtMsg(header, body)

        BbbCommonEnvCoreMsg(envelope, event)
      }

      val event = buildRecordingStatusChangedEvtMsg(
        liveMeeting.props.meetingProp.intId,
        "system", MeetingStatus2x.isRecording(liveMeeting.status)
      )
      outGW.send(event)

    }
  }

  def record(msg: BbbCoreMsg): Unit = {
    if (liveMeeting.props.recordProp.record) {
      outGW.record(msg)
    }
  }
}
