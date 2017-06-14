package org.bigbluebutton.core.apps.presentation.poll

import akka.actor.ActorContext
import akka.event.Logging
import org.bigbluebutton.core.OutMessageGateway
import org.bigbluebutton.core.running.LiveMeeting

class PollApp2x(val liveMeeting: LiveMeeting,
  val outGW: OutMessageGateway)(implicit val context: ActorContext)
    extends EjectUserFromMeetingHdlr {

  val log = Logging(context.system, getClass)
}
