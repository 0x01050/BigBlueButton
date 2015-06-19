package org.bigbluebutton.freeswitch.voice.freeswitch;


import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bigbluebutton.freeswitch.voice.events.ConferenceEventListener;
import org.bigbluebutton.freeswitch.voice.events.DeskShareEndedEvent;
import org.bigbluebutton.freeswitch.voice.events.DeskShareRecordingEvent;
import org.bigbluebutton.freeswitch.voice.events.DeskShareStartedEvent;
import org.bigbluebutton.freeswitch.voice.events.DeskShareViewerJoinedEvent;
import org.bigbluebutton.freeswitch.voice.events.DeskShareViewerLeftEvent;
import org.bigbluebutton.freeswitch.voice.events.VoiceConferenceEvent;
import org.bigbluebutton.freeswitch.voice.events.VoiceStartRecordingEvent;
import org.bigbluebutton.freeswitch.voice.events.VoiceUserJoinedEvent;
import org.bigbluebutton.freeswitch.voice.events.VoiceUserLeftEvent;
import org.bigbluebutton.freeswitch.voice.events.VoiceUserMutedEvent;
import org.bigbluebutton.freeswitch.voice.events.VoiceUserTalkingEvent;
import org.freeswitch.esl.client.IEslEventListener;
import org.freeswitch.esl.client.transport.event.EslEvent;
import org.jboss.netty.channel.ExceptionEvent;

public class ESLEventListener implements IEslEventListener {

    private static final String START_TALKING_EVENT = "start-talking";
    private static final String STOP_TALKING_EVENT = "stop-talking";
    private static final String START_RECORDING_EVENT = "start-recording";
    private static final String STOP_RECORDING_EVENT = "stop-recording";

    private static final String DESKSHARE_CONFERENCE_NAME_LABEL = "-DESKSHARE";
    private static final String DESKSHARE_CALLER_NAME_LABEL = " (Screen)";
    private static final String DESKSHARE_CALLER_ID_LABEL = " (screen)";

    private final ConferenceEventListener conferenceEventListener;
    
    public ESLEventListener(ConferenceEventListener conferenceEventListener) {
    	this.conferenceEventListener = conferenceEventListener;
    }
    
    @Override
    public void conferenceEventPlayFile(String uniqueId, String confName, int confSize, EslEvent event) {
        //Ignored, Noop
    }

    @Override
    public void backgroundJobResultReceived(EslEvent event) {
        System.out.println( "Background job result received [" + event + "]");
    }

    @Override
    public void exceptionCaught(ExceptionEvent e) {
//        setChanged();
//        notifyObservers(e);
    }

    private static final Pattern GLOBAL_AUDION_PATTERN = Pattern.compile("(GLOBAL_AUDIO)_(.*)$");
    private static final Pattern CALLERNAME_PATTERN = Pattern.compile("(.*)-bbbID-(.*)$");
    
    @Override
    public void conferenceEventJoin(String uniqueId, String confName, int confSize, EslEvent event) {

        Integer memberId = this.getMemberIdFromEvent(event);
        Map<String, String> headers = event.getEventHeaders();
        String callerId = this.getCallerIdFromEvent(event);
        String callerIdName = this.getCallerIdNameFromEvent(event);
        boolean muted = headers.get("Speak").equals("true") ? false : true; //Was inverted which was causing a State issue
        boolean speaking = headers.get("Talking").equals("true") ? true : false;

        String voiceUserId = callerIdName;

        System.out.println("User joined voice conference, user=[" + callerIdName + "], conf=[" + confName + "]");

        Matcher gapMatcher = GLOBAL_AUDION_PATTERN.matcher(callerIdName);
        if (gapMatcher.matches()) {
        	System.out.println("Ignoring GLOBAL AUDIO USER [" + callerIdName + "]");
        	return;
        }

        // Deskstop sharing conferences have their name in the form xxxxx-DESKSHARE
        if (confName.endsWith(DESKSHARE_CONFERENCE_NAME_LABEL)) {
            // Deskstop sharing conferences have the user with the desktop video displayed in this way:
            // username (Screen) and usernum (screen)
            if (callerId.endsWith(DESKSHARE_CALLER_ID_LABEL) && callerIdName.endsWith(DESKSHARE_CALLER_NAME_LABEL)) {
                DeskShareStartedEvent dsStart = new DeskShareStartedEvent(confName, callerId, callerIdName);
                conferenceEventListener.handleConferenceEvent(dsStart);
                return; //TODO do we need it?
            } else {
                DeskShareViewerJoinedEvent dsJoined = new DeskShareViewerJoinedEvent(confName, callerId, callerIdName);
                conferenceEventListener.handleConferenceEvent(dsJoined);
        	}
        }

        Matcher matcher = CALLERNAME_PATTERN.matcher(callerIdName);
        if (matcher.matches()) {
            voiceUserId = matcher.group(1).trim();
            callerIdName = matcher.group(2).trim();
        }

        VoiceUserJoinedEvent pj = new VoiceUserJoinedEvent(voiceUserId, memberId.toString(), confName, callerId, callerIdName, muted, speaking);
        conferenceEventListener.handleConferenceEvent(pj);
    }

    @Override
    public void conferenceEventLeave(String uniqueId, String confName, int confSize, EslEvent event) {   	
        Integer memberId = this.getMemberIdFromEvent(event);
        System.out.println("User left voice conference, user=[" + memberId.toString() + "], conf=[" + confName + "]");

        // Deskstop sharing conferences have their name in the form xxxxx-DESKSHARE
        if (confName.endsWith(DESKSHARE_CONFERENCE_NAME_LABEL)) {
            String callerId = this.getCallerIdFromEvent(event);
            String callerIdName = this.getCallerIdNameFromEvent(event);
            // Deskstop sharing conferences have the user with the desktop video displayed in this way:
            // username (Screen) and usernum (screen)
            if (callerId.endsWith(DESKSHARE_CALLER_ID_LABEL) && callerIdName.endsWith(DESKSHARE_CALLER_NAME_LABEL)) {
                DeskShareEndedEvent dsEnd = new DeskShareEndedEvent(confName, callerId, callerIdName);
                conferenceEventListener.handleConferenceEvent(dsEnd);
                return;//TODO do we need it?
            } else {
                DeskShareViewerLeftEvent dsLeft = new DeskShareViewerLeftEvent(confName, callerId, callerIdName);
                conferenceEventListener.handleConferenceEvent(dsLeft);
            }
        }

        VoiceUserLeftEvent pl = new VoiceUserLeftEvent(memberId.toString(), confName);
        conferenceEventListener.handleConferenceEvent(pl);
    }

    @Override
    public void conferenceEventMute(String uniqueId, String confName, int confSize, EslEvent event) {
        Integer memberId = this.getMemberIdFromEvent(event);
        System.out.println("******************** Received Conference Muted Event from FreeSWITCH user[" + memberId.toString() + "]");
        System.out.println("User muted voice conference, user=[" + memberId.toString() + "], conf=[" + confName + "]");
        VoiceUserMutedEvent pm = new VoiceUserMutedEvent(memberId.toString(), confName, true);
        conferenceEventListener.handleConferenceEvent(pm);
    }

    @Override
    public void conferenceEventUnMute(String uniqueId, String confName, int confSize, EslEvent event) {
        Integer memberId = this.getMemberIdFromEvent(event);
        System.out.println("******************** Received ConferenceUnmuted Event from FreeSWITCH user[" + memberId.toString() + "]");
        System.out.println("User unmuted voice conference, user=[" + memberId.toString() + "], conf=[" + confName + "]");
        VoiceUserMutedEvent pm = new VoiceUserMutedEvent(memberId.toString(), confName, false);
        conferenceEventListener.handleConferenceEvent(pm);
    }

    @Override
    public void conferenceEventAction(String uniqueId, String confName, int confSize, String action, EslEvent event) {
        Integer memberId = this.getMemberIdFromEvent(event);
        VoiceUserTalkingEvent pt;
        
        System.out.println("******************** Receive conference Action [" + action + "]");
        
        if (action == null) {
            return;
        }

        if (action.equals(START_TALKING_EVENT)) {
            pt = new VoiceUserTalkingEvent(memberId.toString(), confName, true);
            conferenceEventListener.handleConferenceEvent(pt);        	
        } else if (action.equals(STOP_TALKING_EVENT)) {
            pt = new VoiceUserTalkingEvent(memberId.toString(), confName, false);
            conferenceEventListener.handleConferenceEvent(pt);        	
        } else {
        	System.out.println("Unknown conference Action [" + action + "]");
        }
    }

    @Override
    public void conferenceEventTransfer(String uniqueId, String confName, int confSize, EslEvent event) {
        //Ignored, Noop
    }

    @Override
    public void conferenceEventThreadRun(String uniqueId, String confName, int confSize, EslEvent event) {
    	
    }
    
    //@Override
    public void conferenceEventRecord(String uniqueId, String confName, int confSize, EslEvent event) {
        String action = event.getEventHeaders().get("Action");

        if(action == null) {
            return;
        }

        System.out.println("Handling conferenceEventRecord " + action);

        if (action.equals(START_RECORDING_EVENT)) {
            if (confName.endsWith(DESKSHARE_CONFERENCE_NAME_LABEL)){
                DeskShareRecordingEvent dssre = new DeskShareRecordingEvent(confName, true);
                dssre.setRecordingFilename(getRecordFilenameFromEvent(event));
                dssre.setTimestamp(genTimestamp().toString());
                System.out.println("DeskShare conference recording started. file=["
                 + getRecordFilenameFromEvent(event) + "], conf=[" + confName + "]");
                conferenceEventListener.handleConferenceEvent( (VoiceConferenceEvent)dssre);
            } else {
                VoiceStartRecordingEvent sre = new VoiceStartRecordingEvent(confName, true);
                sre.setRecordingFilename(getRecordFilenameFromEvent(event));
                sre.setTimestamp(genTimestamp().toString());
                System.out.println("Voice conference recording started. file=["
                 + getRecordFilenameFromEvent(event) + "], conf=[" + confName + "]");
                conferenceEventListener.handleConferenceEvent(sre);
            }
        } else if (action.equals(STOP_RECORDING_EVENT)) {
            if (confName.endsWith(DESKSHARE_CONFERENCE_NAME_LABEL)){
            	DeskShareRecordingEvent dssre = new DeskShareRecordingEvent(confName, false);
                dssre.setRecordingFilename(getRecordFilenameFromEvent(event));
                dssre.setTimestamp(genTimestamp().toString());
                System.out.println("DeskShare conference recording stopped. file=["
                 + getRecordFilenameFromEvent(event) + "], conf=[" + confName + "]");
                conferenceEventListener.handleConferenceEvent( (VoiceConferenceEvent)dssre);
            } else {
                VoiceStartRecordingEvent sre = new VoiceStartRecordingEvent(confName, false);
                sre.setRecordingFilename(getRecordFilenameFromEvent(event));
                sre.setTimestamp(genTimestamp().toString());
                System.out.println("Voice conference recording stopped. file=["
                 + getRecordFilenameFromEvent(event) + "], conf=[" + confName + "]");
                conferenceEventListener.handleConferenceEvent(sre);
            }
        } else {
            System.out.println("Processing UNKNOWN conference Action " + action + "]");
        }
    }

    private Long genTimestamp() {
    	return TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
    }
    
	@Override
	public void eventReceived(EslEvent event) {
		System.out.println("ESL Event Listener received event=[" + event.getEventName() + "]");
//        if (event.getEventName().equals(FreeswitchHeartbeatMonitor.EVENT_HEARTBEAT)) {
////           setChanged();
//           notifyObservers(event);
//           return; 
//        }
	}

    private Integer getMemberIdFromEvent(EslEvent e) {
        return new Integer(e.getEventHeaders().get("Member-ID"));
    }

    private String getCallerIdFromEvent(EslEvent e) {
        return e.getEventHeaders().get("Caller-Caller-ID-Number");
    }

    private String getCallerIdNameFromEvent(EslEvent e) {
        return e.getEventHeaders().get("Caller-Caller-ID-Name");
    }
    
    private String getRecordFilenameFromEvent(EslEvent e) {
    	return e.getEventHeaders().get("Path");
    }
}
