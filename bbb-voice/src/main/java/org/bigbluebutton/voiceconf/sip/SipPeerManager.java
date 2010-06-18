package org.bigbluebutton.voiceconf.sip;

import org.slf4j.Logger;
import org.zoolu.sip.provider.SipStack;
import org.red5.logging.Red5LoggerFactory;
import java.util.*;

/**
 * Manages all connections to the sip voice conferencing server.
 * @author Richard Alam
 *
 */
public final class SipPeerManager {
	private static final Logger log = Red5LoggerFactory.getLogger( SipPeerManager.class, "sip" );
	
	private ClientConnectionManager clientConnManager;
	private CallStreamFactory callStreamFactory;
	
    private Map<String, SipPeer> sipPeers;
    private int sipStackDebugLevel = 8;
    
    public SipPeerManager() {
        sipPeers = Collections.synchronizedMap(new HashMap<String, SipPeer>());
    }

    public void createSipPeer(String peerId, String host, int sipPort, int startRtpPort, int stopRtpPort) {
    	SipPeer sipPeer = new SipPeer(peerId, host, sipPort, startRtpPort, stopRtpPort);
    	sipPeer.setClientConnectionManager(clientConnManager);
    	sipPeer.setCallStreamFactory(callStreamFactory);
    	sipPeers.put(peerId, sipPeer);    	
    }
    
    public void register(String peerId, String username, String password) throws PeerNotFoundException {
    	SipPeer sipPeer = sipPeers.get(peerId);
    	if (sipPeer == null) throw new PeerNotFoundException("Can't find sip peer " + peerId);
  		sipPeer.register(username, password);
    }
        
    public void call(String peerId, String clientId, String callerName, String destination) throws PeerNotFoundException {
    	SipPeer sipPeer = sipPeers.get(peerId);
    	if (sipPeer == null) throw new PeerNotFoundException("Can't find sip peer " + peerId);
    	sipPeer.call(clientId, callerName, destination);
    }
    
    public void passDtmf(String peerId, String clientId, String digits) {
    	SipPeer sipPeer = sipPeers.get(peerId);
    	if (sipPeer != null) {
    		sipPeer.dtmf(clientId, digits);
    	}
    }
        
    public void unregister(String userid) {
    	SipPeer sipUser = sipPeers.get(userid);
    	if (sipUser != null) {
    		sipUser.unregister();
    	}
    }
    
    public void hangup(String peerId, String clientId) {
    	SipPeer sipPeer = sipPeers.get(peerId);
    	if (sipPeer != null) {
    		sipPeer.hangup(clientId);
    	}
    }

    /*
    public void startTalkStream(String userid, IBroadcastStream broadcastStream, IScope scope) {
    	SipPeer sipUser = sipPeers.get(userid);
    	if (sipUser != null) {
    		sipUser.startTalkStream(broadcastStream, scope);
    	}
    }
    
    public void stopTalkStream(String userid, IBroadcastStream broadcastStream, IScope scope) {
    	SipPeer sipUser = sipPeers.get(userid);
    	if (sipUser != null) {
    		sipUser.stopTalkStream(broadcastStream, scope);
    	}
    }
 */
    
    private void remove(String userid) {
    	log.debug("Number of SipUsers in Manager before remove {}", sipPeers.size());
        sipPeers.remove(userid);
    }

    public void close(String userid) {
    	SipPeer sipUser = sipPeers.get(userid);
    	if (sipUser != null) {
    		sipUser.close();
    		remove(userid);
    	}
    }

    public void destroyAllSessions() {
    	Collection<SipPeer> sipUsers = sipPeers.values();
        SipPeer sp;

        for (Iterator<SipPeer> iter = sipUsers.iterator(); iter.hasNext();) {
            sp = (SipPeer) iter.next();
            sp.close();
            sp = null;
        }

        sipPeers = new HashMap<String, SipPeer>();
    }

	public void setSipStackDebugLevel(int sipStackDebugLevel) {
		this.sipStackDebugLevel = sipStackDebugLevel;
		SipStack.init();
        SipStack.debug_level = sipStackDebugLevel;
        SipStack.log_path = "log"; 
	}
	
	public void setCallStreamFactory(CallStreamFactory csf) {
		callStreamFactory = csf;
	}
	
	public void setClientConnectionManager(ClientConnectionManager ccm) {
		clientConnManager = ccm;
	}
}
