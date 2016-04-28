package org.bigbluebutton.web.main.models {
	import org.osflash.signals.Signal;
	
	public class UISession implements IUISession {
		private var _loading:Boolean = true;
		private var _loadingMessage:String = "Loading";
		private var _loadingChangeSignal:Signal = new Signal();
		
		private var _participantsOpen:Boolean = false;
		private var _participantsOpenSignal:Signal = new Signal();
		
		public function UISession() {
		}
		
		public function get loading():Boolean {
			return _loading;
		}
		
		public function get loadingMessage():String {
			return _loadingMessage;
		}
		
		public function get loadingChangeSignal():Signal {
			return _loadingChangeSignal;
		}
		
		public function setLoading(val:Boolean, message:String):void {
			_loading = val;
			_loadingMessage = message;
			_loadingChangeSignal.dispatch(_loading, _loadingMessage);
		}
		
		public function get participantsOpen():Boolean {
			return _participantsOpen;
		}
		
		public function set participantsOpen(val:Boolean):void {
			_participantsOpen = val;
			_participantsOpenSignal.dispatch();
		}
		
		public function get participantsOpenSignal():Signal {
			return _participantsOpenSignal;
		}
	}
}
