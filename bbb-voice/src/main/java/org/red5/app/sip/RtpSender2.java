package org.red5.app.sip;

import local.net.RtpPacket;
import local.net.RtpSocket;
import java.net.DatagramSocket;
import java.net.SocketException;

import org.slf4j.Logger;
import org.red5.logging.Red5LoggerFactory;

public class RtpSender2 {
    protected static Logger log = Red5LoggerFactory.getLogger( RtpSender2.class, "sip" );

    public static int RTP_HEADER_SIZE = 12;
    private RtpSocket rtpSocket = null;

    private boolean socketIsLocal = false;
    private byte[] packetBuffer;
    private RtpPacket rtpPacket;
    private int startPayloadPos;
    private int dtmf2833Type = 101;
    private int sequenceNum = 0;
    private long timestamp = 0;
    private NellyToPcmTranscoder2 transcoder;
    
    public RtpSender2(NellyToPcmTranscoder2 transcoder, DatagramSocket srcSocket, String destAddr, int destPort) {
        this.transcoder = transcoder;
        if (srcSocket == null) {
        	try {
				srcSocket = new DatagramSocket();
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            socketIsLocal = true;
        }
    }

    public void start() {
        packetBuffer = new byte[transcoder.getOutgoingEncodedFrameSize() + RTP_HEADER_SIZE];
        rtpPacket = new RtpPacket(packetBuffer, 0);
        rtpPacket.setPayloadType(transcoder.getCodecId());
        startPayloadPos = rtpPacket.getHeaderLength();

        sequenceNum = 0;
        timestamp = 0;
    }

    public void queueSipDtmfDigits(String dtmfDigits) {
        byte[] dtmfbuf = new byte[transcoder.getOutgoingEncodedFrameSize() + RTP_HEADER_SIZE];
        RtpPacket dtmfpacket = new RtpPacket(dtmfbuf, 0);
        dtmfpacket.setPayloadType(dtmf2833Type);
        dtmfpacket.setPayloadLength(transcoder.getOutgoingEncodedFrameSize());

        byte[] blankbuf = new byte[transcoder.getOutgoingEncodedFrameSize() + RTP_HEADER_SIZE];
        RtpPacket blankpacket = new RtpPacket(blankbuf, 0);
        blankpacket.setPayloadType(transcoder.getCodecId());
        blankpacket.setPayloadLength(transcoder.getOutgoingEncodedFrameSize());

        for (int d = 0; d < dtmfDigits.length(); d++) {
            char digit = dtmfDigits.charAt(d);
            if (digit == '*') {
                dtmfbuf[startPayloadPos] = 10;
            }
            else if (digit == '#') {
                dtmfbuf[startPayloadPos] = 11;
            }
            else if (digit >= 'A' && digit <= 'D') {
                dtmfbuf[startPayloadPos] = (byte) (digit - 53);
            }
            else {
                dtmfbuf[startPayloadPos] = (byte) (digit - 48);
            }

            // notice we are bumping times/seqn just like audio packets
            try {
                // send start event packet 3 times
                dtmfbuf[startPayloadPos + 1] = 0; // start event flag
                // and volume
                dtmfbuf[startPayloadPos + 2] = 1; // duration 8 bits
                dtmfbuf[startPayloadPos + 3] = -32; // duration 8 bits

                for (int r = 0; r < 3; r++) {
                    dtmfpacket.setSequenceNumber(sequenceNum++);
                    dtmfpacket.setTimestamp(transcoder.getOutgoingEncodedFrameSize());
                    doRtpDelay();
                    rtpSocketSend(dtmfpacket);
                }

                // send end event packet 3 times
                dtmfbuf[startPayloadPos + 1] = -128; // end event flag
                dtmfbuf[startPayloadPos + 2] = 3; // duration 8 bits
                dtmfbuf[startPayloadPos + 3] = 116; // duration 8 bits
                for (int r = 0; r < 3; r++) {
                    dtmfpacket.setSequenceNumber(sequenceNum++);
                    dtmfpacket.setTimestamp(transcoder.getOutgoingEncodedFrameSize() );
                    doRtpDelay();
                    rtpSocketSend(dtmfpacket);
                }

                // send 200 ms of blank packets
                for (int r = 0; r < 200 / transcoder.getOutgoingPacketization(); r++) {
                    blankpacket.setSequenceNumber(sequenceNum++);
                    blankpacket.setTimestamp(transcoder.getOutgoingEncodedFrameSize());
                    doRtpDelay();
                    rtpSocketSend( blankpacket );
                }
            }
            catch (Exception e) {
                log.warn("queueSipDtmfDigits", e.getLocalizedMessage());
            }
        }
    }

    public void send(byte[] asaoBuffer, int offset, int num) {
    	transcoder.transcode(asaoBuffer, offset, num, packetBuffer, RTP_HEADER_SIZE, this);
    }
    
    public void sendTranscodedData() {
        rtpPacket.setSequenceNumber( sequenceNum++ );
        rtpPacket.setTimestamp( timestamp );
        rtpPacket.setPayloadLength( transcoder.getOutgoingEncodedFrameSize() );
        rtpSocketSend( rtpPacket );    	
    }
    
    public void halt() {
        DatagramSocket socket = rtpSocket.getDatagramSocket();
        rtpSocket.close();
        if (socketIsLocal && socket != null) {
            socket.close();
        }
        rtpSocket = null;
        log.debug(" Stopping Rtp sender." );
    }

    private void doRtpDelay() {
        try {
            Thread.sleep(transcoder.getOutgoingPacketization() - 2);
        }
        catch ( Exception e ) {
        }
    }

    private synchronized void rtpSocketSend(RtpPacket rtpPacket) {
        try {
         	rtpSocket.send( rtpPacket );
            timestamp += rtpPacket.getPayloadLength();
        }
        catch ( Exception e ) {
        }
    }
}
