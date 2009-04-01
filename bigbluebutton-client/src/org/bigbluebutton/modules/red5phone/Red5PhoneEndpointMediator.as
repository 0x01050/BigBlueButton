/**
* BigBlueButton open source conferencing system - http://www.bigbluebutton.org/
*
* Copyright (c) 2008 by respective authors (see below).
*
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License as published by the Free Software
* Foundation; either version 2.1 of the License, or (at your option) any later
* version.
*
* This program is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
* PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License along
* with this program; if not, write to the Free Software Foundation, Inc.,
* 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
* 
*/
package org.bigbluebutton.modules.red5phone
{
	import org.bigbluebutton.common.messaging.Endpoint;
	import org.bigbluebutton.common.messaging.EndpointMessageConstants;
	import org.bigbluebutton.common.messaging.Router;
	import org.bigbluebutton.modules.red5phone.view.components.Red5PhoneWindow;
	import org.puremvc.as3.multicore.interfaces.IMediator;
	import org.puremvc.as3.multicore.interfaces.INotification;
	import org.puremvc.as3.multicore.patterns.mediator.Mediator;
	import org.puremvc.as3.multicore.utilities.pipes.interfaces.IPipeMessage;
	
	public class Red5PhoneEndpointMediator extends Mediator implements IMediator
	{
		public static const NAME:String = "Red5PhoneEndpointMediator";
		
		private var _phoneWindow:Red5PhoneWindow;
		private var _phoneWindowOpen:Boolean = false;
		private var _module:Red5PhoneModule;
		private var _router:Router;
		private var _endpoint:Endpoint;		
		private static const TO_PHONE_MODULE:String = "TO_PHONE_MODULE";
		private static const FROM_PHONE_MODULE:String = "FROM_PHONE_MODULE";
					
		public function Red5PhoneEndpointMediator(module:Red5PhoneModule)
		{
			super(NAME,module);
			_module = module;
			_router = module.router;
			_phoneWindow = new Red5PhoneWindow();
//			_phoneWindow.module = _module;
			LogUtil.debug("Creating endpoint for PhoneModule");
			_endpoint = new Endpoint(_router, FROM_PHONE_MODULE, TO_PHONE_MODULE, messageReceiver);	
		}
		
		override public function getMediatorName():String
		{
			return NAME;
		}
				
		override public function listNotificationInterests():Array
		{
			return [
				Red5PhoneModuleConstants.CONNECTED,
				Red5PhoneModuleConstants.DISCONNECTED,
				Red5PhoneModuleConstants.ADD_WINDOW,
				Red5PhoneModuleConstants.REMOVE_WINDOW
			];
		}
		
		override public function handleNotification(notification:INotification):void
		{
			LogUtil.debug("PhoneEndPoint MSG. " + notification.getName());	
			switch(notification.getName()){
				case Red5PhoneModuleConstants.CONNECTED:
					LogUtil.debug("Sending Phone MODULE_STARTED message to main");
					_endpoint.sendMessage(EndpointMessageConstants.MODULE_STARTED, 
							EndpointMessageConstants.TO_MAIN_APP, _module.moduleId);
					openPhoneWindow();
					break;
				case Red5PhoneModuleConstants.DISCONNECTED:
					LogUtil.debug('Sending Phone MODULE_STOPPED message to main');
					closePhoneWindow();
					var info:Object = new Object();
					info["moduleId"] = _module.moduleId
					_endpoint.sendMessage(EndpointMessageConstants.MODULE_STOPPED, 
							EndpointMessageConstants.TO_MAIN_APP, info);
					break;
				case Red5PhoneModuleConstants.ADD_WINDOW:
					LogUtil.debug('Sending Phone ADD_WINDOW message to main');
					_endpoint.sendMessage(EndpointMessageConstants.ADD_WINDOW, 
							EndpointMessageConstants.TO_MAIN_APP, notification.getBody());
					break;
				case Red5PhoneModuleConstants.REMOVE_WINDOW:
					LogUtil.debug('Sending Phone REMOVE_WINDOW message to main');
					_endpoint.sendMessage(EndpointMessageConstants.REMOVE_WINDOW, 
							EndpointMessageConstants.TO_MAIN_APP, notification.getBody());
					break;
			}
		}
	
		private function messageReceiver(message : IPipeMessage) : void
		{
			var msg : String = message.getHeader().MSG as String;
			switch(msg){
				case EndpointMessageConstants.CLOSE_WINDOW:
					facade.sendNotification(Red5PhoneModuleConstants.CLOSE_WINDOW);
					break;
				case EndpointMessageConstants.OPEN_WINDOW:
					LogUtil.debug('Received OPEN_WINDOW message from ' + message.getHeader().SRC);
					break;
			}
		}
		
		private function closePhoneWindow():void {
			if (_phoneWindowOpen) {
				facade.sendNotification(Red5PhoneModuleConstants.REMOVE_WINDOW, _phoneWindow);
				_phoneWindowOpen = false;
			}
		}
		
		private function openPhoneWindow():void{
		   	_phoneWindow.width = 280;
		   	_phoneWindow.height = 250;
		   	_phoneWindow.title = "Phone Module";
		   	_phoneWindow.showCloseButton = false;
		   	_phoneWindow.xPosition = 675;
		   	_phoneWindow.yPosition = 230;
		   	facade.sendNotification(Red5PhoneModuleConstants.ADD_WINDOW, _phoneWindow); 
		   	_phoneWindowOpen = true;
		}				
	}
}