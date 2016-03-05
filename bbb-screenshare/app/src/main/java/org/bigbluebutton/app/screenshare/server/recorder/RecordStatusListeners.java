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
package org.bigbluebutton.app.screenshare.server.recorder;

import java.util.HashSet;
import java.util.Set;

import org.bigbluebutton.app.screenshare.server.recorder.event.RecordEvent;

public class RecordStatusListeners {
	private final Set<RecordStatusListener> listeners = new HashSet<RecordStatusListener>();
	
	public void addListener(RecordStatusListener l) {
		listeners.add(l);
	}
	
	public void removeListener(RecordStatusListener l) {
		listeners.remove(l);
	}
	
	public void notifyListeners(RecordEvent event) {
		for (RecordStatusListener listener: listeners) {
			listener.notify(event);
		}
	}
}
