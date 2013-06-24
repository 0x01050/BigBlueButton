package org.bigbluebutton.core.apps.poll.messages


import org.bigbluebutton.core.apps.poll._
import org.bigbluebutton.core.api.InMessage
import org.bigbluebutton.core.api.IOutMessage
import org.bigbluebutton.core.apps.poll.Responder

// Poll Messages
case class CreatePoll(meetingID: String, poll: PollVO, requesterID: String) extends InMessage
case class UpdatePoll(meetingID: String, poll: PollVO) extends InMessage
case class GetPolls(meetingID: String, requesterID: String) extends InMessage
case class DestroyPoll(meetingID: String, pollID: String) extends InMessage
case class RemovePoll(meetingID: String, pollID: String) extends InMessage
case class SharePoll(meetingID: String, pollID: String) extends InMessage
case class StopPoll(meetingID:String, pollID: String) extends InMessage
case class StartPoll(meetingID:String, pollID: String) extends InMessage
case class ClearPoll(meetingID: String, pollID: String, requesterID: String, force: Boolean=false) extends InMessage
case class GetPollResult(meetingID:String, pollID: String, requesterID: String) extends InMessage
case class RespondToPoll(meetingID: String, pollID: String, responses : Array[PollResponseVO])

case class PollResponseVO(questionID: String, responses: Array[ResponderVO])
case class ResponderVO(responseID: String, user: Responder)



case class R(id: String, response: String)
case class Q(id: String, questionType: String, question: String, responses: Array[R])
case class P(id: String, title: String, questions: Array[Q])

// Out Messages
case class GetPollResultReply(meetingID: String, recorded: Boolean, requesterID: String, pollVO: PollVO) extends IOutMessage
case class GetPollsReplyOutMsg(meetingID: String, recorded: Boolean, requesterID: String, polls: Array[PollVO]) extends IOutMessage
case class ClearPollFailed(meetingID: String, pollID: String, requesterID: String, reason: String) extends IOutMessage
case class PollClearedOutMsg(meetingID: String, recorded: Boolean, pollID: String) extends IOutMessage
case class PollStartedOutMsg(meetingID: String, recorded: Boolean, pollID: String) extends IOutMessage
case class PollStoppedOutMsg(meetingID: String, recorded: Boolean, pollID: String) extends IOutMessage
case class PollRemovedOutMsg(meetingID: String, recorded: Boolean, pollID: String) extends IOutMessage
case class PollUpdatedOutMsg(meetingID: String, recorded: Boolean, pollID: String, pollVO: PollVO) extends IOutMessage
case class PollCreatedOutMsg(meetingID: String, recorded: Boolean, pollID: String, pollVO: PollVO) extends IOutMessage
