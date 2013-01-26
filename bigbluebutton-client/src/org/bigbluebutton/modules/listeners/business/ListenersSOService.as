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
package org.bigbluebutton.modules.listeners.business
{
	import com.asfusion.mate.events.Dispatcher;	
	import flash.events.AsyncErrorEvent;
	import flash.events.NetStatusEvent;
	import flash.net.NetConnection;
	import flash.net.Responder;
	import flash.net.SharedObject;	
	import org.bigbluebutton.common.LogUtil;
	import org.bigbluebutton.core.EventConstants;
	import org.bigbluebutton.core.UsersUtil;
	import org.bigbluebutton.core.events.CoreEvent;
	import org.bigbluebutton.core.managers.UserManager;
	import org.bigbluebutton.main.events.BBBEvent;
	import org.bigbluebutton.main.model.users.BBBUser;
	import org.bigbluebutton.modules.listeners.business.vo.Listener;
	import org.bigbluebutton.modules.listeners.business.vo.Listeners;
	import org.bigbluebutton.modules.listeners.events.ListenersEvent;

	public class ListenersSOService
	{
		private static const LOGNAME:String = "[ListenersSOService]";		
		private var _listenersSO : SharedObject;
		private static const SHARED_OBJECT:String = "meetMeUsersSO";		
		private var _listeners:Listeners;
		private var netConnectionDelegate: NetConnectionDelegate;
		private var _msgListener:Function;
		private var _connectionListener:Function;
		private var _uri:String;
		private var nc_responder : Responder;
		private var _soErrors:Array;
		private var pingCount:int = 0;
		private var _module:ListenersModule;
		private var dispatcher:Dispatcher;
		
		private static var globalDispatcher:Dispatcher = new Dispatcher();
							
		public function ListenersSOService(listeners:Listeners, module:ListenersModule)
		{			
			_listeners = listeners;		
			_module = module;			
			dispatcher = new Dispatcher();
		}
		
		public function connect(uri:String):void {
			_uri = uri;
			join();
		}
		
		public function disconnect():void {
			leave();
		}
		
		private function connectionListener(connected:Boolean, errors:Array=null):void {
			if (connected) {
				LogUtil.debug(LOGNAME + ":Connected to the VOice application");
				join();
			} else {
				leave();
				LogUtil.debug(LOGNAME + ":Disconnected from the Voice application");
				notifyConnectionStatusListener(false, errors);
			}
		}
		
	    private function join() : void
		{
			trace("ListenertsSOService " + _module.uri);
			_listenersSO = SharedObject.getRemote(SHARED_OBJECT, _module.uri, false);
			_listenersSO.addEventListener(NetStatusEvent.NET_STATUS, netStatusHandler);
			_listenersSO.addEventListener(AsyncErrorEvent.ASYNC_ERROR, asyncErrorHandler);
			_listenersSO.client = this;
			_listenersSO.connect(_module.connection);
			LogUtil.debug(LOGNAME + ":Voice is connected to Shared object");
			notifyConnectionStatusListener(true);		
				
			// Query the server if there are already listeners in the conference.
			getCurrentUsers();
			getRoomMuteState();
		}
		
	    private function leave():void
	    {
	    	if (_listenersSO != null) {
	    		_listenersSO.close();
	    	}
	    	notifyConnectionStatusListener(false);
	    }

		public function addConnectionStatusListener(connectionListener:Function):void {
			_connectionListener = connectionListener;
		}
				
		public function userJoin(userId:Number, cidName:String, cidNum:String, muted:Boolean, talking:Boolean, locked:Boolean):void {
			if (! _listeners.hasListener(userId)) {
				var n:Listener = new Listener();
				n.callerName = cidName != null ? cidName : "<Unknown Caller>";
				n.callerNumber = cidNum;
				n.muted = muted;
				n.userid = userId;
				n.talking = talking;
				n.locked = locked;
				n.moderator = _module.isModerator();
			
				/**
				 * Let's store the voice userid so we can do push to talk.
				 */
				var pattern:RegExp = /(.*)-(.*)$/;
				var result:Object = pattern.exec(n.callerName);
				if (result != null) {
					/**
					 * The first item is the userid and the second is the username.
					 */
					if (UserManager.getInstance().getConference().amIThisUser(result[1])) {
						UserManager.getInstance().getConference().setMyVoiceUserId(n.userid);						
						UserManager.getInstance().getConference().muteMyVoice(n.muted);
						UserManager.getInstance().getConference().setMyVoiceJoined(true);
					}	
          
          if (UsersUtil.hasUser(result[1])) {
            var bu:BBBUser = UsersUtil.getUser(result[1]);
            bu.voiceUserid = n.userid;
            bu.voiceMuted = n.muted;
            bu.voiceJoined = true;
            
            var bbbEvent:BBBEvent = new BBBEvent(BBBEvent.USER_VOICE_JOINED);
            bbbEvent.payload.userID = bu.userID;            
            globalDispatcher.dispatchEvent(bbbEvent);            
          }
          
					n.callerName = result[2]; /* Store the username */
				}
								
				LogUtil.info(LOGNAME + "Adding listener [" + n.callerName + "," + userId + "]");
				_listeners.addListener(n);
				
//				globalDispatcher.dispatchEvent(new BBBEvent(BBBEvent.USER_VOICE_JOINED, n.callerName));
			} else {
				LogUtil.debug(LOGNAME + "There is a listener with userid " + userId + " " + cidName + " in the conference.");
			}
		}

		public function userMute(userID:Number, mute:Boolean):void {
			var l:Listener = _listeners.getListener(userID);			
			if (l != null) {
				l.muted = mute;
        
        if (l.muted) {
          // When the user is muted, set the talking flag to false so that the UI will not display the
          // user as talking even if muted.
          userTalk(userID, false);
        }
        
				/**
				 * Let's store the voice userid so we can do push to talk.
				 */
				if (UserManager.getInstance().getConference().amIThisVoiceUser(userID)) {
					UserManager.getInstance().getConference().muteMyVoice(l.muted);
				}				
        
        var bu:BBBUser = UsersUtil.getVoiceUser(userID)
        if (bu != null) {
          bu.voiceMuted = l.muted;
          
          LogUtil.debug("[" + bu.name + "] is now muted=[" + bu.voiceMuted + "]");
          
          var bbbEvent:BBBEvent = new BBBEvent(BBBEvent.USER_VOICE_MUTED);
          bbbEvent.payload.muted = mute;
          bbbEvent.payload.userID = bu.userID;
          globalDispatcher.dispatchEvent(bbbEvent);
        }     
			}			     
		}

		public function userLockedMute(userId:Number, locked:Boolean):void {
			var l:Listener = _listeners.getListener(userId);			
			if (l != null) {
				l.locked = locked;

				/**
				 * Let's store the voice userid so we can do push to talk.
				 */
				if (UserManager.getInstance().getConference().amIThisVoiceUser(userId)) {
					UserManager.getInstance().getConference().voiceLocked = l.locked;
				}
        
        var bu:BBBUser = UsersUtil.getVoiceUser(userId)
        if (bu != null) {
          bu.voiceLocked = l.locked;
          LogUtil.debug("[" + bu.name + "] is now locked=[" + bu.voiceLocked + "] muted=[" + bu.voiceMuted + "]");
          
          var bbbEvent:BBBEvent = new BBBEvent(BBBEvent.USER_VOICE_LOCKED);
          bbbEvent.payload.locked = bu.voiceLocked;
          bbbEvent.payload.userID = bu.userID;
          globalDispatcher.dispatchEvent(bbbEvent);
        }   
			}					
		}
		
		public function userTalk(userId:Number, talk:Boolean) : void
		{
      trace("User talking event");
			var l:Listener = _listeners.getListener(userId);			
			if (l != null) {
				l.talking = talk;
        
        var bu:BBBUser = UsersUtil.getVoiceUser(userId);
        if (bu != null) {
          var event:CoreEvent = new CoreEvent(EventConstants.USER_TALKING);
          event.message.userID = bu.userID;
          event.message.talking = l.talking;
          var gd:Dispatcher = new Dispatcher();
          gd.dispatchEvent(event);  
        }
			}	
		}

		public function userLeft(userId:Number):void
		{
			_listeners.removeListener(userId);	
			/**
			 * Let's store the voice userid so we can do push to talk.
			 */
			if (UserManager.getInstance().getConference().amIThisVoiceUser(userId)) {
				UserManager.getInstance().getConference().setMyVoiceJoined(false);
				UserManager.getInstance().getConference().setMyVoiceUserId(0);
				UserManager.getInstance().getConference().setMyVoiceJoined(false);
			}
      
      var bu:BBBUser = UsersUtil.getVoiceUser(userId)
      if (bu != null) {
        bu.voiceUserid = 0;
        bu.voiceMuted = false;
        bu.voiceJoined = false;
        
        var bbbEvent:BBBEvent = new BBBEvent(BBBEvent.USER_VOICE_LEFT);
        bbbEvent.payload.userID = bu.userID;
        globalDispatcher.dispatchEvent(bbbEvent);
      }
		}
		
		public function ping(message:String):void {
			if (pingCount < 100) {
				pingCount++;
			} else {
				var date:Date = new Date();
				var t:String = date.toLocaleTimeString();
				LogUtil.debug(LOGNAME + "[" + t + '] - Received ping from server: ' + message);
				pingCount = 0;
			}
		}
		
		public function lockMuteUser(userid:Number, lock:Boolean):void {
			var nc:NetConnection = _module.connection;
			nc.call(
				"voice.lockMuteUser",// Remote function name
				new Responder(
	        		// participants - On successful result
					function(result:Object):void { 
						LogUtil.debug("Successfully lock mute/unmute: " + userid); 	
					},	
					// status - On error occurred
					function(status:Object):void { 
						LogUtil.error("Error occurred:"); 
						for (var x:Object in status) { 
							LogUtil.error(x + " : " + status[x]); 
							} 
					}
				),//new Responder
				userid,
				lock
			); //_netConnection.call		
		}
		
		public function muteUnmuteUser(userid:Number, mute:Boolean):void
		{
			var nc:NetConnection = _module.connection;
			nc.call(
				"voice.muteUnmuteUser",// Remote function name
				new Responder(
	        		// participants - On successful result
					function(result:Object):void { 
						LogUtil.debug("Successfully mute/unmute: " + userid); 	
					},	
					// status - On error occurred
					function(status:Object):void { 
						LogUtil.error("Error occurred:"); 
						for (var x:Object in status) { 
							LogUtil.error(x + " : " + status[x]); 
						} 
					}
				),//new Responder
				userid,
				mute
			); //_netConnection.call		
		}

		public function muteAllUsers(mute:Boolean):void
		{	
			var nc:NetConnection = _module.connection;
			nc.call(
				"voice.muteAllUsers",// Remote function name
				new Responder(
	        		// participants - On successful result
					function(result:Object):void { 
						LogUtil.debug("Successfully mute/unmute all users: "); 	
					},	
					// status - On error occurred
					function(status:Object):void { 
						LogUtil.error("Error occurred:"); 
						for (var x:Object in status) { 
							LogUtil.error(x + " : " + status[x]); 
							} 
					}
				),//new Responder
				mute
			); //_netConnection.call		
			_listenersSO.send("muteStateCallback", mute);
		}
		
		public function muteStateCallback(mute:Boolean):void{
			var e:ListenersEvent = new ListenersEvent(ListenersEvent.ROOM_MUTE_STATE);
			e.mute_state = mute;
			dispatcher.dispatchEvent(e);
		}
		
		public function ejectUser(userId:Number):void
		{
			var nc:NetConnection = _module.connection;
			nc.call(
				"voice.kickUSer",// Remote function name
				new Responder(
	        		// participants - On successful result
					function(result:Object):void { 
						LogUtil.debug("Successfully kick user: userId"); 	
					},	
					// status - On error occurred
					function(status:Object):void { 
						LogUtil.error("Error occurred:"); 
						for (var x:Object in status) { 
							LogUtil.error(x + " : " + status[x]); 
							} 
					}
				),//new Responder
				userId
			); //_netConnection.call		
		}
		
		private function getCurrentUsers():void {
			var nc:NetConnection = _module.connection;
			nc.call(
				"voice.getMeetMeUsers",// Remote function name
				new Responder(
	        		// participants - On successful result
					function(result:Object):void { 
						LogUtil.debug("Successfully queried participants: " + result.count); 
						if (result.count > 0) {
							for(var p:Object in result.participants) 
							{
								var u:Object = result.participants[p]
								userJoin(u.participant, u.name, u.name, u.muted, u.talking, u.locked);
							}							
						}	
					},	
					// status - On error occurred
					function(status:Object):void { 
						LogUtil.error("Error occurred:"); 
						for (var x:Object in status) { 
							LogUtil.error(x + " : " + status[x]); 
							} 
					}
				)//new Responder
			); //_netConnection.call
		}
		
		public function getRoomMuteState():void{
			var nc:NetConnection = _module.connection;
			nc.call(
				"voice.isRoomMuted",// Remote function name
				new Responder(
					// participants - On successful result
					function(result:Object):void { 
						var e:ListenersEvent = new ListenersEvent(ListenersEvent.ROOM_MUTE_STATE);
						e.mute_state = result as Boolean;
						dispatcher.dispatchEvent(e);
					},	
					// status - On error occurred
					function(status:Object):void { 
						LogUtil.error("Error occurred:"); 
						for (var x:Object in status) { 
							LogUtil.error(x + " : " + status[x]); 
						} 
					}
				)//new Responder
			); //_netConnection.call
		}
		
		private function notifyConnectionStatusListener(connected:Boolean, errors:Array=null):void {
			if (_connectionListener != null) {
				LogUtil.debug(LOGNAME + 'notifying connectionListener for Voice');
				_connectionListener(connected, errors);
			} else {
				LogUtil.debug(LOGNAME + "_connectionListener is null");
			}
		}
		
		private function netStatusHandler (event:NetStatusEvent):void
		{
			var statusCode:String = event.info.code;
			
			switch (statusCode) 
			{
				case "NetConnection.Connect.Success":
					LogUtil.debug(LOGNAME + ":Connection Success");			
					break;
			
				case "NetConnection.Connect.Failed":	
					LogUtil.error(LOGNAME + "ChatSO connection failed.");		
					addError("ChatSO connection failed");
					break;
					
				case "NetConnection.Connect.Closed":			
					LogUtil.error(LOGNAME + "Connection to VoiceSO was closed.");						
					addError("Connection to VoiceSO was closed.");									
					notifyConnectionStatusListener(false, _soErrors);
					break;
					
				case "NetConnection.Connect.InvalidApp":	
					LogUtil.error(LOGNAME + "VoiceSO not found in server");			
					addError("VoiceSO not found in server");	
					break;
					
				case "NetConnection.Connect.AppShutDown":
					LogUtil.error(LOGNAME + "VoiceSO is shutting down");	
					addError("VoiceSO is shutting down");
					break;
					
				case "NetConnection.Connect.Rejected":
					LogUtil.error(LOGNAME + "No permissions to connect to the voiceSO");
					addError("No permissions to connect to the voiceSO");
					break;
					
				default:
				   LogUtil.debug("ListenersSOService :default - " + event.info.code );
				   break;
			}
		}
			
		private function asyncErrorHandler (event:AsyncErrorEvent):void
		{
			LogUtil.error(LOGNAME + "ListenersSO asynchronous error.");
			addError("ListenersSO asynchronous error.");
		}

		private function addError(error:String):void {
			if (_soErrors == null) {
				_soErrors = new Array();
			}
			_soErrors.push(error);
		}
	}
}