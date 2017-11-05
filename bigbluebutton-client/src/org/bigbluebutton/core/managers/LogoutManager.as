/**
 * BigBlueButton open source conferencing system - http://www.bigbluebutton.org/
 *
 * Copyright (c) 2017 BigBlueButton Inc. and by respective authors (see below).
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
package org.bigbluebutton.core.managers {
	import com.asfusion.mate.events.Dispatcher;

	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.events.IOErrorEvent;
	import flash.external.ExternalInterface;
	import flash.net.URLLoader;
	import flash.net.URLRequest;
	import flash.net.navigateToURL;

	import mx.core.FlexGlobals;

	import org.as3commons.logging.api.ILogger;
	import org.as3commons.logging.api.getClassLogger;
	import org.bigbluebutton.core.BBB;
	import org.bigbluebutton.core.Options;
	import org.bigbluebutton.core.PopUpUtil;
	import org.bigbluebutton.core.UsersUtil;
	import org.bigbluebutton.main.events.ExitApplicationEvent;
	import org.bigbluebutton.main.events.InvalidAuthTokenEvent;
	import org.bigbluebutton.main.events.MeetingNotFoundEvent;
	import org.bigbluebutton.main.model.options.LayoutOptions;
	import org.bigbluebutton.main.model.users.events.ConnectionFailedEvent;
	import org.bigbluebutton.main.views.LoggedOutWindow;
	import org.bigbluebutton.util.i18n.ResourceUtil;

	public class LogoutManager {

		private static const LOGGER:ILogger = getClassLogger(LogoutManager);

		private var dispatcher:Dispatcher = new Dispatcher();

		public function handleMeetingNotFoundEvent(e:MeetingNotFoundEvent):void {
			showlogoutWindow(ResourceUtil.getInstance().getString('bbb.mainshell.meetingNotFound'));
		}

		private function showlogoutWindow(reason:String):void {
			var layoutOptions:LayoutOptions = Options.getOptions(LayoutOptions) as LayoutOptions;

			if (layoutOptions != null && layoutOptions.showLogoutWindow) {
				if (UsersUtil.iAskedToLogout()) {
					handleExitApplicationEvent();
					return;
				}
				var loggedOutWindow:LoggedOutWindow = PopUpUtil.createModalPopUp(FlexGlobals.topLevelApplication as DisplayObject, LoggedOutWindow, true) as LoggedOutWindow;

				if (loggedOutWindow) {
					loggedOutWindow.setReason(reason);
					dispatcher.dispatchEvent(new ExitApplicationEvent(ExitApplicationEvent.CLOSE_APPLICATION));
				}
			} else {
				dispatcher.dispatchEvent(new ExitApplicationEvent(ExitApplicationEvent.CLOSE_APPLICATION));

				LOGGER.debug("SingOut to [{0}/bigbluebutton/api/signOut]", [BBB.getBaseURL()]);
				var request:URLRequest = new URLRequest(BBB.getBaseURL() + "/bigbluebutton/api/signOut");
				var urlLoader:URLLoader = new URLLoader();
				urlLoader.addEventListener(Event.COMPLETE, handleLogoutComplete);
				urlLoader.addEventListener(IOErrorEvent.IO_ERROR, handleLogoutError);
				urlLoader.load(request);
			}
		}

		private function handleLogoutError(e:Event):void {
			LOGGER.debug("Call to signOut URL failed.");
			redirectToLogoutUrl();
		}

		private function handleLogoutComplete(e:Event):void {
			LOGGER.debug("Call to signOut URL succeeded.");
			redirectToLogoutUrl();
		}

		public function handleExitApplicationEvent(e:ExitApplicationEvent = null):void {
			if (!UsersUtil.isBreakout()) {
				navigateToURL(new URLRequest(BBB.getLogoutURL()), "_self");
			} else {
				ExternalInterface.call("window.close");
			}
		}

		private function redirectToLogoutUrl():void {
			var logoutURL:String = BBB.getLogoutURL();
			var request:URLRequest = new URLRequest(logoutURL);
			LOGGER.debug("Logging out to: {0}", [logoutURL]);
			navigateToURL(request, '_self');
		}

		private function handleLogout(e:ConnectionFailedEvent):void {
			if (e is ConnectionFailedEvent) {
				showlogoutWindow((e as ConnectionFailedEvent).type);
			} else
				showlogoutWindow("You have logged out of the conference");
		}

		public function connectionFailedHandler(e:ConnectionFailedEvent):void {
			handleLogout(e);
		}

		public function handleInvalidAuthToken(event:InvalidAuthTokenEvent):void {
			showlogoutWindow(ResourceUtil.getInstance().getString('bbb.mainshell.invalidAuthToken'));
		}
	}
}
