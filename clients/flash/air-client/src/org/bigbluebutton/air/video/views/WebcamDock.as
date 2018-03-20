package org.bigbluebutton.air.video.views {
	import flash.media.Video;
	import flash.net.NetConnection;
	
	import mx.graphics.SolidColor;
	
	import org.bigbluebutton.air.common.views.VideoView;
	
	import spark.components.BorderContainer;
	import spark.components.Group;
	import spark.primitives.Rect;
	
	public class WebcamDock extends Group {
		
		private var _video:VideoView;
		
		public function WebcamDock() {
			super();
			
			_video = new VideoView();
			_video.percentHeight = 100;
			_video.percentWidth = 100;
			addElement(_video);
		}
		
		public function startStream(connection:NetConnection, name:String, streamName:String, userId:String, oWidth:Number, oHeight:Number):void {
			_video.startStream(connection, name, streamName, userId, oWidth, oHeight);
		}
		
		public function closeStream():void {
			_video.close();
		}
	}
}
