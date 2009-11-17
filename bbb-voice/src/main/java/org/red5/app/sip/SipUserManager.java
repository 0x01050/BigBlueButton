package org.red5.app.sip;

import org.slf4j.Logger;
import org.red5.logging.Red5LoggerFactory;

import java.util.*;

public final class SipUserManager {
	private static final Logger log = Red5LoggerFactory.getLogger( SipUserManager.class, "sip" );
	
    private static Map<String, SipUser> sessions;
    private static SipUserManager singleton = new SipUserManager();

    public static SipUserManager getInstance() {
        return singleton;
    }

    private SipUserManager() {
        sessions = Collections.synchronizedMap(new HashMap<String, SipUser>());
    }

    public void createSipUser(String userid, RtmpConnection connection, int sipPort, int rtpPort) {
    	SipUser sipUser = new SipUser(userid, connection, sipPort, rtpPort);
    	sessions.put(userid, sipUser);
    }
    
    public void login(String userid, String obproxy, String phone, String username, String password, String realm, String proxy) {
    	SipUser sipUser = sessions.get(userid);
    	if (sipUser != null) {
    		sipUser.login(obproxy, phone, username, password, realm, proxy);
    	}
    }
    
    public void registerSipUser(String userid) {
    	SipUser sipUser = sessions.get(userid);
    	if (sipUser != null) {
    		sipUser.register();
    	}
    }
    
    public void call(String userid, String destination) {
    	SipUser sipUser = sessions.get(userid);
    	if (sipUser != null) {
    		sipUser.call(destination);
    	}
    }
    
    public void passDtmf(String userid, String digits) {
    	SipUser sipUser = sessions.get(userid);
    	if (sipUser != null) {
    		sipUser.dtmf(digits);
    	}
    }
    
    public void accept(String userid) {
    	SipUser sipUser = sessions.get(userid);
    	if (sipUser != null) {
    		sipUser.accept();
    	}
    }
    
    public void unregister(String userid) {
    	SipUser sipUser = sessions.get(userid);
    	if (sipUser != null) {
    		sipUser.unregister();
    	}
    }
    
    public void hangup(String userid) {
    	SipUser sipUser = sessions.get(userid);
    	if (sipUser != null) {
    		sipUser.hangup();
    	}
    }
    
    public void streamStatus(String userid, String status) {
    	SipUser sipUser = sessions.get(userid);
    	if (sipUser != null) {
    		sipUser.streamStatus(status);
    	}
    }
    
    public void startTalkStream(String userid, String callid) {
    	SipUser sipUser = sessions.get(userid);
    	if (sipUser != null) {
    		sipUser.startTalkStream(callid);
    	}
    }
    
    private void remove(String userid) {
    	log.debug("Number of SipUsers in Manager before remove {}", sessions.size());
        sessions.remove(userid);
    }


    public Collection< SipUser > getSIPUsers() {
        return sessions.values();
    }


    public int getNumberOfSessions() {
        return sessions.size();
    }


    public void close(String userid) {
    	SipUser sipUser = sessions.get(userid);
    	if (sipUser != null) {
    		sipUser.close();
    		remove(userid);
    	}
    }

    public void destroyAllSessions() {
        Collection sipUsers = getSIPUsers();
        User sipUser;

        for (Iterator iter = sipUsers.iterator(); iter.hasNext();) {
            sipUser = (User) iter.next();
            sipUser.close();

            sipUser = null;
        }

        sessions = new HashMap<String, SipUser>();
    }

}
