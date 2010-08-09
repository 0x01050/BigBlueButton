package org.bigbluebutton.main.model.users.events
{
	import flash.events.Event;

	public class ConnectionFailedEvent extends Event
	{
		public static const UNKNOWN_REASON:String = "unknownReason";
		public static const CONNECTION_FAILED:String = "connectionFailed";
		public static const CONNECTION_CLOSED:String = "connectionClosed";
		public static const INVALID_APP:String = "invalidApp";
		public static const APP_SHUTDOWN:String = "appShutdown";
		public static const CONNECTION_REJECTED:String = "connectionRejected";
		public static const ASYNC_ERROR:String = "asyncError";
		
		public static const CONNECTION_LOST:String = "connectionLost";
		
		public var reason:String;
		
		public function ConnectionFailedEvent()
		{
			super(CONNECTION_LOST, true, false);
		}
	}
}