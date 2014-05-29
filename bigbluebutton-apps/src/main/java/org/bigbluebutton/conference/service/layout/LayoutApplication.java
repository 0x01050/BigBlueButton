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
package org.bigbluebutton.conference.service.layout;

import org.bigbluebutton.core.api.IBigBlueButtonInGW;

public class LayoutApplication {	
	private IBigBlueButtonInGW bbbInGW;
	
	public void setBigBlueButtonInGW(IBigBlueButtonInGW inGW) {
		bbbInGW = inGW;
	}
	
	public void syncLayout(String meetingID, String requesterID, String layoutID) {
		bbbInGW.syncLayout(meetingID, requesterID, layoutID);
	}
	
	public void broadcastLayout(String meetingID, String requesterID, String layoutID, Boolean locked) {
		bbbInGW.broadcastLayout(meetingID, requesterID, layoutID, locked);
	}

	public void unlockLayout(String meetingID, String requesterID) {
		bbbInGW.unlockLayout(meetingID, requesterID); 
	}

	public void getCurrentLayout(String meetingID, String requesterID) {
		bbbInGW.getCurrentLayout(meetingID, requesterID);
	}
}
