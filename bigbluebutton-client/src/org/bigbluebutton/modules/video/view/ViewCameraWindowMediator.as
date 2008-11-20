package org.bigbluebutton.modules.video.view
{
	import flash.events.Event;
	
	import org.bigbluebutton.modules.video.VideoModuleConstants;
	import org.bigbluebutton.modules.video.model.MediaProxy;
	import org.bigbluebutton.modules.video.model.business.MediaType;
	import org.bigbluebutton.modules.video.model.vo.PlayMedia;
	import org.bigbluebutton.modules.video.view.components.ViewCameraWindow;
	import org.bigbluebutton.modules.video.view.events.CloseViewCameraWindowEvent;
	import org.bigbluebutton.modules.video.view.events.StartPlayStreamEvent;
	import org.bigbluebutton.modules.video.view.events.StopPlayStreamEvent;
	import org.puremvc.as3.multicore.interfaces.IMediator;
	import org.puremvc.as3.multicore.interfaces.INotification;
	import org.puremvc.as3.multicore.patterns.mediator.Mediator;
	
	public class ViewCameraWindowMediator extends Mediator implements IMediator
	{
		public static const NAME:String = "ViewCameraWindowMediator";

		private var _viewCamWindow:ViewCameraWindow;
		private var _stream:String;
		
		public function ViewCameraWindowMediator(name:String, streamName:String)
		{
			super(name);
			_stream = streamName;
			_viewCamWindow = new ViewCameraWindow();
			_viewCamWindow.streamName = _stream;
			_viewCamWindow.addEventListener(CloseViewCameraWindowEvent.CLOSE_VIEW_CAMERA_WINDOW_EVENT, onCloseViewCameraWindowEvent);
			_viewCamWindow.addEventListener(StartPlayStreamEvent.START_PLAY_STREAM_EVENT, onStartPlayStreamEvent);
			_viewCamWindow.addEventListener(StopPlayStreamEvent.STOP_PLAY_STREAM_EVENT, onStopPlayStreamEvent);
		}
		
		private function onStartPlayStreamEvent(e:StartPlayStreamEvent):void {
			_viewCamWindow.media = proxy.getPlayMedia(e.streamName) as PlayMedia;
		}
		
		private function onStopPlayStreamEvent(e:StopPlayStreamEvent):void {
			if (e.streamName != _stream) return;
			
			proxy.stopStream(e.streamName);
			proxy.removeStream(MediaType.PLAY, e.streamName);
		}
		
		override public function listNotificationInterests():Array{ 
			return [
					VideoModuleConstants.PLAY_STREAM,
					VideoModuleConstants.STOP_STREAM
					];
		}
		
		override public function handleNotification(notification:INotification):void{
			var streamName:String = notification.getBody().streamName;
			
			if (streamName != _stream) return;
			
			switch(notification.getName()){
				case VideoModuleConstants.PLAY_STREAM:
					proxy.createPlayMedia(streamName);
					proxy.setupStream(streamName);

					_viewCamWindow.media = proxy.getPlayMedia(streamName) as PlayMedia;
					proxy.playStream(streamName,true,false);

					_viewCamWindow.width = 330;
				   	_viewCamWindow.height = 270;
				   	_viewCamWindow.title = "Viewing Camera";
				   	_viewCamWindow.showCloseButton = true;
				   	_viewCamWindow.xPosition = 700;
				   	_viewCamWindow.yPosition = 240;
					facade.sendNotification(VideoModuleConstants.ADD_WINDOW, _viewCamWindow);		
					break;
				case VideoModuleConstants.STOP_STREAM:

					break;	
			}
		}
		
		private function onCloseViewCameraWindowEvent(e:CloseViewCameraWindowEvent):void{
			facade.sendNotification(VideoModuleConstants.REMOVE_WINDOW, _viewCamWindow);
			facade.sendNotification(VideoModuleConstants.STOP_VIEW_CAMERA, _stream);
		}
		
		private function stopStream(e:Event):void{
			//mainApp.publisherApp.stopStream(media.streamName);
//			sendNotification(VideoModuleConstants.STOP_STREAM_COMMAND, videoWindow.media.streamName);
		}

		private function get proxy():MediaProxy {
			return facade.retrieveProxy(MediaProxy.NAME) as MediaProxy;
		}
	}
}