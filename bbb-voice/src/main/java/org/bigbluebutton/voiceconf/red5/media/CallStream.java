package org.bigbluebutton.voiceconf.red5.media;

import java.net.DatagramSocket;
import java.net.SocketException;
import org.bigbluebutton.voiceconf.red5.media.transcoder.NellyToPcmTranscoder;
import org.bigbluebutton.voiceconf.red5.media.transcoder.PcmToNellyTranscoder;
import org.bigbluebutton.voiceconf.red5.media.transcoder.SpeexToSpeexTranscoder;
import org.bigbluebutton.voiceconf.red5.media.transcoder.Transcoder;
import org.bigbluebutton.voiceconf.sip.SipConnectInfo;
import org.red5.app.sip.codecs.Codec;
import org.red5.app.sip.codecs.SpeexCodec;
import org.slf4j.Logger;
import org.red5.logging.Red5LoggerFactory;
import org.red5.server.api.IScope;
import org.red5.server.api.stream.IBroadcastStream;

public class CallStream implements StreamObserver {
    private final static Logger log = Red5LoggerFactory.getLogger(CallStream.class, "sip");

    private DatagramSocket socket = null;
    private FlashToSipAudioStream userTalkStream;
    private SipToFlashAudioStream userListenStream;
    private final Codec sipCodec;
    private final SipConnectInfo connInfo;
    private final IScope scope;
    
    public CallStream(Codec sipCodec, SipConnectInfo connInfo, IScope scope) {        
    	this.sipCodec = sipCodec;
    	this.connInfo = connInfo;
    	this.scope = scope;
    }
    
    public void start() throws Exception {        
    	try {
			socket = new DatagramSocket(connInfo.getLocalPort());
		} catch (SocketException e) {
			log.error("SocketException while initializing DatagramSocket");
			throw new Exception("Exception while initializing CallStream");
		}     
        		
		Transcoder rtmpToRtpTranscoder, rtpToRtmpTranscoder;
		if (sipCodec.getCodecId() == SpeexCodec.codecId) {
			rtmpToRtpTranscoder = new SpeexToSpeexTranscoder(sipCodec);
			rtpToRtmpTranscoder = new SpeexToSpeexTranscoder(sipCodec, userListenStream);
		} else {
			rtmpToRtpTranscoder = new NellyToPcmTranscoder(sipCodec);
			rtpToRtmpTranscoder = new PcmToNellyTranscoder(sipCodec, userListenStream);			
		}
			
		userListenStream = new SipToFlashAudioStream(scope, rtpToRtmpTranscoder, socket);
		userListenStream.addListenStreamObserver(this);
		userTalkStream = new FlashToSipAudioStream(rtmpToRtpTranscoder, socket, connInfo); 
    }
    
    public String getTalkStreamName() {
    	return userTalkStream.getStreamName();
    }
    
    public String getListenStreamName() {
    	return userListenStream.getStreamName();
    }
    
    public void startTalkStream(IBroadcastStream broadcastStream, IScope scope) throws StreamException {
    	userTalkStream.start(broadcastStream, scope);
    }
    
    public void stopTalkStream(IBroadcastStream broadcastStream, IScope scope) {
    	userTalkStream.stop(broadcastStream, scope);
    }

    public void stop() {
        userListenStream.stop();
    }

	@Override
	public void onStreamStopped() {
		socket.close();
	}
}
