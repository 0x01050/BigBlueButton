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

package org.bigbluebutton.conference;

import org.slf4j.Logger;
import org.bigbluebutton.conference.service.participants.messaging.redis.UsersMessagePublisher;
import org.bigbluebutton.conference.service.participants.recorder.redis.UsersEventRecorder;
import org.bigbluebutton.conference.service.participants.red5.UsersClientMessageSender;
import org.red5.logging.Red5LoggerFactory;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class Meeting {
	private static Logger log = Red5LoggerFactory.getLogger( Meeting.class, "bigbluebutton" );	
	ArrayList<String> currentPresenter = null;
	private String meetingID;
	private Boolean recorded = false;
	
	private Map <String, User> users;

	private UsersEventRecorder usersEventRecorder;
	private UsersMessagePublisher usersMessagePublisher;
	private UsersClientMessageSender usersClientMessageSender;
		
	public Meeting(String meetingID, Boolean recorded, UsersEventRecorder recorder, UsersMessagePublisher usersMessagePublisher, UsersClientMessageSender usersClientMessageSender) {
		this.meetingID = meetingID;
		this.recorded = recorded;
		usersEventRecorder = recorder;
		this.usersMessagePublisher = usersMessagePublisher;
		users = new ConcurrentHashMap<String, User>();
	}

	public String getMeetingID() {
		return meetingID;
	}
	
	public void addParticipant(User user) {
		synchronized (this) {
			users.put(user.getInternalUserID(), user);
		}
		
		if (recorded) {
			usersEventRecorder.userJoined(meetingID, user);
		}
		
		usersMessagePublisher.participantJoined(meetingID, user);
		usersClientMessageSender.participantJoined(meetingID, user);
		
	}

	public void removeParticipant(String userid) {
		boolean present = false;
		User user = null;
		synchronized (this) {
			present = users.containsKey(userid);
			if (present) {
				user = users.remove(userid);
			}
		}
		if (present) {
			if (recorded) {
				usersEventRecorder.userLeft(meetingID, user);
			}
			
			usersMessagePublisher.participantLeft(meetingID, user);
			usersClientMessageSender.participantLeft(meetingID, user);
		}
	}

	public void changeParticipantStatus(String userid, String status, Object value) {
		boolean present = false;
		User p = null;
		synchronized (this) {
			present = users.containsKey(userid);
			if (present) {
				p = users.get(userid);
				p.setStatus(status, value);
			}
		}
		if (present) {
			if (recorded) {
				usersEventRecorder.userStatusChange(meetingID, p, status, value);
			}
			
			usersMessagePublisher.participantStatusChange(meetingID, p, status, value);
			usersClientMessageSender.participantStatusChange(meetingID, p, status, value);
		}		
	}

	public void endAndKickAll() {
		if (recorded) {
			usersEventRecorder.endAndKickAll(meetingID);
		}
		
		usersClientMessageSender.endAndKickAll(meetingID);
	}

	public Map getParticipants() {
		return users;//unmodifiableMap;
	}	

	public Collection<User> getParticipantCollection() {
		return users.values();
	}

	public int getNumberOfParticipants() {
		return users.size();
	}

	public int getNumberOfModerators() {
		int sum = 0;
		for (Iterator<User> it = users.values().iterator(); it.hasNext(); ) {
			User part = it.next();
			if (part.isModerator()) {
				sum++;
			}
		} 
		log.debug("Returning number of moderators: " + sum);
		return sum;
	}

	public ArrayList<String> getCurrentPresenter() {
		return currentPresenter;
	}
	
	public void assignPresenter(String newPresenterID, String newPresenterName, String assignedBy){
		ArrayList<String> presenter = new ArrayList<String>();
		presenter.add(newPresenterID);
		presenter.add(newPresenterName);
		presenter.add(assignedBy);
		
		currentPresenter = presenter;
		usersEventRecorder.assignPresenter(meetingID, newPresenterID, newPresenterName, assignedBy);
		usersClientMessageSender.assignPresenter(meetingID, newPresenterID, newPresenterName, assignedBy);	
	}
}