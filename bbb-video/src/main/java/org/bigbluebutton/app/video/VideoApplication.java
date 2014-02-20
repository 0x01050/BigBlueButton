/**
* BigBlueButton open source conferencing system - http://www.bigbluebutton.org/
* 
* Copyright (c) 2012 BigBlueButton Inc. and by respective authors (see below).
*
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License as published by the Free Software
* Foundation; either version 3.0 of the License, or (at your option) any later
* version.
* 
* BigBlueButton is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
* PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License along
* with BigBlueButton; if not, see <http://www.gnu.org/licenses/>.
*
*/
package org.bigbluebutton.app.video;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.red5.logging.Red5LoggerFactory;
import org.red5.server.adapter.MultiThreadedApplicationAdapter;
import org.red5.server.api.IConnection;
import org.red5.server.api.Red5;
import org.red5.server.api.scope.IScope;
import org.red5.server.api.scope.IBasicScope;
import org.red5.server.api.scope.IBroadcastScope;
import org.red5.server.api.scope.ScopeType;
import org.red5.server.api.stream.IBroadcastStream;
import org.red5.server.api.stream.IPlayItem;
import org.red5.server.api.stream.IServerStream;
import org.red5.server.api.stream.IStreamListener;
import org.red5.server.api.stream.ISubscriberStream;
import org.red5.server.stream.ClientBroadcastStream;
import org.red5.client.StreamRelay;
import org.slf4j.Logger;

public class VideoApplication extends MultiThreadedApplicationAdapter {
	private static Logger log = Red5LoggerFactory.getLogger(VideoApplication.class, "video");
	
	private IScope appScope;
	private IServerStream serverStream;
	
	private boolean recordVideoStream = false;
	private EventRecordingService recordingService;
	private final Map<String, IStreamListener> streamListeners = new HashMap<String, IStreamListener>();

    private Map<String, StreamRelay> remoteStreams = new ConcurrentHashMap<String, StreamRelay>();

	
    @Override
	public boolean appStart(IScope app) {
	    super.appStart(app);
		log.info("oflaDemo appStart");
		System.out.println("oflaDemo appStart");    	
		appScope = app;
		return true;
	}

    @Override
	public boolean appConnect(IConnection conn, Object[] params) {
		log.info("oflaDemo appConnect"); 
       
        return super.appConnect(conn, params);
	}

    @Override
	public void appDisconnect(IConnection conn) {
		log.info("oflaDemo appDisconnect");
		if (appScope == conn.getScope() && serverStream != null) {
			serverStream.close();
		}
		super.appDisconnect(conn);
	}
    
    @Override
    public void streamPublishStart(IBroadcastStream stream) {
    	super.streamPublishStart(stream);
    }
    

    public IBroadcastScope getBroadcastScope(IScope scope, String name) {
    IBasicScope basicScope = scope.getBasicScope(ScopeType.BROADCAST, name);
    if (!(basicScope instanceof IBroadcastScope)) {
        return null;
    } else {
        return (IBroadcastScope) basicScope;
    }
}


    @Override
    public void streamBroadcastStart(IBroadcastStream stream) {
    	IConnection conn = Red5.getConnectionLocal();  
    	super.streamBroadcastStart(stream);
    	log.info("streamBroadcastStart " + stream.getPublishedName() + " " + System.currentTimeMillis() + " " + conn.getScope().getName());

        //if (recordVideoStream) {
	    //	recordStream(stream);
	    //	VideoStreamListener listener = new VideoStreamListener(); 
	     //   listener.setEventRecordingService(recordingService);
	     //   stream.addStreamListener(listener); 
	     //   streamListeners.put(conn.getScope().getName() + "-" + stream.getPublishedName(), listener);
       // }

       /* System.out.println("TESTE " + stream.getPublishedName());
        System.out.println("TESTE");
        System.out.println("TESTE");
        System.out.println("TESTE");
        System.out.println("TESTE");
        System.out.println("TESTE");*/
        //IScope scope = stream.getScope();
        //IBroadcastScope bsScope = getBroadcastScope(scope, stream.getPublishedName());
        //StreamingProxy proxy = new StreamingProxy();
        //proxy.setHost("143.54.10.163");
        //proxy.setApp("video");
        //proxy.setPort(1935);
        //proxy.init();
        //bsScope.subscribe(proxy, null);
        //proxy.start("teste", StreamingProxy.LIVE, null);
        //streamingProxyMap.put(stream.getPublishedName(), proxy);
        //stream.addStreamListener(this);
    }

    @Override
    public void streamBroadcastClose(IBroadcastStream stream) {
    	IConnection conn = Red5.getConnectionLocal();  
    	super.streamBroadcastClose(stream);
    	
    	if (recordVideoStream) {
    		IStreamListener listener = streamListeners.remove(conn.getScope().getName() + "-" + stream.getPublishedName());
    		if (listener != null) {
    			stream.removeStreamListener(listener);
    		}
    		
        	long publishDuration = (System.currentTimeMillis() - stream.getCreationTime()) / 1000;
        	log.info("streamBroadcastClose " + stream.getPublishedName() + " " + System.currentTimeMillis() + " " + conn.getScope().getName());
    		Map<String, String> event = new HashMap<String, String>();
    		event.put("module", "WEBCAM");
    		event.put("timestamp", new Long(System.currentTimeMillis()).toString());
    		event.put("meetingId", conn.getScope().getName());
    		event.put("stream", stream.getPublishedName());
    		event.put("duration", new Long(publishDuration).toString());
    		event.put("eventName", "StopWebcamShareEvent");
    		
    		recordingService.record(conn.getScope().getName(), event);    		
    	}
    }
    
    /**
     * A hook to record a stream. A file is written in webapps/video/streams/
     * @param stream
     */
    private void recordStream(IBroadcastStream stream) {
    	IConnection conn = Red5.getConnectionLocal();   
    	long now = System.currentTimeMillis();
    	String recordingStreamName = stream.getPublishedName(); // + "-" + now; /** Comment out for now...forgot why I added this - ralam */
     
    	try {    		
    		log.info("Recording stream " + recordingStreamName );
    		ClientBroadcastStream cstream = (ClientBroadcastStream) this.getBroadcastStream(conn.getScope(), stream.getPublishedName());
    		cstream.saveAs(recordingStreamName, false);
    	} catch(Exception e) {
    		log.error("ERROR while recording stream " + e.getMessage());
    		e.printStackTrace();
    	}    	
    }

	public void setRecordVideoStream(boolean recordVideoStream) {
		this.recordVideoStream = recordVideoStream;
	}
	
	public void setEventRecordingService(EventRecordingService s) {
		recordingService = s;
	}

    @Override
    public void streamPlayItemPlay(ISubscriberStream stream, IPlayItem item, boolean isLive) {
        // log w3c connect event
        String streamName = item.getName();
        
        if(streamName.contains("remote")) {
            String[] parts = streamName.split("/");
            if(remoteStreams.containsKey(parts.length-1) == false) {
                StreamRelay remoteRelay = null;
                String conference = Red5.getConnectionLocal().getScope().getName();
                String host = Red5.getConnectionLocal().getHost();
                String[] initRelay = new String[7];
                
                initRelay[0] = parts[parts.length-2];
                initRelay[3] = host;
                initRelay[4] = "video" + "/" + conference; 
                initRelay[5] = streamName;
                initRelay[6] = "live";
                
               if(parts.length > 4) {
                    initRelay[1] = "video/remote";
                    String aux = "remote/" + parts[1] + "/";
                    for(int i = 2; i <= parts.length-3; i++) {
                        aux = aux + parts[i] + "/";
                    }
                    aux = aux + parts[parts.length-1];
                    initRelay[2] = aux;
                }
                else {
                    initRelay[1] = "video/" + parts[1];
                    initRelay[2] = parts[parts.length-1];
                }

                remoteRelay = new StreamRelay(initRelay);
                remoteStreams.put(streamName, remoteRelay);
            }
        }
        
        //URL
        //rtmp://143.54.10.63/video/conferenciaNesseServidor/
        //stremName: remote/conferenciaOrigem/143.54.10.22/streamName

       // parts[0] = "remote"
       // parts[1] = "conferenciaOrigem"
       // parts[2] = "143.54.10.22"
       // parts[3] = "143.54.10.21"
       // parts[4] = "143.54.10.20"
       // parts[5] = 'streamName"

        log.info("W3C x-category:stream x-event:play c-ip:{} x-sname:{} x-name:{}", new Object[] { Red5.getConnectionLocal().getRemoteAddress(), stream.getName(), item.getName() });
    }
	
}
