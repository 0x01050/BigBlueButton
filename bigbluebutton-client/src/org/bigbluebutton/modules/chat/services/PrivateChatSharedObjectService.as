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
package org.bigbluebutton.modules.chat.services
{
	import com.asfusion.mate.events.Dispatcher;
	import flash.events.IEventDispatcher;
	import flash.events.SyncEvent;
	import flash.net.NetConnection;
	import flash.net.Responder;
	import flash.net.SharedObject;	
	import org.bigbluebutton.common.LogUtil;
	import org.bigbluebutton.core.managers.UserManager;
	import org.bigbluebutton.main.events.ParticipantJoinEvent;
	import org.bigbluebutton.modules.chat.events.PrivateChatMessageEvent;
	import org.bigbluebutton.modules.chat.vo.ChatMessageVO;
	import org.bigbluebutton.modules.chat.model.MessageVO;

	public class PrivateChatSharedObjectService
	{
		public static const NAME:String = "PrivateChatSharedObjectService";
		
		private var chatSO:SharedObject;
		private var connection:NetConnection;
		private var dispatcher:IEventDispatcher;
		
		private var privateResponder:Responder;
	
		// This participant's userid
		private var userid:String;
		
		public function PrivateChatSharedObjectService(connection:NetConnection, dispatcher:IEventDispatcher)
		{			
			this.connection = connection;
			this.dispatcher = dispatcher;		
			
			privateResponder = new Responder(
				function(result:Object):void{
					LogUtil.debug("Successfully called chat server private message");
				},
				function(status:Object):void{
					LogUtil.error("Error while trying to call privateMessage on server");
				}
			);				
		}
						
	 public function join(userid:String, uri:String):void {
			this.userid = userid;
			chatSO = SharedObject.getRemote(userid, uri, false);
			chatSO.addEventListener(SyncEvent.SYNC, sharedObjectSyncHandler);
			chatSO.client = this;
			chatSO.connect(connection);							
		}
		
	    public function leave():void {
	    	if (chatSO != null) {
	    		chatSO.close();
	    	}
	    }
		
		public function sendMessage(message:MessageVO):void{
			connection.call("chat.privateMessage", privateResponder, message.chatobj, message.sender , message.recepient);
			
			sendMessageToSelf(message);
		}
		
		private function sendMessageToSelf(message:MessageVO):void {
			messageReceived(message.recepient, message.chatobj);
		}
		
		public function messageReceived(from:String, chatobj:ChatMessageVO):void {
			var event:PrivateChatMessageEvent = new PrivateChatMessageEvent(PrivateChatMessageEvent.PRIVATE_CHAT_MESSAGE_EVENT);
			
			event.message = new MessageVO(chatobj, from, this.userid);
			
			//trace("Sending private message " + message);
			var globalDispatcher:Dispatcher = new Dispatcher();
			globalDispatcher.dispatchEvent(event);	 
		}
		
		private function sharedObjectSyncHandler(event:SyncEvent) : void
		{	
			trace("Connection to private shared object successful.");
		}
	}
}