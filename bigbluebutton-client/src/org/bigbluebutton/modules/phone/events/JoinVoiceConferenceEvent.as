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
package org.bigbluebutton.modules.phone.events
{
	import flash.events.Event;

	public class JoinVoiceConferenceEvent extends Event
	{
		
		public static const JOIN_VOICE_CONFERENCE_EVENT:String = 'JOIN_VOICE_CONFERENCE_EVENT';
		
		//For automation testing. Is not a test unless set to true. If true, skip microphone initialization
		public var test:Boolean = false;
		
		public function JoinVoiceConferenceEvent(bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(JOIN_VOICE_CONFERENCE_EVENT, bubbles, cancelable);
		}
		
	}
}