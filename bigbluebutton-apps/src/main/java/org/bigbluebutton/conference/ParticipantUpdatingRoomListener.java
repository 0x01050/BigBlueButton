/**
* BigBlueButton open source conferencing system - http://www.bigbluebutton.org/
*
* Copyright (c) 2010 BigBlueButton Inc. and by respective authors (see below).
*
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License as published by the Free Software
* Foundation; either version 2.1 of the License, or (at your option) any later
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

import org.bigbluebutton.conference.service.recorder.pubsub.RedisPublisher;


public class ParticipantUpdatingRoomListener implements IRoomListener{

	RedisPublisher publisher;
	private IConferenceEventListener conferenceEventListener;
	private Room room;
	
	public ParticipantUpdatingRoomListener(IConferenceEventListener lstnr, Room room, RedisPublisher publisher) {
		this.conferenceEventListener = lstnr;
		this.room = room;
		this.publisher=publisher;
	}
	
	public String getName() {
		return "TEMPNAME";
	}
	
	public void participantStatusChange(Long userid, String status, Object value){
		if (conferenceEventListener != null) {
			conferenceEventListener.participantsUpdated(room);
			//redis pubsub
		}
	}
	
	public void participantJoined(Participant p) {
		if (conferenceEventListener != null) {
			conferenceEventListener.participantsUpdated(room);
			//redis pubsub
			//redis pubsub test
			publisher.publish("bigbluebutton:conference:join", room.getName()+":"+p.getUserid()+":"+p.getName()+":"+p.getRole());
			
		}
	}
	
	public void participantLeft(Long userid) {		
		if (conferenceEventListener != null) {
			conferenceEventListener.participantsUpdated(room);
			//redis pubsub
			//redis pubsub test
			publisher.publish("bigbluebutton:conference:remove", room.getName()+":"+userid);
		}
	}

	public void endAndKickAll() {
		// no-op
	}
	
	
}
