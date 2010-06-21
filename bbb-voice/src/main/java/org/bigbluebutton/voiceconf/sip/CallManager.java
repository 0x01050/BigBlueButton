package org.bigbluebutton.voiceconf.sip;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CallManager {

	private final Map<String, CallAgent> calls = new ConcurrentHashMap<String, CallAgent>();
	
	public CallAgent add(CallAgent ca) {
		return calls.put(ca.getCallId(), ca);
	}
	
	public CallAgent remove(String id) {
		return calls.remove(id);
	}
	
	public CallAgent get(String id) {
		return calls.get(id);
	}
	
	public Collection<CallAgent> getAll() {
		return calls.values();
	}
}
