/*
 * BigBlueButton - http://www.bigbluebutton.org
 * 
 * Copyright (c) 2008-2009 by respective authors (see below). All rights reserved.
 * 
 * BigBlueButton is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * Foundation; either version 3 of the License, or (at your option) any later 
 * version. 
 * 
 * BigBlueButton is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along 
 * with BigBlueButton; if not, If not, see <http://www.gnu.org/licenses/>.
 *
 * $Id: $
 */

package org.bigbluebutton.conference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.red5.logging.Red5LoggerFactory;

public class BigBlueButtonSession {
	private static Logger log = Red5LoggerFactory.getLogger(BigBlueButtonSession.class, "bigbluebutton");
	
	private final String username;
	private final String role;
	private final String conference;
	private final String mode;
	private final String room;
	private final long userid;
	private final String sessionName;
	private final String voiceBridge;
	private final Boolean record;
	private final String externUserID;
	
	public BigBlueButtonSession(String sessionName, long userid, String username, 
				String role, String conference, String mode, String room, String voiceBridge, Boolean record, 
				String externUserID){
		this.userid = userid;
		this.sessionName = sessionName;
		this.username = username;
		this.role = role;
		this.conference = conference;
		this.mode = mode;
		this.room = room;
		
		this.voiceBridge = voiceBridge;
		this.record = record;
		this.externUserID = externUserID;
	}

	public String getUsername() {
		return username;
	}

	public String getRole() {
		return role;
	}

	public String getConference() {
		return conference;
	}

	public String getMode() {
		return mode;
	}

	public String getRoom() {
		return room;
	}

	public long getUserid() {
		return userid;
	}

	public String getSessionName() {
		return sessionName;
	}

	public String getVoiceBridge() {
		return voiceBridge;
	}

	public Boolean getRecord() {
		return record;
	}

	public String getExternUserID() {
		return externUserID;
	}
}
