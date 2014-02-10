package org.bigbluebutton.core.apps.whiteboard.red5

import org.bigbluebutton.conference.meeting.messaging.red5.ConnectionInvokerService
import org.bigbluebutton.core.api._
import org.bigbluebutton.conference.meeting.messaging.red5.BroadcastClientMessage
import org.bigbluebutton.conference.meeting.messaging.red5.DirectClientMessage
import scala.collection.JavaConversions._
import com.google.gson.Gson

class WhiteboardClientMessageSender(service: ConnectionInvokerService) extends OutMessageListener2 {
  def handleMessage(msg: IOutMessage) {
    msg match {
      case msg: SendWhiteboardAnnotationHistoryReply => 
                   handleSendWhiteboardAnnotationHistoryReply(msg)
      case msg: SendWhiteboardAnnotationEvent => 
                   handleSendWhiteboardAnnotationEvent(msg)
      case msg: ChangeWhiteboardPageEvent => 
                   handleChangeWhiteboardPageEvent(msg)
      case msg: ClearWhiteboardEvent => 
                   handleClearWhiteboardEvent(msg)
      case msg: UndoWhiteboardEvent => 
                   handleUndoWhiteboardEvent(msg)
      case msg: WhiteboardActivePresentationEvent => 
                   handleWhiteboardActivePresentationEvent(msg)
      case msg: WhiteboardEnabledEvent => 
                   handleWhiteboardEnabledEvent(msg)
      case msg: IsWhiteboardEnabledReply => 
                   handleIsWhiteboardEnabledReply(msg)
      case _ => // do nothing
    }
  }

  private def handleWhiteboardActivePresentationEvent(msg: WhiteboardActivePresentationEvent) {
	val args = new java.util.HashMap[String, Object]()
	args.put("presentationID", msg.presentationID)
	args.put("numberOfPages", msg.numPages:java.lang.Integer)
	
	val message = new java.util.HashMap[String, Object]() 
	val gson = new Gson();
  	message.put("msg", gson.toJson(args))
  	
	println("WhiteboardClientMessageSender - handleWhiteboardActivePresentationEvent \n" + message.get("msg") + "\n")
	
	val m = new BroadcastClientMessage(msg.meetingID, "WhiteboardChangePresentationCommand", args)
	service.sendMessage(m)
  }
	
  private def handleWhiteboardEnabledEvent(msg: WhiteboardEnabledEvent) {
	val args = new java.util.HashMap[String, Object]()
	args.put("enabled", msg.enable:java.lang.Boolean)
	
	val message = new java.util.HashMap[String, Object]() 
	val gson = new Gson();
  	message.put("msg", gson.toJson(args))
  	
	println("WhiteboardClientMessageSender - handleWhiteboardEnabledEvent \n" + message.get("msg") + "\n")	
	
	val m = new BroadcastClientMessage(msg.meetingID, "WhiteboardEnableWhiteboardCommand", args)
	service.sendMessage(m)
  }
	
  private def handleIsWhiteboardEnabledReply(msg: IsWhiteboardEnabledReply) {
	val args = new java.util.HashMap[String, Object]()
	args.put("enabled", msg.enabled:java.lang.Boolean)
	
	val message = new java.util.HashMap[String, Object]() 
	val gson = new Gson();
  	message.put("msg", gson.toJson(args))
  	
	println("WhiteboardClientMessageSender - handleIsWhiteboardEnabledReply \n" + message.get("msg") + "\n")		
	
	val m = new DirectClientMessage(msg.meetingID, msg.requesterID, "WhiteboardIsWhiteboardEnabledReply", args)
	service.sendMessage(m)
  }
	
  private def handleSendWhiteboardAnnotationHistoryReply(msg: SendWhiteboardAnnotationHistoryReply) {
	val args = new java.util.HashMap[String, Object]()			
	args.put("count", msg.shapes.length:java.lang.Integer)
	args.put("presentationID", msg.presentationID)
	args.put("pageNumber", msg.numPages:java.lang.Integer)
	args.put("annotations", msg.shapes)
	
	val message = new java.util.HashMap[String, Object]() 
	val gson = new Gson();
  	message.put("msg", gson.toJson(args))
  	
	println("WhiteboardClientMessageSender - handleSendWhiteboardAnnotationHistoryReply \n" + message.get("msg") + "\n")	
	
	val m = new DirectClientMessage(msg.meetingID, msg.requesterID, "WhiteboardRequestAnnotationHistoryReply", args)
	service.sendMessage(m)
  }
	
  private def handleSendWhiteboardAnnotationEvent(msg: SendWhiteboardAnnotationEvent) {	  
	val m = new BroadcastClientMessage(msg.meetingID, "WhiteboardNewAnnotationCommand", mapAsJavaMap(msg.shape.shape))
	service.sendMessage(m)
  }
	
  private def handleChangeWhiteboardPageEvent(msg: ChangeWhiteboardPageEvent) {
	val args = new java.util.HashMap[String, Object]()		
	args.put("pageNum", msg.page:java.lang.Integer)
	args.put("numAnnotations", msg.numAnnotations:java.lang.Integer)
	
	val message = new java.util.HashMap[String, Object]() 
	val gson = new Gson();
  	message.put("msg", gson.toJson(args))
  	
	println("WhiteboardClientMessageSender - handleChangeWhiteboardPageEvent \n" + message.get("msg") + "\n")
	
	val m = new BroadcastClientMessage(msg.meetingID, "WhiteboardChangePageCommand", args)
	service.sendMessage(m)
  }
	
  private def handleClearWhiteboardEvent(msg: ClearWhiteboardEvent) {
    val args = new java.util.HashMap[String, Object]()		
	args.put("ignoreThis", "Filler")
	
	val message = new java.util.HashMap[String, Object]() 
	val gson = new Gson();
  	message.put("msg", gson.toJson(args))
  	
	println("WhiteboardClientMessageSender - handleClearWhiteboardEvent \n" + message.get("msg") + "\n")
		
	val m = new BroadcastClientMessage(msg.meetingID, "WhiteboardClearCommand", message);
	service.sendMessage(m);	
  }
	
  private def handleUndoWhiteboardEvent(msg: UndoWhiteboardEvent) {
    val args = new java.util.HashMap[String, Object]()		
	args.put("ignoreThis", "Filler")
	
	val message = new java.util.HashMap[String, Object]() 
	val gson = new Gson();
  	message.put("msg", gson.toJson(args))
  	
	println("WhiteboardClientMessageSender - handleUndoWhiteboardEvent \n" + message.get("msg") + "\n")
		
	val m = new BroadcastClientMessage(msg.meetingID, "WhiteboardUndoCommand", message);
	service.sendMessage(m);
  }
}