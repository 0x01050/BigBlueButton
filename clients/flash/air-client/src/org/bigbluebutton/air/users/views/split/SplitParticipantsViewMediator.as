package org.bigbluebutton.air.users.views.split {
	
	import flash.events.Event;
	
	import mx.core.FlexGlobals;
	import mx.events.ResizeEvent;
	
	import org.bigbluebutton.air.common.PageEnum;
	import org.bigbluebutton.air.common.views.SplitViewEvent;
	import org.bigbluebutton.air.main.models.IUserUISession;
	
	import robotlegs.bender.bundles.mvcs.Mediator;
	
	public class SplitParticipantsViewMediator extends Mediator {
		
		[Inject]
		public var view:ISplitParticipantsView;
		
		[Inject]
		public var userUISession:IUserUISession;
		
		private var currentParticipant:Object = null;
		
		override public function initialize():void {
			eventDispatcher.addEventListener(SplitViewEvent.CHANGE_VIEW, changeView);
			view.participantsList.pushView(PageEnum.getClassfromName(PageEnum.PARTICIPANTS));
			FlexGlobals.topLevelApplication.stage.addEventListener(ResizeEvent.RESIZE, stageOrientationChangingHandler);
		}
		
		private function stageOrientationChangingHandler(e:Event):void {
			var tabletLandscape:Boolean = FlexGlobals.topLevelApplication.isTabletLandscape();
			if (currentParticipant) {
				if (tabletLandscape) {
					userUISession.pushPage(PageEnum.SPLITPARTICIPANTS, currentParticipant);
				} else {
					userUISession.popPage();
					userUISession.pushPage(PageEnum.PARTICIPANTS);
					userUISession.pushPage(PageEnum.USER_DETAILS, currentParticipant);
				}
			}
		}
		
		private function changeView(event:SplitViewEvent):void {
			view.participantDetails.pushView(event.view);
			currentParticipant = event.details
			userUISession.pushPage(PageEnum.SPLITPARTICIPANTS, event.details);
		}
		
		override public function destroy():void {
			super.destroy();
			eventDispatcher.removeEventListener(SplitViewEvent.CHANGE_VIEW, changeView);
			FlexGlobals.topLevelApplication.stage.removeEventListener(ResizeEvent.RESIZE, stageOrientationChangingHandler);
			view.dispose();
			view = null;
		}
	}
}
