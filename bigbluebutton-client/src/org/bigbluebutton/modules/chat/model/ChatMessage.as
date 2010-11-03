package org.bigbluebutton.modules.chat.model {
	import be.boulevart.google.ajaxapi.translation.GoogleTranslation;
	import be.boulevart.google.ajaxapi.translation.data.GoogleTranslationResult;
	import be.boulevart.google.events.GoogleApiEvent;
	
	public class ChatMessage {
		[Bindable] public var lastSenderId:String;
		[Bindable] public var senderId:String;
		[Bindable] public var senderLanguage:String;
		[Bindable] public var receiverLanguage:String;
		[Bindable] public var translate:Boolean;
		[Bindable] public var senderColor:uint;
		[Bindable] public var translateLocale:String = "";	 
			 
		[Bindable] public var name:String;
		[Bindable] public var senderTime:String;
		[Bindable] public var time:String;
		[Bindable] public var lastTime:String;
		[Bindable] public var senderText:String;
		[Bindable] public var translatedText:String;
		[Bindable] public var translated:Boolean = false;
		[Bindable] public var translatedColor:uint;
		
		private var g:GoogleTranslation;	 
			 
		public function ChatMessage() {
			g = new GoogleTranslation();
			g.addEventListener(GoogleApiEvent.TRANSLATION_RESULT, onTranslationDone);
		}

		public function translateMessage():void {		
			if (!translate) return;
				
			if ((senderLanguage != receiverLanguage) && !translated) {
//				LogUtil.debug("Translating " + senderText + " from " + senderLanguage + " to " + receiverLanguage + ".");
				g.translate(senderText, senderLanguage, receiverLanguage);
			} else {
//				LogUtil.debug("NOT Translating " + senderText + " from " + senderLanguage + " to " + receiverLanguage + ".");
			}			
		}
			
		private function onTranslationDone(e:GoogleApiEvent):void{
			var result:GoogleTranslationResult = e.data as GoogleTranslationResult;

			if (result.result != senderText) {
				translated = true;
				translatedText = result.result ;
				translateLocale = "[" + senderLanguage + "->" + receiverLanguage + "]";
				translatedColor = 0xCF4C5C;
			} 
		}
	}
}