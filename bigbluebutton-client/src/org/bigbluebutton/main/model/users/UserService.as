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
package org.bigbluebutton.main.model.users
{
	import com.asfusion.mate.events.Dispatcher;
	
	import flash.events.TimerEvent;
	import flash.external.ExternalInterface;
	import flash.net.NetConnection;
	import flash.utils.Timer;
	
	import mx.collections.ArrayCollection;
	
	import org.bigbluebutton.common.LogUtil;
	import org.bigbluebutton.core.BBB;
	import org.bigbluebutton.core.UsersUtil;
	import org.bigbluebutton.core.events.LockControlEvent;
	import org.bigbluebutton.core.events.VoiceConfEvent;
	import org.bigbluebutton.core.managers.ConfigManager;
	import org.bigbluebutton.core.managers.ConnectionManager;
	import org.bigbluebutton.core.managers.UserConfigManager;
	import org.bigbluebutton.core.managers.UserManager;
	import org.bigbluebutton.core.model.Config;
	import org.bigbluebutton.common.Role;
	import org.bigbluebutton.main.events.BBBEvent;
	import org.bigbluebutton.main.events.SuccessfulLoginEvent;
	import org.bigbluebutton.main.events.UserServicesEvent;
	import org.bigbluebutton.main.events.ResponseModeratorEvent;
	import org.bigbluebutton.main.events.LogoutEvent;
	import org.bigbluebutton.main.model.ConferenceParameters;
	import org.bigbluebutton.main.model.users.events.BroadcastStartedEvent;
	import org.bigbluebutton.main.model.users.events.BroadcastStoppedEvent;
	import org.bigbluebutton.main.model.users.events.ChangeRoleEvent;
	import org.bigbluebutton.main.model.users.events.ConferenceCreatedEvent;
	import org.bigbluebutton.main.model.users.events.KickUserEvent;
	import org.bigbluebutton.main.model.users.events.ChangeStatusEvent
	import org.bigbluebutton.main.model.users.events.RoleChangeEvent;
	import org.bigbluebutton.main.model.users.events.UsersConnectionEvent;
	import org.bigbluebutton.modules.users.services.MessageReceiver;
	import org.bigbluebutton.modules.users.services.MessageSender;

	public class UserService {
    private static const LOG:String = "Users::UserService - ";
    
		private var joinService:JoinService;
		private var _conferenceParameters:ConferenceParameters;		
		private var applicationURI:String;
		private var hostURI:String;		
		private var connection:NetConnection;
		private var dispatcher:Dispatcher;
		private var reconnecting:Boolean = false;
		
    private var _connectionManager:ConnectionManager;
    private var msgReceiver:MessageReceiver = new MessageReceiver();
    private var sender:MessageSender = new MessageSender();
    
		public function UserService() {
			dispatcher = new Dispatcher();
			msgReceiver.onAllowedToJoin = function():void {
				onAllowedToJoin();
			}
		}

		private function onAllowedToJoin():void {
			sender.queryForParticipants();
			sender.queryForRecordingStatus();
			sender.queryForGuestPolicy();

			var loadCommand:SuccessfulLoginEvent = new SuccessfulLoginEvent(SuccessfulLoginEvent.USER_LOGGED_IN);
			loadCommand.conferenceParameters = _conferenceParameters;
			dispatcher.dispatchEvent(loadCommand);
		}
		
		public function startService(e:UserServicesEvent):void {
			applicationURI = e.applicationURI;
			hostURI = e.hostURI;
			BBB.initConnectionManager().isTunnelling = e.isTunnelling;
      
			joinService = new JoinService();
			joinService.addJoinResultListener(joinListener);
			joinService.load(e.hostURI);
		}
		
		private function joinListener(success:Boolean, result:Object):void {
			if (success) {
				var config:Config = BBB.initConfigManager().config;
				
				UserManager.getInstance().getConference().setMyName(result.username);
				UserManager.getInstance().getConference().setMyRole(result.role);
				UserManager.getInstance().getConference().setMyRoom(result.room);
				UserManager.getInstance().getConference().setGuest(result.guest == "true");
				UserManager.getInstance().getConference().setMyAuthToken(result.authToken);
				UserManager.getInstance().getConference().setMyCustomData(result.customdata);
				UserManager.getInstance().getConference().setDefaultLayout(result.defaultLayout);
				
				UserManager.getInstance().getConference().setMyUserid(result.internalUserId);
				
				UserManager.getInstance().getConference().externalMeetingID = result.externMeetingID;
				UserManager.getInstance().getConference().meetingName = result.conferenceName;
				UserManager.getInstance().getConference().internalMeetingID = result.room;
				UserManager.getInstance().getConference().externalUserID = result.externUserID;
				UserManager.getInstance().getConference().avatarURL = result.avatarURL;
				UserManager.getInstance().getConference().voiceBridge = result.voicebridge;
				UserManager.getInstance().getConference().dialNumber = result.dialnumber;
				UserManager.getInstance().getConference().record = (result.record != "false");
				
        
        
				_conferenceParameters = new ConferenceParameters();
				_conferenceParameters.meetingName = result.conferenceName;
				_conferenceParameters.externMeetingID = result.externMeetingID;
				_conferenceParameters.conference = result.conference;
				_conferenceParameters.username = result.username;
				_conferenceParameters.guest = (result.guest.toUpperCase() == "TRUE");
				_conferenceParameters.role = result.role;
				_conferenceParameters.room = result.room;
        _conferenceParameters.authToken = result.authToken;
				_conferenceParameters.webvoiceconf = result.webvoiceconf;
				_conferenceParameters.voicebridge = result.voicebridge;
				_conferenceParameters.welcome = result.welcome;
				_conferenceParameters.meetingID = result.meetingID;
				_conferenceParameters.externUserID = result.externUserID;
				_conferenceParameters.internalUserID = result.internalUserId;
				_conferenceParameters.logoutUrl = processLogoutUrl(result);
				_conferenceParameters.record = (result.record != "false");
				
				var muteOnStart:Boolean;
				try {
					muteOnStart = (config.meeting.@muteOnStart.toUpperCase() == "TRUE");
				} catch(e:Error) {
					muteOnStart = false;
				}
				
				_conferenceParameters.muteOnStart = muteOnStart;
				_conferenceParameters.lockSettings = UserManager.getInstance().getConference().getLockSettings().toMap();
				
				trace("_conferenceParameters.muteOnStart = " + _conferenceParameters.muteOnStart);
				
				// assign the meeting name to the document title
				ExternalInterface.call("setTitle", _conferenceParameters.meetingName);
				
				trace(LOG + " Got the user info from web api.");       
				/**
				 * Temporarily store the parameters in global BBB so we get easy access to it.
				 */
				var ucm:UserConfigManager = BBB.initUserConfigManager();
				ucm.setConferenceParameters(_conferenceParameters);
				var e:ConferenceCreatedEvent = new ConferenceCreatedEvent(ConferenceCreatedEvent.CONFERENCE_CREATED_EVENT);
				e.conference = UserManager.getInstance().getConference();
				dispatcher.dispatchEvent(e);
				
				connect();
			}
		}

		private function processLogoutUrl(confInfo:Object):String {
			var logoutUrl:String = confInfo.logoutUrl;
			var rules:Object = {
				"%%FULLNAME%%": confInfo.username,
				"%%CONFNAME%%": confInfo.conferenceName,
				"%%DIALNUM%%": confInfo.dialnumber,
				"%%CONFNUM%%": confInfo.voicebridge
			}

			for (var attr:String in rules) {
				logoutUrl = logoutUrl.replace(new RegExp(attr, "g"), rules[attr]);
			}

			return logoutUrl;
		}
		
    private function connect():void{
      _connectionManager = BBB.initConnectionManager();
      _connectionManager.setUri(applicationURI);
      _connectionManager.connect(_conferenceParameters);
    }
	
    public function logoutUser():void {
      disconnect(true);
    }
    
    public function disconnect(onUserAction:Boolean):void {
      _connectionManager.disconnect(onUserAction);
    }
      
    private function queryForRecordingStatus():void {
      sender.queryForRecordingStatus();
    }
    
    public function changeRecordingStatus(e:BBBEvent):void {
      trace(LOG + "changeRecordingStatus")
      if (this.isModerator() && !e.payload.remote) {
        var myUserId: String = UserManager.getInstance().getConference().getMyUserId();
        sender.changeRecordingStatus(myUserId, e.payload.recording);
      }
    }
       
		public function userLoggedIn(e:UsersConnectionEvent):void{
      trace(LOG + "userLoggedIn - Setting my userid to [" + e.userid + "]");
			UserManager.getInstance().getConference().setMyUserid(e.userid);
			_conferenceParameters.userid = e.userid;

			var waitingForAcceptance:Boolean = true;
			if (UserManager.getInstance().getConference().hasUser(e.userid)) {
				trace(LOG + "userLoggedIn - conference has this user");
				waitingForAcceptance = UserManager.getInstance().getConference().getUser(e.userid).waitingForAcceptance;
			}

			if (reconnecting && !waitingForAcceptance) {
				trace(LOG + "userLoggedIn - reconnecting and allowed to join");
				onAllowedToJoin();
				reconnecting = false;
			}
		}
		
		public function denyGuest():void {
			dispatcher.dispatchEvent(new LogoutEvent(LogoutEvent.MODERATOR_DENIED_ME));
		}

		public function setGuestPolicy(event:BBBEvent):void {
			sender.setGuestPolicy(event.payload['guestPolicy']);
		}

		public function guestDisconnect():void {
			_connectionManager.guestDisconnect();
		}
					
		public function isModerator():Boolean {
			return UserManager.getInstance().getConference().amIModerator();
		}
		
		public function get participants():ArrayCollection {
			return UserManager.getInstance().getConference().users;
		}
				
		public function addStream(e:BroadcastStartedEvent):void {
      sender.addStream(e.userid, e.stream);
		}
		
		public function removeStream(e:BroadcastStoppedEvent):void {			
      sender.removeStream(e.userid, e.stream);
		}
		
		public function changeStatus(e:ChangeStatusEvent):void {
			sender.changeStatus(e.userId, e.getStatusName());
		}

		public function responseToGuest(e:ResponseModeratorEvent):void {
			sender.responseToGuest(e.userid, e.resp);
		}
		
		public function kickUser(e:KickUserEvent):void{
			if (this.isModerator()) sender.kickUser(e.userid);
		}

		public function changeRole(e:ChangeRoleEvent):void {
			if (this.isModerator()) sender.changeRole(e.userid, e.role);
		}

		public function onReconnecting():void {
			trace(LOG + "onReconnecting");
			reconnecting = true;
		}
		
		/**
		 * Assign a new presenter 
		 * @param e
		 * 
		 */		
		public function assignPresenter(e:RoleChangeEvent):void{
			var assignTo:String = e.userid;
			var name:String = e.username;
      sender.assignPresenter(assignTo, name, 1);
		}

    public function muteUnmuteUser(command:VoiceConfEvent):void {
      sender.muteUnmuteUser(command.userid, command.mute);		
    }
    
    public function muteAllUsers(command:VoiceConfEvent):void {	
      sender.muteAllUsers(true);			
    }
    
    public function unmuteAllUsers(command:VoiceConfEvent):void{
      sender.muteAllUsers(false);
    }
       
    public function muteAllUsersExceptPresenter(command:VoiceConfEvent):void {	
      sender.muteAllUsersExceptPresenter(true);
    }
        
    public function ejectUser(command:VoiceConfEvent):void {
      sender.ejectUser(command.userid);			
    }	
    
    //Lock events
    public function lockAllUsers(command:LockControlEvent):void {
      sender.setAllUsersLock(true);			
    }
    
    public function unlockAllUsers(command:LockControlEvent):void {	
      sender.setAllUsersLock(false);			
    }
    
    public function lockAlmostAllUsers(command:LockControlEvent):void {	
      var pres:BBBUser = UserManager.getInstance().getConference().getPresenter();
      sender.setAllUsersLock(true, [pres.userID]);
    }
    
    public function lockUser(command:LockControlEvent):void {	
      sender.setUserLock(command.internalUserID, true);			
    }
    
    public function unlockUser(command:LockControlEvent):void {	
      sender.setUserLock(command.internalUserID, false);			
    }
    
    public function saveLockSettings(command:LockControlEvent):void {	
      sender.saveLockSettings(command.payload);			
    }
	}
}
