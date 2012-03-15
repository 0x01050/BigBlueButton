/** 
* ===License Header===
*
* BigBlueButton open source conferencing system - http://www.bigbluebutton.org/
*
* Copyright (c) 2010 BigBlueButton Inc. and by respective authors (see below).
*
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License as published by the Free Software
* Foundation; either version 2.1 of the License, or (at your option) any later
* version.
*
* BigBlueButton is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
* PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License along
* with BigBlueButton; if not, see <http://www.gnu.org/licenses/>.
* 
* ===License Header===
*/
package org.bigbluebutton.deskshare.server.socket;

import java.awt.Point;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.bigbluebutton.deskshare.common.Dimension;
import org.bigbluebutton.deskshare.server.events.CaptureEndBlockEvent;
import org.bigbluebutton.deskshare.server.events.CaptureStartBlockEvent;
import org.bigbluebutton.deskshare.server.events.CaptureUpdateBlockEvent;
import org.bigbluebutton.deskshare.server.events.MouseLocationEvent;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;

public class BlockStreamProtocolDecoder extends CumulativeProtocolDecoder {
	final private Logger log = Red5LoggerFactory.getLogger(BlockStreamProtocolDecoder.class, "deskshare");
	
	private static final String ROOM = "ROOM";
	
    private static final byte[] HEADER = new byte[] {'B', 'B', 'B', '-', 'D', 'S'};
    private static final byte CAPTURE_START_EVENT = 0;
    private static final byte CAPTURE_UPDATE_EVENT = 1;
    private static final byte CAPTURE_END_EVENT = 2;
    private static final byte MOUSE_LOCATION_EVENT = 3;
        
    protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
     	
    	// Let's work with a buffer that contains header and the message length,
    	// ten (10) should be enough since header (6-bytes) plus length (4-bytes)
    	if (in.remaining() < 10) return false;
    		
    	byte[] header = new byte[HEADER.length];    
    	
    	int start = in.position();
    	
    	in.get(header, 0, HEADER.length);    	
    	
    	
    	int messageLength = in.getInt();    	
//    	System.out.println("Message Length " + messageLength);
    	if (in.remaining() < messageLength) {
    		in.position(start);
    		return false;
    	}
    	
    	Integer numMsg = (Integer)session.getAttribute("NUM_MSG", 0);
    	numMsg++;
    	session.setAttribute("NUM_MSG", numMsg);
    //	System.out.println("**** Num msgs [" + numMsg + "]");
    //	if (numMsg % 10 == 0)
    //		log.debug("**** Num msgs [{}]", numMsg);
 /*   	if (numMsg >= 50) {
    		System.out.println("**** Closing connection. Corrupt packet.");
    		if (numMsg == 50) {
    			int seqNum = 0;
    			String room = (String)session.getAttribute(ROOM, null);
    			if (room != null) {
    				System.out.println("Closing room " + room);
    				CaptureEndBlockEvent event = new CaptureEndBlockEvent(room, seqNum);
    				out.write(event);
    			} else {
    				System.out.println("Cannot determine room for session.");
    			}
        		CloseFuture future = session.close(true);   			
    		}
    	} else {
    		System.out.println("Decoding");
*/    		decodeMessage(session, in, out);
//    	}
    	
    	return true;
    }
    
    private void decodeMessage(IoSession session, IoBuffer in, ProtocolDecoderOutput out) {
    	byte event = in.get();
    	switch (event) {
	    	case CAPTURE_START_EVENT:
	    		System.out.println("Decoding CAPTURE_START_EVENT");
	    		decodeCaptureStartEvent(session, in, out);
	    		break;
	    	case CAPTURE_UPDATE_EVENT:
//	    		System.out.println("Decoding CAPTURE_UPDATE_EVENT");
	    		decodeCaptureUpdateEvent(session, in, out);
	    		break;
	    	case CAPTURE_END_EVENT:
	    		log.warn("Got CAPTURE_END_EVENT event: " + event);
	    		System.out.println("Got CAPTURE_END_EVENT event: " + event);
	    		decodeCaptureEndEvent(session, in, out);
	    		break;
	    	case MOUSE_LOCATION_EVENT:
	    		decodeMouseLocationEvent(session, in, out);
	    		break;
	    	default:
    			log.error("Unknown event: " + event);
    			throwAwayCorruptedPacket(in);
    	    	Integer numErrors = (Integer)session.getAttribute("NUM_ERRORS", 0);    	    	
    	    	session.setAttribute("NUM_ERRORS", numErrors++);
    	    	
    	    	if (numErrors > 50) {
    	    		log.info("Closing session. Too many corrupt packets.");
    	    		int seqNum = 0;
    	    		String room = (String)session.getAttribute(ROOM, null);
    	    		if (room != null) {
    	    			log.info("Closing session [" + room + "]. Too many corrupt packets.");
    	    			CaptureEndBlockEvent ceb = new CaptureEndBlockEvent(room, seqNum);
    	    			out.write(ceb);
    	    		} else {
    	    			log.info("Cannot determine session. Too many corrupt packets.");
    	    		}
    	        	CloseFuture future = session.close(true);   			
    	    	} 
    	    	break;
    	    	
    	}
    }
    
    private static final byte CR = 13;
    private static final byte LF = 10;
    
    private void throwAwayCorruptedPacket(IoBuffer in) {
    	// Remember the initial position.
        int start = in.position();

        // Now find the first CRLF in the buffer.
        byte previous = 0;
        while (in.hasRemaining()) {
            byte current = in.get();

            if (previous == CR && current == LF) {
                // Remember the current position and limit.
                int position = in.position();
                int limit = in.limit();
                try {
                    in.position(start);
                    in.limit(position);
                    // The bytes between in.position() and in.limit()
                    // now contain a full CRLF terminated byte stream.
                    log.info("Throwing corrupted packet.");
                    in.slice();
                } finally {
                    // Set the position to point right after the
                    // detected CRLF and set the limit to the old
                    // one.
                    in.position(position);
                    in.limit(limit);
                }
                return;
            }

            previous = current;
        }

        // Could not find CRLF in the buffer. Reset the initial
        // position to the one we recorded above.
        in.position(start);
    }
    
    private void decodeMouseLocationEvent(IoSession session, IoBuffer in, ProtocolDecoderOutput out) {
    	String room = decodeRoom(session, in);
    	int seqNum = in.getInt();
    	int mouseX = in.getInt();
    	int mouseY = in.getInt();
    	
    	/** Swallow CRLF **/
    	byte cr = in.get();
    	byte lf = in.get();
    	
    	MouseLocationEvent event = new MouseLocationEvent(room, new Point(mouseX, mouseY), seqNum);
    	out.write(event);
    }
    
    private void decodeCaptureEndEvent(IoSession session, IoBuffer in, ProtocolDecoderOutput out) {
    	String room = decodeRoom(session, in);
    	
    	if (! "".equals(room)) {
    		log.info("CaptureEndEvent for " + room);
    		int seqNum = in.getInt();
        	
    		/** Swallow CRLF **/
        	byte cr = in.get();
        	byte lf = in.get();
        	
    		CaptureEndBlockEvent event = new CaptureEndBlockEvent(room, seqNum);
    		out.write(event);
    	} else {
    		log.warn("Room is empty.");
    	}
    }
    
    private void decodeCaptureStartEvent(IoSession session, IoBuffer in, ProtocolDecoderOutput out) { 
    	String room = decodeRoom(session, in);
    	session.setAttribute(ROOM, room);
    	int seqNum = in.getInt();
    	
		Dimension blockDim = decodeDimension(in);
		Dimension screenDim = decodeDimension(in);    	
		
		/** Swallow CRLF **/
    	byte cr = in.get();
    	byte lf = in.get();
    			
	    log.info("CaptureStartEvent for " + room);
	    CaptureStartBlockEvent event = new CaptureStartBlockEvent(room, screenDim, blockDim, seqNum);	
	    out.write(event);
    }
    
    private Dimension decodeDimension(IoBuffer in) {
    	int width = in.getInt();
    	int height = in.getInt();
		return new Dimension(width, height);
    }
       
    private String decodeRoom(IoSession session, IoBuffer in) {
    	int roomLength = in.get();
//    	System.out.println("Room length = " + roomLength);
    	String room = "";
    	try {    		
    		room = in.getString(roomLength, Charset.forName( "UTF-8" ).newDecoder());
    		if (session.containsAttribute(ROOM)) {
        		String attRoom = (String) session.getAttribute(ROOM);
        		if (!attRoom.equals(room)) {
        			log.warn(room + " is not the same as room in attribute [" + attRoom + "]");
        		}     			
    		}   		
		} catch (CharacterCodingException e) {
			log.error(e.getMessage());
		}   
		
		return room;
    }
    
    private void decodeCaptureUpdateEvent(IoSession session, IoBuffer in, ProtocolDecoderOutput out) {
    	String room = decodeRoom(session, in);
    	int seqNum = in.getInt();
    	int numBlocks = in.getShort();

    	String blocksStr = "Blocks changed ";
    	
    	for (int i = 0; i < numBlocks; i++) {
        	int position = in.getShort();
        	blocksStr += " " + position;
        	
        	boolean isKeyFrame = (in.get() == 1) ? true : false;
        	int length = in.getInt();
        	byte[] data = new byte[length];
        	in.get(data, 0, length);    	
        	CaptureUpdateBlockEvent event = new CaptureUpdateBlockEvent(room, position, data, isKeyFrame, seqNum);
        	out.write(event);    		
    	}
    	
    	/** Swallow CRLF **/
    	byte cr = in.get();
    	byte lf = in.get();
    	
    }
}
