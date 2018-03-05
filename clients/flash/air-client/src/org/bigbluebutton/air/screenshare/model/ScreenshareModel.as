package org.bigbluebutton.air.screenshare.model
{
	import org.bigbluebutton.air.screenshare.signals.ScreenshareStreamStartedSignal;
	import org.bigbluebutton.air.screenshare.signals.ScreenshareStreamStoppedSignal;

	public class ScreenshareModel implements IScreenshareModel
	{
		[Inject]
		public var screenshareStreamStartedSignal:ScreenshareStreamStartedSignal;
		
		[Inject]
		public var screenshareStreamStoppedSignal:ScreenshareStreamStoppedSignal;
		
		private var _isScreenSharing:Boolean = false;
		private var _stream:ScreenshareStream = new ScreenshareStream();
		
		public function get isSharing():Boolean {
			return _isScreenSharing;
		}
		
		public function get width():int {
			return _stream.width;
		}
		
		public function set width(w:int):void {
			_stream.width = w;
		}
		
		public function get height():int {
			return _stream.height;
		}
		
		public function set height(h:int):void {
			_stream.height = h;
		}
		
		public function get url():String {
			return _stream.url;
		}
		
		public function set url(u:String):void {
			_stream.url = u;
		}
		
		public function get streamId():String {
			return _stream.streamId;
		}
		
		public function set streamId(s:String):void {
			_stream.streamId = s;
		}
		
		public function get authToken():String {
			return _stream.authToken;
		}
		
		public function set authToken(token:String):void {
			_stream.authToken = token;
		}
		
		public function get jnlp():String {
			return _stream.jnlp;
		}
		
		public function set jnlp(j:String):void {
			_stream.jnlp = j;
		}
		
		public function get session():String {
			return _stream.session;
		}
		
		public function set session(j:String):void {
			_stream.session = j;
		}
		
		public function screenshareStreamRunning(streamId:String, width:int, height:int, url:String, session:String):void {
			this.streamId = streamId;
			this.width = width;
			this.height = height;
			this.url = url;
			this.session = session;
			
			screenshareStreamStartedSignal.dispatch();
		}
		
		public function screenshareStreamStarted(streamId:String, width:int, height:int, url:String):void {
			this.streamId = streamId;
			this.width = width;
			this.height = height;
			this.url = url;
			screenshareStreamStartedSignal.dispatch();
		}
		
		public function screenshareStreamStopped(session:String, reason:String):void {
			if (this.session == session) {
				screenshareStreamStoppedSignal.dispatch(session, reason);
			}
		}
		
	}
}