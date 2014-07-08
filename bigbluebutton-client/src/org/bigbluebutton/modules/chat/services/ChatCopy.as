package org.bigbluebutton.modules.chat.services
{
  import flash.events.Event;
  import flash.system.System;
  
  import org.bigbluebutton.core.UsersUtil;
  import org.bigbluebutton.modules.chat.model.ChatMessages;
  import org.bigbluebutton.modules.chat.events.ChatCopyEvent;
  
  public class ChatCopy
  {
  	public function ChatCopy(){

  	}
	
	public function copyAllText(e:ChatCopyEvent):void{
		var chat:ChatMessages = e.chatMessages;
		System.setClipboard(chat.getAllMessageAsString());
	}
  }
}

