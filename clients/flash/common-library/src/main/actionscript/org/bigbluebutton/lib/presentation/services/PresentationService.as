package org.bigbluebutton.lib.presentation.services {
	
	import org.bigbluebutton.lib.main.models.IConferenceParameters;
	import org.bigbluebutton.lib.main.models.IUserSession;
	
	public class PresentationService implements IPresentationService {
		
		[Inject]
		public var conferenceParameters:IConferenceParameters;
		
		[Inject]
		public var userSession:IUserSession;
		
		public var presentMessageSender:PresentMessageSender;
		
		public var presentMessageReceiver:PresentMessageReceiver;
		
		public function PresentationService() {
			presentMessageSender = new PresentMessageSender;
			presentMessageReceiver = new PresentMessageReceiver;
		}
		
		public function setupMessageSenderReceiver():void {
			presentMessageSender.userSession = userSession;
			presentMessageSender.conferenceParameters = conferenceParameters;
			presentMessageReceiver.userSession = userSession;
			userSession.mainConnection.addMessageListener(presentMessageReceiver);
		}
		
		public function getPresentationInfo():void {
			presentMessageSender.getPresentationInfo();
		}
		
		public function setCurrentPage(presentationId: String, pageId: String):void {
			presentMessageSender.setCurrentPage(presentationId, pageId);
		}
		
		public function move(presentationId:String, pageId:String, xOffset:Number, yOffset:Number, widthRatio:Number, heightRatio:Number):void {
			presentMessageSender.move(presentationId, pageId, xOffset, yOffset, widthRatio, heightRatio);
		}
		
		public function removePresentation(presentationId:String):void {
			presentMessageSender.removePresentation(presentationId);
		}
		
		public function setCurrentPresentation(presentationId:String):void {
			presentMessageSender.setCurrentPresentation(presentationId);
		}
	}
}
