package org.bigbluebutton.webconference.voice.asterisk.meetme;

import org.slf4j.Logger;
import org.asteriskjava.live.DefaultAsteriskServer;
import org.asteriskjava.live.ManagerCommunicationException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.live.MeetMeUser;
import java.util.Collection;
import java.util.Iterator;

import org.asteriskjava.live.MeetMeRoom;
import org.bigbluebutton.webconference.voice.asterisk.AbstractAsteriskServerListener;
import org.red5.logging.Red5LoggerFactory;
import org.asteriskjava.manager.ManagerConnectionState;

class MeetMeApplication extends AbstractAsteriskServerListener {
	private static Logger log = Red5LoggerFactory.getLogger(MeetMeApplication.class, "bigbluebutton");

	private ManagerConnection managerConnection;	
	private DefaultAsteriskServer asteriskServer = new DefaultAsteriskServer();
	private UserStateChangeListener userStateListener;
	
	void startup() {
		log.info("Staring meetme application");
		asteriskServer = new DefaultAsteriskServer();    
		asteriskServer.setManagerConnection(managerConnection);
		try {
			asteriskServer.addAsteriskServerListener(this);
			asteriskServer.initialize();
		} catch (ManagerCommunicationException e) {
			log.error("ManagerCommunicationException while starting meetme application");
		}						
	}
	
	void shutdown() {
		log.info("Shutting down meetme application");
		asteriskServer.shutdown();
	}
	
	void mute(Integer user, String conference, Boolean mute) {
		log.debug("mute: [" + user + "," + conference + "," + mute + "]");
		MeetMeRoom room = getMeetMeRoom(conference);
		
		if (room == null) {
			log.warn("Cannot mute user from non-existing meetme room");
			return;
		}
		
		Collection<MeetMeUser> users = room.getUsers();		
		log.debug("room=" + conference + ",users=" + users.size());
		for (Iterator<MeetMeUser> it = users.iterator(); it.hasNext();) {
    		MeetMeUser muser = (MeetMeUser) it.next();
    		log.debug("user:" + user + "muser=" + muser.getUserNumber());
    		if (user.intValue() == muser.getUserNumber().intValue()) {
    			muteUser(muser, mute);
    		}
    	}
	}

	void mute(String conference, Boolean mute) {
		log.debug("Mute: [" + conference + "," + mute + "]");
		MeetMeRoom room = getMeetMeRoom(conference);
		
		if (room == null) {
			log.warn("Cannot mute everybody from non-existing meetme room");
			return;
		}
		
		Collection<MeetMeUser> users = room.getUsers();		
		for (Iterator<MeetMeUser> it = users.iterator(); it.hasNext();) {
    		MeetMeUser muser = (MeetMeUser) it.next();    		
    		muteUser(muser, mute);
    	}
	}
	
	private void muteUser(MeetMeUser user, Boolean mute) {
		log.debug("MuteUser: [" + user.getUserNumber() + "," + user.getRoom().getRoomNumber() + "," + mute + "]");
		try {
			if (mute) user.mute();
			else user.unmute();
		} catch (ManagerCommunicationException e) {
			log.warn("ManagerCommunicationException while trying to mute user");
		}
	}
	
	void kick(Integer user, String conference) {
		log.debug("Kick: [" + conference + "," + user + "]");
		MeetMeRoom room = getMeetMeRoom(conference);
		
		if (room == null) {
			log.warn("Cannot kick user from non-existing meetme room");
			return;
		}
		
		Collection<MeetMeUser> users = room.getUsers();		
		for (Iterator<MeetMeUser> it = users.iterator(); it.hasNext();) {
    		MeetMeUser muser = (MeetMeUser) it.next();
    		if (user.intValue() == muser.getUserNumber().intValue()) {
    			kickUser(muser);
    		}
    	}
	}
		
	void kick(String conference){
		log.debug("Kick: [" + conference + "]");
		MeetMeRoom room = getMeetMeRoom(conference);
		
		if (room == null) {
			log.warn("Cannot kick everybody from non-existing meetme room");
			return;
		}
		
		Collection<MeetMeUser> users = room.getUsers();		
		for (Iterator<MeetMeUser> it = users.iterator(); it.hasNext();) {
    		MeetMeUser muser = (MeetMeUser) it.next();    		
    		kickUser(muser);
    	}
	}
	
	private void kickUser(MeetMeUser user) {
		log.debug("KickUser: [" + user.getUserNumber() + "," + user.getRoom().getRoomNumber() + "]");
		try {
			user.kick();
		} catch (ManagerCommunicationException e) {
			log.warn("ManagerCommunicationException while trying to kick user");
		}
	}
	
	void initializeRoom(String conference){
		log.debug("initialize " + conference);
		MeetMeRoom room = getMeetMeRoom(conference);
		
		if (room == null) {
			log.warn("Cannot initialize non-existing meetme room");
			return;
		}
		
		if (room.isEmpty()) {
			log.debug(conference + " is empty.");
			return;
		}
		
		Collection<MeetMeUser> users = room.getUsers();		
		for (Iterator<MeetMeUser> it = users.iterator(); it.hasNext();) {
    		MeetMeUser muser = (MeetMeUser) it.next();    		
    		onNewMeetMeUser(muser);
    	}
	}
	
    public void onNewMeetMeUser(MeetMeUser user) {
		log.info("New user joined meetme room: " + user.getRoom() + 
				" " + user.getChannel().getCallerId().getName());
		// add a listener for changes to this user
		user.addPropertyChangeListener(userStateListener);
		userStateListener.handleNewUserJoined(user);
    }
        	
	MeetMeRoom getMeetMeRoom(String room) {
		if (managerConnection.getState() != ManagerConnectionState.CONNECTED) {
			log.error("No connection to the Asterisk server. Connection state is {}", managerConnection.getState().toString());
			return null;
		}
		
		try {
			MeetMeRoom mr = asteriskServer.getMeetMeRoom(room);
			return mr;
		} catch (ManagerCommunicationException e) {
			log.error("Exception error when trying to get conference ${room}");
		}
		return null;
	}
	
	public void setManagerConnection(ManagerConnection connection) {
		log.debug("setting manager connection");
		this.managerConnection = connection;
		log.debug("setting manager connection DONE");
	}
		
	public void setUserStateListener(UserStateChangeListener listener) {
		userStateListener = listener;
	}
}
