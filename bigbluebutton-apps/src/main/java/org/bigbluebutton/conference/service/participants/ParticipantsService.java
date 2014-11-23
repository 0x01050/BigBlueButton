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

package org.bigbluebutton.conference.service.participants;

import org.slf4j.Logger;
import org.red5.logging.Red5LoggerFactory;
import org.red5.server.api.Red5;
import org.red5.server.api.scope.IScope;
import java.util.Map;
import org.bigbluebutton.conference.BigBlueButtonSession;
import org.bigbluebutton.conference.Constants;


public class ParticipantsService {
	private static Logger log = Red5LoggerFactory.getLogger( ParticipantsService.class, "bigbluebutton" );	
	private ParticipantsApplication application;

	public void assignPresenter(Map<String, String> msg) {
		IScope scope = Red5.getConnectionLocal().getScope();
		application.assignPresenter(scope.getName(), (String) msg.get("newPresenterID"), (String) msg.get("newPresenterName"), (String) msg.get("assignedBy"));
	}
	
	public void getParticipants() {
		IScope scope = Red5.getConnectionLocal().getScope();
		application.getUsers(scope.getName(), getBbbSession().getInternalUserID());
	}
	
	public void userRaiseHand() {
		IScope scope = Red5.getConnectionLocal().getScope();
		String userId = getBbbSession().getInternalUserID();
		application.userRaiseHand(scope.getName(), userId);
	}
	
	public void lowerHand(Map<String, String> msg) {
		String userId = (String) msg.get("userId");
		String loweredBy = (String) msg.get("loweredBy");
		IScope scope = Red5.getConnectionLocal().getScope();
		application.lowerHand(scope.getName(), userId, loweredBy);
	}
	
	public void ejectUserFromMeeting(Map<String, String> msg) {
		String userId = (String) msg.get("userId");
		String ejectedBy = (String) msg.get("ejectedBy");
		IScope scope = Red5.getConnectionLocal().getScope();
		application.ejectUserFromMeeting(scope.getName(), userId, ejectedBy);
	}
	
	public void shareWebcam(String stream) {
		IScope scope = Red5.getConnectionLocal().getScope();
		String userId = getBbbSession().getInternalUserID();
		application.shareWebcam(scope.getName(), userId, stream);		
	}
	
	public void unshareWebcam(String stream) {
		IScope scope = Red5.getConnectionLocal().getScope();
		String userId = getBbbSession().getInternalUserID();
		application.unshareWebcam(scope.getName(), userId, stream);
	}
	
	public void setParticipantStatus(Map<String, Object> msg) {
		String roomName = Red5.getConnectionLocal().getScope().getName();

		application.setParticipantStatus(roomName, (String) msg.get("userID"), (String) msg.get("status"), (Object) msg.get("value"));
	}
	
	public void setParticipantsApplication(ParticipantsApplication a) {
		application = a;
	}
	
	public void setRecordingStatus(Map<String, Object> msg) {
		String roomName = Red5.getConnectionLocal().getScope().getName();
		application.setRecordingStatus(roomName, (String)msg.get("userId"), (Boolean) msg.get("recording"));
	}

	public void getRecordingStatus() {
		String roomName = Red5.getConnectionLocal().getScope().getName();
		application.getRecordingStatus(roomName, getMyUserId());
	}
	
	public String getMyUserId() {
		BigBlueButtonSession bbbSession = (BigBlueButtonSession) Red5.getConnectionLocal().getAttribute(Constants.SESSION);
		assert bbbSession != null;
		return bbbSession.getInternalUserID();
	}
	
	private BigBlueButtonSession getBbbSession() {
        return (BigBlueButtonSession) Red5.getConnectionLocal().getAttribute(Constants.SESSION);
    }

	public void askingToEnter() {
		String userId = getBbbSession().getInternalUserID();
		String roomName = Red5.getConnectionLocal().getScope().getName();
		application.askingToEnter(roomName, userId);
	}

	public void getGuestPolicy() {
		String requesterId = getBbbSession().getInternalUserID();
		String roomName = Red5.getConnectionLocal().getScope().getName();
		application.getGuestPolicy(roomName, requesterId);
	}

	public void setGuestPolicy(String guestPolicy) {
		String roomName = Red5.getConnectionLocal().getScope().getName();
		application.newGuestPolicy(roomName, guestPolicy);
	}

	public void getGuestsWaiting() {
		String userId = getBbbSession().getInternalUserID();
		String roomName = Red5.getConnectionLocal().getScope().getName();
		application.askingForGuestWaiting(roomName, userId);
	}

	public void responseToAllGuests(Boolean resp) {
		String roomName = Red5.getConnectionLocal().getScope().getName();
		application.responseToAllGuests(roomName, resp);
	}

	public void responseToGuest(Map<String, Object> msg) {
		String roomName = Red5.getConnectionLocal().getScope().getName();
		application.responseToGuest(roomName, (String) msg.get("guestID"), (Boolean) msg.get("response"));
	}

	public void kickGuest(String guestId) {
		String roomName = Red5.getConnectionLocal().getScope().getName();
		application.kickGuest(roomName, guestId);
	}

	public void setParticipantRole(Map<String, String> msg) {
		String roomName = Red5.getConnectionLocal().getScope().getName();
		String userId = (String) msg.get("userId");
		String role = (String) msg.get("role");
		log.debug("Setting participant role " + roomName + " " + userId + " " + role);
		application.setParticipantRole(roomName, userId, role);
	}
}
