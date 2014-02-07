package org.bigbluebutton.core.apps.users.red5

import org.bigbluebutton.conference.meeting.messaging.red5.ConnectionInvokerService
import org.bigbluebutton.conference.meeting.messaging.red5.SharedObjectClientMessage
import java.util.ArrayList
import java.util.Map
import java.util.HashMap
import org.bigbluebutton.core.api._
import org.bigbluebutton.conference.meeting.messaging.red5.DirectClientMessage
import org.bigbluebutton.conference.meeting.messaging.red5.BroadcastClientMessage
import com.google.gson.Gson
import scala.collection.JavaConversions._

class UsersClientMessageSender(service: ConnectionInvokerService) extends OutMessageListener2 {
	private val USERS_SO: String = "participantsSO"; 

	def handleMessage(msg: IOutMessage) {
	  msg match {
	    case endMsg: EndAndKickAll => handleEndAndKickAll(endMsg)
	    case assignPres: PresenterAssigned => handleAssignPresenter(assignPres)
	    case userJoin: UserJoined => handleUserJoined(userJoin)
	    case userLeft: UserLeft => handleUserLeft(userLeft)
	    case statusChange: UserStatusChange => handleUserStatusChange(statusChange)
	    case getUsersReply: GetUsersReply => handleGetUsersReply(getUsersReply)
	    case _ => // println("Unhandled message in UsersClientMessageSender")
	  }
	}
	
	private def handleGetUsersReply(msg: GetUsersReply):Unit = {
		var message = new HashMap[String, Object]();
			
		message.put("count", msg.users.length:java.lang.Integer)
		
		var users = new HashMap[String, Object]();
		
		println("*************** Users lenght=[" + msg.users.length + "]")
		
		msg.users.foreach(uvo => {
  		  var pm = new HashMap[String, Object]();

		  pm.put("userID", uvo.userID)
		  pm.put("externUserID", uvo.externUserID)
		  pm.put("name", uvo.name)
		  pm.put("role", uvo.role.toString())
		  pm.put("hasStream", uvo.hasStream:java.lang.Boolean)
		  pm.put("presenter", uvo.presenter:java.lang.Boolean)
		  pm.put("raiseHand", uvo.raiseHand:java.lang.Boolean)
		  
		  users.put(uvo.userID, pm)
		})
		
		message.put("users", users);
		
		val gson = new Gson()
		val msgString = gson.toJson(message)
		
		println("JSON = \n" + msgString)
			
		var m = new DirectClientMessage(msg.meetingID, msg.requesterID, "getUsersReply", message);
		service.sendMessage(m);	  
	}
		
	private def handleEndAndKickAll(msg: EndAndKickAll):Unit = {
	  var message = new HashMap[String, Object]();
	  var m = new BroadcastClientMessage(msg.meetingID, "logout", message);
	  service.sendMessage(m);
	}


	private def handleAssignPresenter(msg:PresenterAssigned):Unit = {
	  	var message = new HashMap[String, Object]();
		message.put("newPresenterID", msg.presenter.presenterID);
		message.put("newPresenterName", msg.presenter.presenterName);
		message.put("assignedBy", msg.presenter.assignedBy);

		val gson = new Gson()
		val msgString = gson.toJson(message)
		
		println("JSON = \n" + msgString)
		
		var m = new BroadcastClientMessage(msg.meetingID, "assignPresenterCallback", message);
		service.sendMessage(m);
			
	}
	

	private def handleUserJoined(msg: UserJoined):Unit = {
		var message = new HashMap[String, Object]();
		message.put("userID", msg.user.userID);
		message.put("externUserID", msg.user.externUserID);
		message.put("name", msg.user.name);
		message.put("role", msg.user.role.toString());
		message.put("raiseHand", msg.user.raiseHand:java.lang.Boolean)
		message.put("presenter", msg.user.presenter:java.lang.Boolean)
		message.put("hasStream", msg.user.hasStream:java.lang.Boolean)
		
		val gson = new Gson()
		val msgString = gson.toJson(message)
		
		println("JSON = \n" + msgString)
		
		var m = new BroadcastClientMessage(msg.meetingID, "participantJoined", message);
		service.sendMessage(m);		
	}


	private def handleUserLeft(msg: UserLeft):Unit = {
		var message = new HashMap[String, Object]();
		message.put("userID", msg.user.userID);
		
		val gson = new Gson()
		val msgString = gson.toJson(message)
		
		println("JSON = \n" + msgString)
		
		var m = new BroadcastClientMessage(msg.meetingID, "participantLeft", message);
		service.sendMessage(m);
	}

	private def handleUserStatusChange(msg: UserStatusChange):Unit = {
		var message = new HashMap[String, Object]();
		message.put("userID", msg.userID);
		message.put("status", msg.status);
		message.put("value", msg.value);
		
		val gson = new Gson()
		val msgString = gson.toJson(message)
		
		println("JSON = \n" + msgString)
		
		var m = new BroadcastClientMessage(msg.meetingID, "participantStatusChange", message);
		service.sendMessage(m);
	}
}