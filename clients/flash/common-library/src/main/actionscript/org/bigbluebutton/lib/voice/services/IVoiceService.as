package org.bigbluebutton.lib.voice.services {
	
	public interface IVoiceService {
		function setupMessageSenderReceiver():void;
		function mute(userId:String):void;
		function unmute(userId:String):void;
	}
}
