package org.bigbluebutton.air.common.views
{
	import mx.core.UIComponent;
	
	import spark.components.Image;
	
	import org.bigbluebutton.BBBRtmpPlayer;
	import org.bigbluebutton.BBBRtmpPlayerEvent;

	public class IOSVideoView extends UIComponent {
		
		protected var _image:Image;
		
		protected var player:BBBRtmpPlayer;
		
		protected var originalVideoWidth:Number;
		
		protected var originalVideoHeight:Number;
		
		public function startStream(uri:String, streamName:String, imgWidth:Number, imgHeight:Number, meetingId:String, authToken:String, externalUserId:String):void {
			
			if(player) {
				close();
			}

			_image = new Image();
			if (numChildren == 0 ) {
				addChild(_image);	
			}
			
			this.originalVideoWidth = imgWidth;
			this.originalVideoHeight = imgHeight;
			
			var url:String = uri + "/" + streamName + " live=1 conn=S:" + meetingId + " conn=S:" + externalUserId + " conn=S:" + authToken;
			
			player = new BBBRtmpPlayer(url);
			
			player.addEventListener(BBBRtmpPlayerEvent.NEW_IMAGE, onNewImage);
			player.addEventListener(BBBRtmpPlayerEvent.CONNECTING, onConnecting);
			player.addEventListener(BBBRtmpPlayerEvent.CONNECTION_FAILED, onConnectionFailed);
			player.addEventListener(BBBRtmpPlayerEvent.DISCONNECTED, onDisconnected);
			
			player.play();
		}
		
		private function onConnecting(e:BBBRtmpPlayerEvent):void {
			trace("EVENT: " + e.type + " MESSAGE: " + e.getMessage());
		}
		
		private function onNewImage(e:BBBRtmpPlayerEvent):void {
			_image.source = player.getLastImage();
		}
		
		private function onConnectionFailed(e:BBBRtmpPlayerEvent):void {
			close();
		}
		
		private function onDisconnected(e:BBBRtmpPlayerEvent):void {
			close();	
		}
		
		public function close():void {
			player.removeEventListener(BBBRtmpPlayerEvent.NEW_IMAGE, onNewImage);
			player.removeEventListener(BBBRtmpPlayerEvent.CONNECTION_FAILED, onConnectionFailed);
			player.removeEventListener(BBBRtmpPlayerEvent.DISCONNECTED, onDisconnected);
			if (getChildAt(0) == _image) {
				removeChild(_image);
			}
			_image = null;
			player = null;
		}
		
		override protected function updateDisplayList(w:Number, h:Number):void {
			super.updateDisplayList(w, h);
			
			if (player) {
				resizeForPortrait();
			}
		}
		
		public function resizeForPortrait():void {
			// if we have device where screen width less than screen height e.g. phone
			if (width < height) {
				// make the video width full width of the screen 
				_image.width = width;
				// calculate height based on a video width, it order to keep the same aspect ratio
				_image.height = (_image.width / originalVideoWidth) * originalVideoHeight;
				// if calculated height appeared to be bigger than screen height, recalculuate the video size based on width
				if (height < _image.height) {
					// make the video height full height of the screen
					_image.height = height;
					// calculate width based on a video height, it order to keep the same aspect ratio
					_image.width = ((originalVideoWidth * _image.height) / originalVideoHeight);
				}
			} // if we have device where screen height less than screen width e.g. tablet
			else {
				// make the video height full height of the screen
				_image.height = height;
				// calculate width based on a video height, it order to keep the same aspect ratio
				_image.width = ((originalVideoWidth * _image.height) / originalVideoHeight);
				// if calculated width appeared to be bigger than screen width, recalculuate the video size based on height
				if (width < _image.width) {
					// make the video width full width of the screen 
					_image.width = width;
					// calculate height based on a video width, it order to keep the same aspect ratio
					_image.height = (_image.width / originalVideoWidth) * originalVideoHeight;
				}
			}
			
			_image.x = width - _image.width;
			_image.y = height - _image.height;
		}
		
		public function resizeForLandscape():void {
			if (height < width) {
				_image.height = width;
				_image.width = ((originalVideoWidth * _image.height) / originalVideoHeight);
				if (width < _image.width) {
					_image.width = height;
					_image.height = (_image.width / originalVideoWidth) * originalVideoHeight;
				}
			} else {
				_image.width = height;
				_image.height = (_image.width / originalVideoWidth) * originalVideoHeight;
				if (height < _image.height) {
					_image.height = width;
					_image.width = ((originalVideoWidth * _image.height) / originalVideoHeight);
				}
			}
		}
		
		public function rotateVideo(rotation:Number):void {
			if (_image && stage.contains(_image)) {
				removeChild(_image);
			}
			_image = new Image();
			switch (rotation) {
				case 0:
					resizeForPortrait();
					_image.x = width / 2 - _image.width / 2;
					_image.y = height / 2 - _image.height / 2; // + topMenuBarHeight;
					break;
				case -90:
					resizeForLandscape();
					_image.x = (width / 2) - (_image.height / 2);
					_image.y = (height / 2) + (_image.width / 2); // + topMenuBarHeight;
					break;
				case 90:
					resizeForLandscape();
					_image.x = (width / 2) + (_image.height / 2);
					_image.y = (height / 2) - (_image.width / 2); // + topMenuBarHeight;
					break;
				case 180:
					resizeForPortrait();
					_image.x = width / 2 + _image.width / 2;
					_image.y = (height / 2) + (_image.height / 2); // + topMenuBarHeight
					break;
				default:
					break;
			}
			_image.rotation = rotation;
			addChild(_image);
		}	
	}
}