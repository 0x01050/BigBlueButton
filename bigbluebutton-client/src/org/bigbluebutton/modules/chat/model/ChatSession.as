package org.bigbluebutton.modules.chat.model
{
  import mx.collections.ArrayCollection;

  public class ChatSession
  {
    private var _chatType:String;
    private var _chatWithUserID:String;
    
    private var _messages:ArrayCollection = new ArrayCollection();
    
    public function ChatSession(type:String, withUserID:String)
    {
      _chatType = type;
      _chatWithUserID = withUserID;
    }
    
    /**
    * return PUBLIC or PRIVATE chat
    */
    public function getType():String {
      return _chatType;  
    }
    
    public function getUserID():String {
      return _chatWithUserID;
    }
    
    private function getLastSender(chatMessages:ArrayCollection):String {
      var msg:ChatMessage = chatMessages.getItemAt(chatMessages.length - 1) as ChatMessage;
      return msg.senderId;
    }
    
    public function newChatMessage(msg:Object):void {
/*      var msg:ChatMessage = new ChatMessage();
      if (_messages.length == 0) {
        msg.lastSenderId = msg.userID;
      }
      
      msg.senderId = msg.userID;
      
      msg.senderLanguage = msg.lang;
      msg.receiverLanguage = language.toString();
      msg.translate = translate;
      
      msg.translatedText = chatobj.message;
      msg.senderText = chatobj.message;
      
      msg.name = chatobj.username;
      msg.senderColor = uint(chatobj.color);
      msg.translatedColor = msg.senderColor;
      msg.senderTime = chatobj.time;			
      msg.time = timeString;
      msg.lastTime = lastTime;
      lastTime = timeString;
*/    }
  }
}