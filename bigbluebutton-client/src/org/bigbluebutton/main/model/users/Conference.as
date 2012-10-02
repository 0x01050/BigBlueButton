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
package org.bigbluebutton.main.model.users {
	import mx.collections.ArrayCollection;
	
	import org.bigbluebutton.common.LogUtil;
	import org.bigbluebutton.common.Role;
	import org.bigbluebutton.core.BBB;

	public class Conference {		
//		private var _myUserid:Number;		
		[Bindable] private var me:BBBUser = null;		
		[Bindable] public var users:ArrayCollection = null;			
				
		public function Conference():void {
      LogUtil.debug("******************************************************************************* Initializing Conference");
			me = new BBBUser();
			users = new ArrayCollection();
		}

		public function addUser(newuser:BBBUser):void {
      LogUtil.debug("Adding new user [" + newuser.userid + "]");
			if (! hasParticipant(newuser.userid)) {
        LogUtil.debug("Am I this new user [" + newuser.userid + ", " + me.userid + "]");
				if (newuser.userid == me.userid) {
					newuser.me = true;
				}						
				
				users.addItem(newuser);
				sort();
			}					
		}

		public function hasParticipant(userID:String):Boolean {
			var p:Object = getParticipantIndex(userID);
			if (p != null) {
				return true;
			}
						
			return false;		
		}
		
		public function hasOnlyOneModerator():Boolean {
			var p:BBBUser;
			var moderatorCount:int = 0;
			
			for (var i:int = 0; i < users.length; i++) {
				p = users.getItemAt(i) as BBBUser;				
				if (p.role == Role.MODERATOR) {
					moderatorCount++;
				}
			}				
			
			if (moderatorCount == 1) return true;
			return false;			
		}
		
		public function getTheOnlyModerator():BBBUser {
			var p:BBBUser;
			for (var i:int = 0; i < users.length; i++) {
				p = users.getItemAt(i) as BBBUser;				
				if (p.role == Role.MODERATOR) {
					return BBBUser.copy(p);
				}
			}		
			
			return null;	
		}
		
		public function getPresenter():BBBUser {
			var p:BBBUser;
			for (var i:int = 0; i < users.length; i++) {
				p = users.getItemAt(i) as BBBUser;	
				if (isUserPresenter(p.userid)) {
					return BBBUser.copy(p);
				}
			}		
			
			return null;
		}
		
		public function getParticipant(userID:String):BBBUser {
			var p:Object = getParticipantIndex(userID);
			if (p != null) {
				return p.participant as BBBUser;
			}
						
			return null;				
		}

		public function isUserPresenter(userID:String):Boolean {
			var user:Object = getParticipantIndex(userID);
			if (user == null) {
				LogUtil.warn("User not found with id=" + userID);
				return false;
			}
			var a:BBBUser = user.participant as BBBUser;
			return a.presenter;
		}
			
		public function removeParticipant(userID:String):void {
			var p:Object = getParticipantIndex(userID);
			if (p != null) {
				LogUtil.debug("removing user[" + p.participant.name + "," + p.participant.userid + "]");				
				users.removeItemAt(p.index);
				sort();
			}							
		}
		
		/**
		 * Get the index number of the participant with the specific userid 
		 * @param userid
		 * @return -1 if participant not found
		 * 
		 */		
		private function getParticipantIndex(userID:String):Object {
			var aUser : BBBUser;
			
			for (var i:int = 0; i < users.length; i++) {
				aUser = users.getItemAt(i) as BBBUser;
				
				if (aUser.userid == userID) {
					return {index:i, participant:aUser};
				}
			}				
			
			// Participant not found.
			return null;
		}
	
		public function amIPresenter():Boolean {
			return me.presenter;
		}
		
		public function setMePresenter(presenter:Boolean):void {
			me.presenter = presenter;
		}
				
		public function amIThisUser(userID:String):Boolean {
			return me.userid == userID;
		}
				
		public function amIModerator():Boolean {
			return me.role == Role.MODERATOR;
		}

		public function muteMyVoice(mute:Boolean):void {
			voiceMuted = mute;
		}
		
		public function isMyVoiceMuted():Boolean {
			return me.voiceMuted;
		}
		
		[Bindable]
		public function set voiceMuted(m:Boolean):void {
			me.voiceMuted = m;
		}
		
		public function get voiceMuted():Boolean {
			return me.voiceMuted;
		}
		
		public function setMyVoiceUserId(userID:int):void {
			me.voiceUserid = userID;
		}
		
		public function getMyVoiceUserId():Number {
			return me.voiceUserid;
		}
		
		public function amIThisVoiceUser(userID:int):Boolean {
			return me.voiceUserid == userID;
		}
		
		public function setMyVoiceJoined(joined:Boolean):void {
			voiceJoined = joined;
		}
		
		public function amIVoiceJoined():Boolean {
			return me.voiceJoined;
		}
		
		/** Hook to make the property Bindable **/
		[Bindable]
		public function set voiceJoined(j:Boolean):void {
			me.voiceJoined = j;			
		}
		
		public function get voiceJoined():Boolean {
			return me.voiceJoined;
		}
		
		[Bindable]
		public function set voiceLocked(locked:Boolean):void {
			me.voiceLocked = locked;
		}
		
		public function get voiceLocked():Boolean {
			return me.voiceLocked;
		}
		
		public function getMyUserId():String {
			return me.userid;
		}
    
		public function setMyUserid(userID:String):void {
			me.userid = userID;
      LogUtil.debug("Setting my userid to [" + me.userid + "]");
		}
		
		public function setMyName(name:String):void {
			me.name = name;
		}
		
		public function getMyName():String {
			return me.name;
		}
		
		public function setMyRole(role:String):void {
			me.role = role;
		}
		
		public function setMyRoom(room:String):void {
			me.room = room;
		}
		
		public function setMyAuthToken(token:String):void {
			me.authToken = token;
		}
		
		public function removeAllParticipants():void {
			users.removeAll();
		}		
	
		public function newUserStatus(userID:String, status:String, value:Object):void {
			var aUser:BBBUser = getParticipant(userID);			
			if (aUser != null) {
				var s:Status = new Status(status, value);
				aUser.changeStatus(s);
			}	
			
			sort();		
		}
		
		/**
		 * Sorts the users by name 
		 * 
		 */		
		private function sort():void {
			users.source.sortOn("name", Array.CASEINSENSITIVE);	
			users.refresh();				
		}				
	}
}