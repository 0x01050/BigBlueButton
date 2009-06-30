/*
 * BigBlueButton - http://www.bigbluebutton.org
 * 
 * Copyright (c) 2008-2009 by respective authors (see below). All rights reserved.
 * 
 * BigBlueButton is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * Foundation; either version 3 of the License, or (at your option) any later 
 * version. 
 * 
 * BigBlueButton is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along 
 * with BigBlueButton; if not, If not, see <http://www.gnu.org/licenses/>.
 *
 * $Id: $
 */
package org.bigbluebutton.modules.video.view
{
	import flash.events.Event;
	
	import org.bigbluebutton.modules.video.VideoModuleConstants;
	import org.bigbluebutton.modules.video.view.components.MyCameraWindow;
	import org.bigbluebutton.modules.video.view.components.ToolbarButton;
	import org.puremvc.as3.multicore.interfaces.IMediator;
	import org.puremvc.as3.multicore.interfaces.INotification;
	import org.puremvc.as3.multicore.patterns.mediator.Mediator;

	public class ToolbarButtonMediator extends Mediator implements IMediator
	{
		public static const NAME:String = "ToolbarButtonMediator";
		
		private var button:ToolbarButton;
		private var myCamWindow:MyCameraWindow;
		
		public function ToolbarButtonMediator()
		{
			super(NAME);
			button = new ToolbarButton();			
			button.addEventListener(VideoModuleConstants.START_CAMERA_EVENT, onStartCameraEvent);			
		}
		
		private function onStartCameraEvent(e:Event):void {
			button.enabled = false;			
			myCamWindow = new MyCameraWindow();
			myCamWindow.width = 330;
		   	myCamWindow.height = 270;
		   	myCamWindow.title = "My Camera";
		   	myCamWindow.showCloseButton = true;
		   	myCamWindow.xPosition = 700;
		   	myCamWindow.yPosition = 240;
		   	myCamWindow.addEventListener(MyCameraWindow.WINDOW_CLOSE_EVENT, onMyCameraWindowClose);
		   	facade.registerMediator(new MyCameraWindowMediator(myCamWindow));
		   	facade.sendNotification(VideoModuleConstants.ADD_WINDOW, myCamWindow); 
		}
		
		private function onMyCameraWindowClose(e:Event):void {
			button.enabled = true;
			facade.removeMediator(MyCameraWindowMediator.NAME);
			facade.sendNotification(VideoModuleConstants.REMOVE_WINDOW, myCamWindow);
		}
		
		override public function listNotificationInterests():Array
		{
			return [
				VideoModuleConstants.SETUP_COMPLETE
			];
		}
		
		override public function handleNotification(notification:INotification):void
		{
			switch(notification.getName()){
				case VideoModuleConstants.SETUP_COMPLETE:
					LogUtil.debug(NAME + ":Got VideoModuleConstants.SETUP_COMPLETE");
					facade.sendNotification(VideoModuleConstants.ADD_BUTTON, button);
				break;
			}
		}
		
	}
}