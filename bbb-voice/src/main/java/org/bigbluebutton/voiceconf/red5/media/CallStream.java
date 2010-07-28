/*
 * BigBlueButton - http://www.bigbluebutton.org
 * 
 * Copyright (c) 2008-2009 by respective authors (see below). All rights reserved.
 * 
 * BigBlueButton is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * Foundation; either version 3 of the License, or (at your option) any later 
 * version. 
 * 
 * BigBlueButton is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along 
 * with BigBlueButton; if not, If not, see <http://www.gnu.org/licenses/>.
 *
 * $Id: $
 */
package org.bigbluebutton.voiceconf.red5.media;

import java.net.DatagramSocket;
import java.net.SocketException;
import org.bigbluebutton.voiceconf.red5.media.transcoder.NellyToPcmTranscoder;
import org.bigbluebutton.voiceconf.red5.media.transcoder.PcmToNellyTranscoder;
import org.bigbluebutton.voiceconf.red5.media.transcoder.RtmpToRtpSpeexTranscoder;
import org.bigbluebutton.voiceconf.red5.media.transcoder.RtpToRtmpSpeexTranscoder;
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
    
    public void start() {        
    	Transcoder rtmpToRtpTranscoder, rtpToRtmpTranscoder;
    	System.out.println("Using codec " + sipCodec.getCodecId() + " " + sipCodec.getCodecName());
		if (sipCodec.getCodecId() == SpeexCodec.codecId) {
			System.out.println("Using SPEEX codec " + sipCodec.getCodecId() + " " + sipCodec.getCodecName());
			rtmpToRtpTranscoder = new RtmpToRtpSpeexTranscoder(sipCodec);
			rtpToRtmpTranscoder = new RtpToRtmpSpeexTranscoder(sipCodec);

		} else {
			rtmpToRtpTranscoder = new NellyToPcmTranscoder(sipCodec);
			rtpToRtmpTranscoder = new PcmToNellyTranscoder(sipCodec);	
		}
		
		System.out.println("Packetization [" + sipCodec.getIncomingPacketization() + "," + sipCodec.getOutgoingPacketization() + "]");
		System.out.println("Outgoing Frame size [" + sipCodec.getOutgoingEncodedFrameSize() + ", " + sipCodec.getOutgoingDecodedFrameSize() + "]");
		System.out.println("Incoming Frame size [" + sipCodec.getIncomingEncodedFrameSize() + ", " + sipCodec.getIncomingDecodedFrameSize() + "]");
		userListenStream = new SipToFlashAudioStream(scope, rtpToRtmpTranscoder, connInfo.getSocket());
		userListenStream.addListenStreamObserver(this);	
		rtpToRtmpTranscoder.addTranscodedAudioDataListener(userListenStream);
		userTalkStream = new FlashToSipAudioStream(rtmpToRtpTranscoder, connInfo.getSocket(), connInfo); 
    }
    
    public String getTalkStreamName() {
    	return userTalkStream.getStreamName();
    }
    
    public String getListenStreamName() {
    	return userListenStream.getStreamName();
    }
    
    public void startTalkStream(IBroadcastStream broadcastStream, IScope scope) throws StreamException {
    	userListenStream.start();
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

	}
}
