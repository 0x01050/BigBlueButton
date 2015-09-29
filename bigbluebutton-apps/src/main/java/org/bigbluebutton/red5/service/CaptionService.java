/**
* BigBlueButton open source conferencing system - http://www.bigbluebutton.org/
* 
* Copyright (c) 2012 BigBlueButton Inc. and by respective authors (see below).
*
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License as published by the Free Software
* Foundation; either version 3.0 of the License, or (at your option) any later
* version.
* 
* BigBlueButton is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
* PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License along
* with BigBlueButton; if not, see <http://www.gnu.org/licenses/>.
*
*/
package org.bigbluebutton.red5.service;

import java.util.Map;

import org.bigbluebutton.red5.BigBlueButtonSession;
import org.bigbluebutton.red5.Constants;
import org.bigbluebutton.red5.pubsub.MessagePublisher;
import org.red5.logging.Red5LoggerFactory;
import org.red5.server.api.Red5;
import org.slf4j.Logger;

public class CaptionService {	
	private static Logger log = Red5LoggerFactory.getLogger( ChatService.class, "bigbluebutton" );
	
	private MessagePublisher red5InGW;

	public void setRed5Publisher(MessagePublisher inGW) {
		red5InGW = inGW;
	}
	
	private BigBlueButtonSession getBbbSession() {
		return (BigBlueButtonSession) Red5.getConnectionLocal().getAttribute(Constants.SESSION);
	}
	
	public void getCaptionHistory() {
		String meetingID = Red5.getConnectionLocal().getScope().getName();
		String requesterID = getBbbSession().getInternalUserID();
		
		red5InGW.sendCaptionHistory(meetingID, requesterID);
	}
	
	public void sendPublicMessage(Map<String, Object> msg) {
		log.debug("Received new caption line request");
		int lineNumber = (Integer) msg.get("lineNumber");
		String locale = msg.get("locale").toString();
		int startTime = (Integer) msg.get("startTime");
		String text = msg.get("text").toString();
		
		String meetingId = Red5.getConnectionLocal().getScope().getName();
		
		red5InGW.newCaptionLine(meetingId, lineNumber, locale, startTime, text);
	}
}
