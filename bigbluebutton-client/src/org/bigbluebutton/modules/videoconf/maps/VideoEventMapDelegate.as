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
package org.bigbluebutton.modules.videoconf.maps
{
  import flash.events.IEventDispatcher;
  import flash.media.Camera;
  
  import mx.collections.ArrayCollection;
  import mx.collections.ArrayList;
  import mx.events.FlexEvent;
  import mx.utils.ObjectUtil;
  
  import org.bigbluebutton.common.LogUtil;
  import org.bigbluebutton.common.events.CloseWindowEvent;
  import org.bigbluebutton.common.events.OpenWindowEvent;
  import org.bigbluebutton.common.events.ToolbarButtonEvent;
  import org.bigbluebutton.core.BBB;
  import org.bigbluebutton.core.UsersUtil;
  import org.bigbluebutton.core.events.ConnectAppEvent;
  import org.bigbluebutton.core.managers.UserManager;
  import org.bigbluebutton.core.model.VideoProfile;
  import org.bigbluebutton.core.vo.CameraSettingsVO;
  import org.bigbluebutton.main.events.BBBEvent;
  import org.bigbluebutton.main.events.MadePresenterEvent;
  import org.bigbluebutton.main.events.StoppedViewingWebcamEvent;
  import org.bigbluebutton.main.events.UserJoinedEvent;
  import org.bigbluebutton.main.events.UserLeftEvent;
  import org.bigbluebutton.main.model.users.BBBUser;
  import org.bigbluebutton.main.model.users.events.BroadcastStartedEvent;
  import org.bigbluebutton.main.model.users.events.BroadcastStoppedEvent;
  import org.bigbluebutton.main.model.users.events.StreamStartedEvent;
  import org.bigbluebutton.modules.videoconf.business.VideoProxy;
  import org.bigbluebutton.modules.videoconf.business.VideoWindowItf;
  import org.bigbluebutton.modules.videoconf.events.CloseAllWindowsEvent;
  import org.bigbluebutton.modules.videoconf.events.ClosePublishWindowEvent;
  import org.bigbluebutton.modules.videoconf.events.ConnectedEvent;
  import org.bigbluebutton.modules.videoconf.events.OpenVideoWindowEvent;
  import org.bigbluebutton.modules.videoconf.events.ShareCameraRequestEvent;
  import org.bigbluebutton.modules.videoconf.events.StopShareCameraRequestEvent;
  import org.bigbluebutton.modules.videoconf.events.StartBroadcastEvent;
  import org.bigbluebutton.modules.videoconf.events.StopBroadcastEvent;
  import org.bigbluebutton.modules.videoconf.model.VideoConfOptions;
  import org.bigbluebutton.modules.videoconf.views.AvatarWindow;
  import org.bigbluebutton.modules.videoconf.views.GraphicsWrapper;
  import org.bigbluebutton.modules.videoconf.views.PublishWindow;
  import org.bigbluebutton.modules.videoconf.views.ToolbarPopupButton;
  import org.bigbluebutton.modules.videoconf.views.UserAvatar;
  import org.bigbluebutton.modules.videoconf.views.UserGraphic;
  import org.bigbluebutton.modules.videoconf.views.UserGraphicHolder;
  import org.bigbluebutton.modules.videoconf.views.UserVideo;
  import org.bigbluebutton.modules.videoconf.views.VideoDock;
  import org.bigbluebutton.modules.videoconf.views.VideoWindow;


  public class VideoEventMapDelegate
  {
    private var options:VideoConfOptions = new VideoConfOptions();
    private var uri:String;
    
    private var button:ToolbarPopupButton = new ToolbarPopupButton();
    private var proxy:VideoProxy;
    
    private var _dispatcher:IEventDispatcher;
    private var _ready:Boolean = false;
    private var _isPublishing:Boolean = false;
    private var _isPreviewWebcamOpen:Boolean = false;
    private var _isWaitingActivation:Boolean = false;

    private var _videoDock:VideoDock;
    private var _graphics:GraphicsWrapper = new GraphicsWrapper();
    private var streamList:ArrayList = new ArrayList();
    private var numberOfWindows:Object = new Object();

	// Store userID of windows waiting for a NetConnection
	private var pendingVideoWindowsList:Object = new Object();
    
    public function VideoEventMapDelegate(dispatcher:IEventDispatcher)
    {
      _dispatcher = dispatcher;
    }
    
    private function get me():String {
      return UsersUtil.getMyUsername();
    }
    
    public function start(uri:String):void {
      trace("VideoEventMapDelegate:: [" + me + "] Video Module Started.");
      this.uri = uri;

      _videoDock = new VideoDock();
      var windowEvent:OpenWindowEvent = new OpenWindowEvent(OpenWindowEvent.OPEN_WINDOW_EVENT);
      windowEvent.window = _videoDock;
      _dispatcher.dispatchEvent(windowEvent);

      _videoDock.addChild(_graphics);
    }
        
    public function viewCamera(userID:String, stream:String, name:String, mock:Boolean = false):void {
      trace("VideoEventMapDelegate:: [" + me + "] viewCamera. ready = [" + _ready + "]");
      
      if (!_ready) return;
      trace("VideoEventMapDelegate:: [" + me + "] Viewing [" + userID + " stream [" + stream + "]");
      if (! UserManager.getInstance().getConference().amIThisUser(userID)) {
        initPlayConnectionFor(userID);
      }      
    }

    public function handleUserLeftEvent(event:UserLeftEvent):void {
      trace("VideoEventMapDelegate:: [" + me + "] handleUserLeftEvent. ready = [" + _ready + "]");
      
      if (!_ready) return;
      
      closeWindow(event.userID);
    }
    
    public function handleUserJoinedEvent(event:UserJoinedEvent):void {
      trace("VideoEventMapDelegate:: [" + me + "] handleUserJoinedEvent. ready = [" + _ready + "]");
      
      if (!_ready) return;
      
      if (options.displayAvatar) {
        openAvatarWindowFor(event.userID);
      }
    }
    
    private function displayToolbarButton():void {
      button.isPresenter = true;
      
      if (options.presenterShareOnly) {
        if (UsersUtil.amIPresenter()) {
          button.isPresenter = true;
        } else { 
          button.isPresenter = false;
        }
      }
            
    }
    
    private function addToolbarButton():void{
      LogUtil.debug("****************** Adding toolbar button. presenter?=[" + UsersUtil.amIPresenter() + "]");
      if (proxy.videoOptions.showButton) {  

        displayToolbarButton();
        
        var event:ToolbarButtonEvent = new ToolbarButtonEvent(ToolbarButtonEvent.ADD);
        event.button = button;
        event.module="Webcam";
        _dispatcher.dispatchEvent(event);
      }
    }
    
    private function autoStart():void {          
      if (options.skipCamSettingsCheck) {
        skipCameraSettingsCheck();
      } else {
        var dp:Object = [];
        for(var i:int = 0; i < Camera.names.length; i++) {
          dp.push({label: Camera.names[i], status: button.OFF_STATE});    
        }
        button.enabled = false;
        var shareCameraRequestEvent:ShareCameraRequestEvent = new ShareCameraRequestEvent();
        shareCameraRequestEvent.camerasArray = dp;
        _dispatcher.dispatchEvent(shareCameraRequestEvent);
      }
    }

    private function changeDefaultCamForMac():Camera {
      for (var i:int = 0; i < Camera.names.length; i++){
        if (Camera.names[i] == "USB Video Class Video") {
          /** Set as default for Macs */
          return Camera.getCamera("USB Video Class Video");
        }
      }
      
      return null;
    }
    
    private function skipCameraSettingsCheck(camIndex:int = -1):void {
      if (camIndex == -1) {
        var cam:Camera = changeDefaultCamForMac();
        if (cam == null) {
          cam = Camera.getCamera();
        }
        camIndex = cam.index;
      }
      
      var videoProfile:VideoProfile = BBB.defaultVideoProfile;
      initCameraWithSettings(camIndex, videoProfile);
    }
    
    private function openWebcamWindows():void {
      trace("VideoEventMapDelegate:: [" + me + "] openWebcamWindows:: ready = [" + _ready + "]");
      
      var uids:ArrayCollection = UsersUtil.getUserIDs();
      
      for (var i:int = 0; i < uids.length; i++) {
        var u:String = uids.getItemAt(i) as String;
        trace("VideoEventMapDelegate:: [" + me + "] openWebcamWindows:: open window for = [" + u + "]");
        openWebcamWindowFor(u); 
      }
    }
    
    private function openWebcamWindowFor(userID:String):void {      
      trace("VideoEventMapDelegate:: [" + me + "] openWebcamWindowFor:: open window for = [" + userID + "]");
      if (! UsersUtil.isMe(userID) && UsersUtil.hasWebcamStream(userID)) {
        trace("VideoEventMapDelegate:: [" + me + "] openWebcamWindowFor:: Not ME and user = [" + userID + "] is publishing.");
        
        if (hasWindow(userID)) {
          trace("VideoEventMapDelegate:: [" + me + "] openWebcamWindowFor:: user = [" + userID + "] has a window open. Close it.");
          closeWindow(userID);
        }
        trace("VideoEventMapDelegate:: [" + me + "] openWebcamWindowFor:: View user's = [" + userID + "] webcam.");
        initPlayConnectionFor(userID);
      } else {
        if (UsersUtil.isMe(userID) && options.autoStart) {
          trace("VideoEventMapDelegate:: [" + me + "] openWebcamWindowFor:: It's ME and AutoStart. Start publishing.");
          autoStart();
        } else {
          if (options.displayAvatar) {
            trace("VideoEventMapDelegate:: [" + me + "] openWebcamWindowFor:: It's NOT ME and NOT AutoStart. Open Avatar for user = [" + userID + "]");
            openAvatarWindowFor(userID);              
          } else {
            trace("VideoEventMapDelegate:: [" + me + "] openWebcamWindowFor:: Is THERE another option for user = [" + userID + "]");
          }
        }
      }
    }
    
    private function openAvatarWindowFor(userID:String):void {
      if (! UsersUtil.hasUser(userID)) return;
      
      closeAllAvatarWindows(userID);

      _graphics.addAvatarFor(userID);
    }
    
    private function closeAllAvatarWindows(userID:String):void {
      _graphics.removeAvatarFor(userID);
    }
    
    private function openPublishWindowFor(userID:String, camIndex:int, videoProfile:VideoProfile):void {
      closeAllAvatarWindows(userID);

      _graphics.addCameraFor(userID, camIndex, videoProfile);
    }

    private function hasWindow(userID:String):Boolean {
      return _graphics.hasGraphicsFor(userID);
    }
    
    private function closeWindow(userID:String):void {
      _graphics.removeGraphicsFor(userID);
    }

    private function closePublishWindowWithStream(userID:String, stream:String):int {
      return _graphics.removeVideoByStreamName(userID, stream);
    }

	private function initPlayConnectionFor(userID:String):void {
		//TODO: Change to trace
		LogUtil.debug("VideoEventMapDelegate:: initPlayConnectionFor : [" + userID + "]");
		// Store the userID
		pendingVideoWindowsList[userID] = true;
		// Request the connection
		var bbbUser:BBBUser = UsersUtil.getUser(userID);
		proxy.createPlayConnectionFor(bbbUser.streamName);
	}

	public function handlePlayConnectionReady():void {
		// Iterate through all pending windows
		for(var userID:String in pendingVideoWindowsList) {
			var bbbUser:BBBUser = UsersUtil.getUser(userID);
			if(proxy.playConnectionIsReadyFor(bbbUser.streamName)) {
				delete pendingVideoWindowsList[userID];
				openViewWindowFor(userID);
			}
		}
	}
    
    private function openViewWindowFor(userID:String):void {
      trace("VideoEventMapDelegate:: [" + me + "] openViewWindowFor:: Opening VIEW window for [" + userID + "] [" + UsersUtil.getUserName(userID) + "]");
      
<<<<<<< HEAD
      var bbbUser:BBBUser = UsersUtil.getUser(userID);
      if (bbbUser.hasStream) {
        closeAllAvatarWindows(userID);
      }
      _graphics.addVideoFor(userID, proxy.connection);
=======
      var window:VideoWindow = new VideoWindow();
      window.userID = userID;
      window.videoOptions = options;       
      window.resolutions = options.resolutions.split(",");
      window.title = UsersUtil.getUserName(userID);
      
      closeWindow(userID);
            
      var bbbUser:BBBUser = UsersUtil.getUser(userID);      
		var streamName:String = proxy.getStreamNamePrefixFor(bbbUser.streamName) + bbbUser.streamName;
		window.startVideo(proxy.getPlayConnectionFor(bbbUser.streamName), streamName);
      
      webcamWindows.addWindow(window);        
      openWindow(window);
      dockWindow(window);  
    }
    
    private function openWindow(window:VideoWindowItf):void {
      var windowEvent:OpenWindowEvent = new OpenWindowEvent(OpenWindowEvent.OPEN_WINDOW_EVENT);
      windowEvent.window = window;
      _dispatcher.dispatchEvent(windowEvent);      
    }
    
    private function dockWindow(window:VideoWindowItf):void {
      // this event will dock the window, if it's enabled
      var openVideoEvent:OpenVideoWindowEvent = new OpenVideoWindowEvent();
      openVideoEvent.window = window;
      _dispatcher.dispatchEvent(openVideoEvent);         
    }

    public function connectToVideoApp():void {
      proxy = new VideoProxy(uri);
      proxy.connect();
    }
    
    public function startPublishing(e:StartBroadcastEvent):void{
      LogUtil.debug("VideoEventMapDelegate:: [" + me + "] startPublishing:: Publishing stream to: " + proxy.publishConnection.uri + "/" + e.stream);
      proxy.startPublishing(e);
      
      _isWaitingActivation = false;
      _isPublishing = true;
      UsersUtil.setIAmPublishing(true);
      
      var broadcastEvent:BroadcastStartedEvent = new BroadcastStartedEvent();
      streamList.addItem(e.stream);
      broadcastEvent.stream = e.stream;
      broadcastEvent.userid = UsersUtil.getMyUserID();
      broadcastEvent.isPresenter = UsersUtil.amIPresenter();
      broadcastEvent.camSettings = UsersUtil.amIPublishing();
      
      _dispatcher.dispatchEvent(broadcastEvent);
      if (proxy.videoOptions.showButton) {
        button.publishingStatus(button.START_PUBLISHING);
      }
    }
       
    public function stopPublishing(e:StopBroadcastEvent):void{
      trace("VideoEventMapDelegate:: [" + me + "] Stop publishing. ready = [" + _ready + "]"); 
      if(streamList.length <= 1) {
        setStopLastBroadcasting();
      } else {
        UsersUtil.setIAmPublishing(true);
      }
      streamList.removeItem(e.stream);
      stopBroadcasting(e.stream);
      button.setCamAsInactive(e.camId);
    }

    private function stopAllBroadcasting():void {
      trace("[VideoEventMapDelegate:stopAllBroadcasting]");
      setStopLastBroadcasting();
      streamList = new ArrayList();
      proxy.stopAllBroadcasting();

      var userID:String = UsersUtil.getMyUserID();
      _graphics.removeGraphicsFor(userID);

      var broadcastEvent:BroadcastStoppedEvent = new BroadcastStoppedEvent();
      broadcastEvent.stream = "";
      broadcastEvent.userid = UsersUtil.getMyUserID();
      broadcastEvent.avatarURL = UsersUtil.getAvatarURL();
      _dispatcher.dispatchEvent(broadcastEvent);

      if (proxy.videoOptions.showButton) {
        //Make toolbar button enabled again
        button.setAllCamAsInactive();
      }
      if (options.displayAvatar) {
        trace("VideoEventMapDelegate:: [" + me + "] Opening avatar");
        openAvatarWindowFor(UsersUtil.getMyUserID());              
      }
    }

    private function setStopLastBroadcasting():void {
      trace("[VideoEventMapDelegate:setStopLastBroadcasting]");
      _isPublishing = false;
      UsersUtil.setIAmPublishing(false);
    }

    private function stopBroadcasting(stream:String):void {
      trace("Stopping broadcast of stream [" + stream + "]");
      
      proxy.stopBroadcasting(stream);
      
      var broadcastEvent:BroadcastStoppedEvent = new BroadcastStoppedEvent();
      broadcastEvent.stream = stream;
      broadcastEvent.userid = UsersUtil.getMyUserID();
      broadcastEvent.avatarURL = UsersUtil.getAvatarURL();
      _dispatcher.dispatchEvent(broadcastEvent);
      
      var camId:int = closePublishWindowWithStream(UsersUtil.getMyUserID(), stream);

      if (proxy.videoOptions.showButton) {
        //Make toolbar button enabled again
        button.publishingStatus(button.STOP_PUBLISHING, camId);
      }
      
      if (streamList.length == 0 && options.displayAvatar) {
        trace("VideoEventMapDelegate:: [" + me + "] Opening avatar");
        openAvatarWindowFor(UsersUtil.getMyUserID());              
      }      
    }
    
    public function handleClosePublishWindowEvent(event:ClosePublishWindowEvent):void {
      trace("Closing publish window");
      if (_isPublishing) {
        stopAllBroadcasting();
      }
    }
    
    public function handleShareCameraRequestEvent(event:ShareCameraRequestEvent):void {     
      trace("[VideoEventMapDelegate:handleShareCameraRequestEvent]");
      if (options.skipCamSettingsCheck) {
        skipCameraSettingsCheck(int(event.defaultCamera));
      } else {
        openWebcamPreview(event.publishInClient, event.defaultCamera, event.camerasArray);
      }
    }

    public function handleStopAllShareCameraRequestEvent(event:StopShareCameraRequestEvent):void {
      trace("[VideoEventMapDelegate:handleStopAllShareCameraRequestEvent]");
      stopAllBroadcasting();
    }

    public function handleStopShareCameraRequestEvent(event:StopShareCameraRequestEvent):void {
      trace("[VideoEventMapDelegate:handleStopShareCameraRequestEvent]");
      var userID:String = UsersUtil.getMyUserID();
      var camIndex:int = event.camId;

      _graphics.removeVideoByCamIndex(userID, camIndex);
    }
    
    public function handleCamSettingsClosedEvent(event:BBBEvent):void{
      _isPreviewWebcamOpen = false;
    }
    
    private function openWebcamPreview(publishInClient:Boolean, defaultCamera:String, camerasArray:Object):void {
      var openEvent:BBBEvent = new BBBEvent(BBBEvent.OPEN_WEBCAM_PREVIEW);
      openEvent.payload.publishInClient = publishInClient;
      openEvent.payload.defaultCamera = defaultCamera;
      openEvent.payload.camerasArray = camerasArray;
      
    _isPreviewWebcamOpen = true;
    
      _dispatcher.dispatchEvent(openEvent);
    }
    
    public function stopModule():void {
      trace("VideoEventMapDelegate:: stopping video module");
      closeAllWindows();
      proxy.disconnect();
    }
    
    public function closeAllWindows():void{
      trace("VideoEventMapDelegate:: closing all windows");
      if (_isPublishing) {
        stopAllBroadcasting();
      }
      
      _graphics.shutdown();
    }
    
    public function switchToPresenter(event:MadePresenterEvent):void{
      trace("VideoEventMapDelegate:: [" + me + "] Got Switch to presenter event. ready = [" + _ready + "]");
           
      if (options.showButton) {
        displayToolbarButton();
      }  
    }
        
    public function switchToViewer(event:MadePresenterEvent):void{
      trace("VideoEventMapDelegate:: [" + me + "] Got Switch to viewer event. ready = [" + _ready + "]");
                  
      if (options.showButton){
        LogUtil.debug("****************** Switching to viewer. Show video button?=[" + UsersUtil.amIPresenter() + "]");
        displayToolbarButton();
        if (_isPublishing && options.presenterShareOnly) {
          stopAllBroadcasting();
        }
      }
    }
    
    public function connectedToVideoApp():void{
      trace("VideoEventMapDelegate:: [" + me + "] Connected to video application.");
      _ready = true;
      addToolbarButton();
      openWebcamWindows();        
    }
    
    public function handleCameraSetting(event:BBBEvent):void {      
      var cameraIndex:int = event.payload.cameraIndex;
      var videoProfile:VideoProfile = event.payload.videoProfile;
      trace("VideoEventMapDelegate::handleCameraSettings [" + cameraIndex + "," + videoProfile.id + "]");
      initCameraWithSettings(cameraIndex, videoProfile);
    }
    
    private function initCameraWithSettings(camIndex:int, videoProfile:VideoProfile):void {
      var camSettings:CameraSettingsVO = new CameraSettingsVO();
      camSettings.camIndex = camIndex;
      camSettings.videoProfile = videoProfile;
      
      UsersUtil.setCameraSettings(camSettings);
      
      _isWaitingActivation = true;
      button.setCamAsActive(camIndex);
      openPublishWindowFor(UsersUtil.getMyUserID(), camIndex, videoProfile);
    }

    private function closeViewWindowWithStream(userID:String, stream:String):void {
      _graphics.removeVideoByStreamName(userID, stream);
    }
    
    public function handleStoppedViewingWebcamEvent(event:StoppedViewingWebcamEvent):void {
      trace("VideoEventMapDelegate::handleStoppedViewingWebcamEvent [" + me + "] received StoppedViewingWebcamEvent for user [" + event.webcamUserID + "]");
      
      closeViewWindowWithStream(event.webcamUserID, event.streamName);
            
      if (options.displayAvatar && UsersUtil.hasUser(event.webcamUserID) && ! UsersUtil.isUserLeaving(event.webcamUserID)) {
        trace("VideoEventMapDelegate::handleStoppedViewingWebcamEvent [" + me + "] Opening avatar for user [" + event.webcamUserID + "]");
        openAvatarWindowFor(event.webcamUserID);              
      }        
    }
  }
}
