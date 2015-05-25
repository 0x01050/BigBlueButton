package org.bigbluebutton.core.apps.users.red5

import org.bigbluebutton.conference.meeting.messaging.red5.ConnectionInvokerService
import org.bigbluebutton.conference.meeting.messaging.red5.SharedObjectClientMessage
import java.util.ArrayList
import java.util.List
import java.util.Map
import java.util.HashMap
import org.bigbluebutton.core.api._
import org.bigbluebutton.conference.meeting.messaging.red5.DirectClientMessage
import org.bigbluebutton.conference.meeting.messaging.red5.BroadcastClientMessage
import com.google.gson.Gson
import scala.collection.JavaConversions._
import org.bigbluebutton.conference.meeting.messaging.red5.DisconnectAllClientsMessage
import org.bigbluebutton.conference.meeting.messaging.red5.DisconnectClientMessage

class UsersClientMessageSender(service: ConnectionInvokerService) extends OutMessageListener2 {
	
	def handleMessage(msg: IOutMessage) {
	  msg match {             
	    case msg: GetUsersReply                          => handleGetUsersReply(msg)
	    case msg: UserVoiceTalking                       => handleUserVoiceTalking(msg)
	    case msg: UserLeftVoice                          => handleUserLeftVoice(msg)
	    case msg: RecordingStatusChanged                 => handleRecordingStatusChanged(msg)
	    case msg: GetRecordingStatusReply                => handleGetRecordingStatusReply(msg)
	    case msg: UserListeningOnly                      => handleUserListeningOnly(msg)
	    case msg: NewPermissionsSetting                  => handleNewPermissionsSetting(msg)
      case msg: UserLocked                             => handleUserLocked(msg)
	    case msg: MeetingMuted                           => handleMeetingMuted(msg)
	    case msg: MeetingState                           => handleMeetingState(msg)
	    
	    case _ => // println("Unhandled message in UsersClientMessageSender")
	  }
	}
	
	private def buildPermissionsHashMap(perms: Permissions):java.util.HashMap[String, java.lang.Boolean] = {
	  val args = new java.util.HashMap[String, java.lang.Boolean]();  
	  args.put("disableCam", perms.disableCam:java.lang.Boolean);
	  args.put("disableMic", perms.disableMic:java.lang.Boolean);
	  args.put("disablePrivChat", perms.disablePrivChat:java.lang.Boolean);
	  args.put("disablePubChat", perms.disablePubChat:java.lang.Boolean);
    args.put("lockedLayout", perms.lockedLayout:java.lang.Boolean);
    args.put("lockOnJoin", perms.lockOnJoin:java.lang.Boolean);
    args.put("lockOnJoinConfigurable", perms.lockOnJoinConfigurable:java.lang.Boolean);
    args
	}
	
	private def buildUserHashMap(user: UserVO):java.util.HashMap[String, Object] = {
	  val vu = user.voiceUser
	  val vuser = new HashMap[String, Object]()
	  vuser.put("userId", vu.userId)
	  vuser.put("webUserId", vu.webUserId)
	  vuser.put("callerName", vu.callerName)
	  vuser.put("callerNum", vu.callerNum)
	  vuser.put("joined", vu.joined:java.lang.Boolean)
	  vuser.put("locked", vu.locked:java.lang.Boolean)
	  vuser.put("muted", vu.muted:java.lang.Boolean)
	  vuser.put("talking", vu.talking:java.lang.Boolean)
	
	  val wuser = new HashMap[String, Object]()
	  wuser.put("userId", user.userID)
	  wuser.put("externUserID", user.externUserID)
	  wuser.put("name", user.name)
	  wuser.put("role", user.role.toString())
	  wuser.put("raiseHand", user.raiseHand:java.lang.Boolean)
	  wuser.put("presenter", user.presenter:java.lang.Boolean)
	  wuser.put("hasStream", user.hasStream:java.lang.Boolean)
	  wuser.put("locked", user.locked:java.lang.Boolean)
	  wuser.put("webcamStream", user.webcamStreams mkString("|"))
	  wuser.put("phoneUser", user.phoneUser:java.lang.Boolean)
	  wuser.put("voiceUser", vuser)	  
	  wuser.put("listenOnly", user.listenOnly:java.lang.Boolean)
   
	  wuser
	}
	
	private def handleNewPermissionsSetting(msg: NewPermissionsSetting) {
	  val args = new java.util.HashMap[String, Object]();  
	  args.put("disableCam", msg.permissions.disableCam:java.lang.Boolean);
	  args.put("disableMic", msg.permissions.disableMic:java.lang.Boolean);
	  args.put("disablePrivChat", msg.permissions.disablePrivChat:java.lang.Boolean);
	  args.put("disablePubChat", msg.permissions.disablePubChat:java.lang.Boolean);
    args.put("lockedLayout", msg.permissions.lockedLayout:java.lang.Boolean);
    args.put("lockOnJoin", msg.permissions.lockOnJoin:java.lang.Boolean);
    args.put("lockOnJoinConfigurable", msg.permissions.lockOnJoinConfigurable:java.lang.Boolean);
    
	  var users = new ArrayList[java.util.HashMap[String, Object]];
      msg.applyTo.foreach(uvo => {		
        users.add(buildUserHashMap(uvo))
      })
		
      args.put("users", users);
      
	  val message = new java.util.HashMap[String, Object]() 
	  val gson = new Gson();
  	  message.put("msg", gson.toJson(args))
  	  
  	  println("UsersClientMessageSender - handleNewPermissionsSetting \n" + message.get("msg") + "\n")
      val m = new BroadcastClientMessage(msg.meetingID, "permissionsSettingsChanged", message);
	  service.sendMessage(m);	    
	}
	
  private def handleUserLocked(msg: UserLocked) {
     val args = new java.util.HashMap[String, Object]();
     args.put("meetingID", msg.meetingID);
     args.put("user", msg.userId)
     args.put("lock", msg.lock:java.lang.Boolean)
     
     val message = new java.util.HashMap[String, Object]()
     val gson = new Gson();
     message.put("msg", gson.toJson(args))
     
     val m = new BroadcastClientMessage(msg.meetingID, "userLocked", message);
     service.sendMessage(m);   
  }
  	
	private def handleGetRecordingStatusReply(msg: GetRecordingStatusReply) {
	  val args = new java.util.HashMap[String, Object]();  
	  args.put("userId", msg.userId);
	  args.put("recording", msg.recording:java.lang.Boolean);	    
	  
	  val message = new java.util.HashMap[String, Object]() 
	  val gson = new Gson();
  	  message.put("msg", gson.toJson(args))
  	  
  	  println("UsersClientMessageSender - handleGetRecordingStatusReply \n" + message.get("msg") + "\n")
      val m = new DirectClientMessage(msg.meetingID, msg.userId, "getRecordingStatusReply", message);
	  service.sendMessage(m);	  
	}
	
	private def handleRecordingStatusChanged(msg: RecordingStatusChanged) {
	  val args = new java.util.HashMap[String, Object]();  
	  args.put("userId", msg.userId);
	  args.put("recording", msg.recording:java.lang.Boolean);	    
	  
	  val message = new java.util.HashMap[String, Object]() 
	  val gson = new Gson();
  	message.put("msg", gson.toJson(args))
  	  
  	println("UsersClientMessageSender - handleRecordingStatusChanged \n" + message.get("msg") + "\n")
    val m = new BroadcastClientMessage(msg.meetingID, "recordingStatusChanged", message);
	  service.sendMessage(m);	
	}
	
	
	private def handleUserVoiceTalking(msg: UserVoiceTalking) {
	  val args = new java.util.HashMap[String, Object]();
	  args.put("meetingID", msg.meetingID);	  
	  args.put("userId", msg.user.userID);
	  args.put("voiceUserId", msg.user.voiceUser.userId);
	  args.put("talking", msg.user.voiceUser.talking:java.lang.Boolean);
	  
	  val message = new java.util.HashMap[String, Object]() 
	  val gson = new Gson();
  	  message.put("msg", gson.toJson(args))
  	
 	  println("UsersClientMessageSender - handleUserVoiceTalking \n" + message.get("msg") + "\n")
//  	log.debug("UsersClientMessageSender - handlePresentationConversionProgress \n" + message.get("msg") + "\n")
      val m = new BroadcastClientMessage(msg.meetingID, "voiceUserTalking", message);
	  service.sendMessage(m);	  
	}
	
	private def handleUserLeftVoice(msg: UserLeftVoice) {
	  val args = new java.util.HashMap[String, Object]();
	  args.put("meetingID", msg.meetingID);
	  args.put("user", buildUserHashMap(msg.user))
	
	  val message = new java.util.HashMap[String, Object]() 
	  val gson = new Gson();
  	message.put("msg", gson.toJson(args))
  	
  	  println("UsersClientMessageSender - handleUserLeftVoice \n" + message.get("msg") + "\n")
//  	log.debug("UsersClientMessageSender - handleUserLeftVoice \n" + message.get("msg") + "\n")
      val m = new BroadcastClientMessage(msg.meetingID, "userLeftVoice", message);
	  service.sendMessage(m);	  
	}
	
	
	private def handleGetUsersReply(msg: GetUsersReply):Unit = {
      var args = new HashMap[String, Object]();			
      args.put("count", msg.users.length:java.lang.Integer)
		
      var users = new ArrayList[java.util.HashMap[String, Object]];
      msg.users.foreach(uvo => {		
        users.add(buildUserHashMap(uvo))
      })
		
      args.put("users", users);
		
      val message = new java.util.HashMap[String, Object]() 
      val gson = new Gson()
  	  message.put("msg", gson.toJson(args))
		
      println("UsersClientMessageSender - handleGetUsersReply \n" + message.get("msg") + "\n")
			
      var m = new DirectClientMessage(msg.meetingID, msg.requesterID, "getUsersReply", message)
  	  service.sendMessage(m)
	}


	private def handleMeetingState(msg: MeetingState) {
	  var args = new HashMap[String, Object]();	
	  args.put("permissions", buildPermissionsHashMap(msg.permissions));
		args.put("meetingMuted", msg.meetingMuted:java.lang.Boolean);
		
	  val message = new java.util.HashMap[String, Object]() 
	  val gson = new Gson();
  	message.put("msg", gson.toJson(args))

  	var jmr = new DirectClientMessage(msg.meetingID, msg.userId, "meetingState", message);
  	service.sendMessage(jmr);	  
	}
	
	private def handleMeetingMuted(msg: MeetingMuted) {
	  var args = new HashMap[String, Object]();	
	  args.put("meetingMuted", msg.meetingMuted:java.lang.Boolean);
		
	  var message = new HashMap[String, Object]();
	  val gson = new Gson();
  	message.put("msg", gson.toJson(args))
  	    
	  var m = new BroadcastClientMessage(msg.meetingID, "meetingMuted", message);
	  service.sendMessage(m);	  
	}
	                
	
	private def handleUserListeningOnly(msg: UserListeningOnly) {
	  var args = new HashMap[String, Object]();	
	  args.put("userId", msg.userID);
	  args.put("listenOnly", msg.listenOnly:java.lang.Boolean);
	
	  val message = new java.util.HashMap[String, Object]() 
	  val gson = new Gson();
 	  message.put("msg", gson.toJson(args))
  	    
    println("UsersClientMessageSender - handleUserListeningOnly \n" + message.get("msg") + "\n")
  	    
 	  var m = new BroadcastClientMessage(msg.meetingID, "user_listening_only", message);
 	  service.sendMessage(m);	  
	}
}