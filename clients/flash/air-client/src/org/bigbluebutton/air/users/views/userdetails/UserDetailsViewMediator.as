package org.bigbluebutton.air.users.views.userdetails {
	
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.core.FlexGlobals;
	import mx.events.ResizeEvent;
	
	import org.bigbluebutton.air.common.PageEnum;
	import org.bigbluebutton.air.common.TransitionAnimationEnum;
	import org.bigbluebutton.air.main.models.IUserUISession;
	import org.bigbluebutton.lib.main.commands.ChangeRoleSignal;
	import org.bigbluebutton.lib.main.commands.ClearUserStatusSignal;
	import org.bigbluebutton.lib.main.commands.LockUserSignal;
	import org.bigbluebutton.lib.main.commands.PresenterSignal;
	import org.bigbluebutton.lib.main.models.IConferenceParameters;
	import org.bigbluebutton.lib.main.models.IUserSession;
	import org.bigbluebutton.lib.user.models.User;
	
	import robotlegs.bender.bundles.mvcs.Mediator;
	
	public class UserDetailsViewMediator extends Mediator {
		
		[Inject]
		public var view:IUserDetailsView;
		
		[Inject]
		public var userSession:IUserSession;
		
		[Inject]
		public var userUISession:IUserUISession;
		
		[Inject]
		public var clearUserStatusSignal:ClearUserStatusSignal;
		
		[Inject]
		public var presenterSignal:PresenterSignal;
		
		[Inject]
		public var changeRoleSignal:ChangeRoleSignal;
		
		[Inject]
		public var lockUserSignal:LockUserSignal;
		
		[Inject]
		public var conferenceParameters:IConferenceParameters;
		
		protected var _user:User;
		
		override public function initialize():void {
			_user = userUISession.currentPageDetails as User;
			userSession.userList.userChangeSignal.add(userChanged);
			userSession.userList.userRemovedSignal.add(userRemoved);
			view.user = _user;
			view.conferenceParameters = conferenceParameters;
			view.userMe = userSession.userList.me;
			view.showCameraButton.addEventListener(MouseEvent.CLICK, onShowCameraButton);
			view.showPrivateChat.addEventListener(MouseEvent.CLICK, onShowPrivateChatButton);
			view.clearStatusButton.addEventListener(MouseEvent.CLICK, onClearStatusButton);
			view.makePresenterButton.addEventListener(MouseEvent.CLICK, onMakePresenterButton);
			view.promoteButton.addEventListener(MouseEvent.CLICK, onPromoteButton);
			view.updateLockButtons(isRoomLocked());
			view.lockButton.addEventListener(MouseEvent.CLICK, onLockUser);
			view.unlockButton.addEventListener(MouseEvent.CLICK, onUnlockUser);
			FlexGlobals.topLevelApplication.topActionBar.pageName.text = view.user.name;
		}
		
		protected function onLockUser(event:MouseEvent):void {
			//dispatch lock signal
			lockUserSignal.dispatch(_user.userID, true);
			userUISession.popPage();
		}
		
		protected function onUnlockUser(event:MouseEvent):void {
			//dispatch lock signal
			lockUserSignal.dispatch(_user.userID, false);
			userUISession.popPage();
		}
		
		private function isRoomLocked():Boolean {
			return userSession.lockSettings.disableCam || userSession.lockSettings.disableMic || userSession.lockSettings.disablePrivateChat || userSession.lockSettings.disablePublicChat || userSession.lockSettings.lockedLayout;
		}
		
		protected function onShowCameraButton(event:MouseEvent):void {
			userUISession.pushPage(PageEnum.VIDEO_CHAT, _user, TransitionAnimationEnum.APPEAR);
		}
		
		protected function onShowPrivateChatButton(event:MouseEvent):void {
			userUISession.pushPage(PageEnum.CHAT, _user, TransitionAnimationEnum.APPEAR);
		}
		
		protected function onClearStatusButton(event:MouseEvent):void {
			clearUserStatusSignal.dispatch(_user.userID);
			userSession.userList.getUser(_user.userID).status = User.NO_STATUS;
			view.clearStatusButton.includeInLayout = false;
			view.clearStatusButton.visible = false;
			userUISession.popPage();
		}
		
		protected function onPromoteButton(event:MouseEvent):void {
			var roleOptions:Object = new Object();
			roleOptions.userID = _user.userID;
			roleOptions.role = (_user.role == User.MODERATOR) ? User.VIEWER : User.MODERATOR;
			changeRoleSignal.dispatch(roleOptions);
			userUISession.popPage();
		}
		
		protected function onMakePresenterButton(event:MouseEvent):void {
			presenterSignal.dispatch(_user, userSession.userList.me.userID);
			userUISession.popPage();
		}
		
		private function userRemoved(userID:String):void {
			if (_user.userID == userID) {
				userUISession.popPage();
			}
		}
		
		private function userChanged(user:User, type:int):void {
			if (_user.userID == user.userID || user.me) {
				view.update();
				view.updateLockButtons(isRoomLocked());
			}
		}
		
		override public function destroy():void {
			super.destroy();
			view.showCameraButton.removeEventListener(MouseEvent.CLICK, onShowCameraButton);
			view.showPrivateChat.removeEventListener(MouseEvent.CLICK, onShowPrivateChatButton);
			userSession.userList.userChangeSignal.remove(userChanged);
			userSession.userList.userRemovedSignal.remove(userRemoved);
			view.dispose();
			view = null;
		}
	}
}
