/*
 * BigBlueButton - http://www.bigbluebutton.org
 * 
 * Copyright (c) 2008-2009 by respective authors (see below). All rights reserved.
 * 
 * BigBlueButton is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Affero General Public License as published by the Free Software 
 * Foundation; either version 3 of the License, or (at your option) any later 
 * version. 
 * 
 * BigBlueButton is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along 
 * with BigBlueButton; if not, If not, see <http://www.gnu.org/licenses/>.
 *
 * Author: Richard Alam <ritzalam@gmail.com>
 *
 * $Id: $x
 */
package org.bigbluebutton.deskshare.server.socket;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.bigbluebutton.deskshare.common.Dimension;
import org.bigbluebutton.deskshare.server.CaptureStartEvent;
import org.bigbluebutton.deskshare.server.CaptureUpdateEvent;
import org.bigbluebutton.deskshare.server.events.CaptureEndBlockEvent;
import org.bigbluebutton.deskshare.server.events.CaptureStartBlockEvent;
import org.bigbluebutton.deskshare.server.events.CaptureUpdateBlockEvent;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;

public class BlockStreamProtocolDecoder extends CumulativeProtocolDecoder {
	final private Logger log = Red5LoggerFactory.getLogger(BlockStreamProtocolDecoder.class, "deskshare");
	
	private static final String ROOM = "ROOM";
	
    private static final byte[] HEADER = new byte[] {'B', 'B', 'B', '-', 'D', 'S'};
    private static final byte CAPTURE_START_EVENT = 0;
    private static final byte CAPTURE_UPDATE_EVENT = 1;
    private static final byte CAPTURE_END_EVENT = 2;
        
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
    		
    	decodeMessage(session, in, out);
    	
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
	    	default:
    			log.error("Unknown event: " + event);
    	}
    }
    
    private void decodeCaptureEndEvent(IoSession session, IoBuffer in, ProtocolDecoderOutput out) {
    	String room = decodeRoom(session, in);
    	
    	if (! room.isEmpty()) {
    		CaptureEndBlockEvent event = new CaptureEndBlockEvent(room);
    		out.write(event);
    	} else {
    		System.out.println("Room is empty.");
    	}
    }
    
    private void decodeCaptureStartEvent(IoSession session, IoBuffer in, ProtocolDecoderOutput out) { 
    	String room = decodeRoom(session, in);

		Dimension blockDim = decodeDimension(in);
		Dimension screenDim = decodeDimension(in);    	
	    System.out.println("Block dim [" + blockDim.getWidth() + "," + blockDim.getHeight() + "]");
	    System.out.println("Screen dim [" + screenDim.getWidth() + "," + screenDim.getHeight() + "]");
	    
	    CaptureStartBlockEvent event = new CaptureStartBlockEvent(room, 
	    									screenDim, blockDim);	
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
    		session.setAttribute(ROOM, room);
		} catch (CharacterCodingException e) {
			e.printStackTrace();
		}   
		
		return room;
    }
    
    private void decodeCaptureUpdateEvent(IoSession session, IoBuffer in, ProtocolDecoderOutput out) {
    	String room = decodeRoom(session, in);
    	int position = in.getShort();
    	boolean isKeyFrame = (in.get() == 1) ? true : false;
    	int length = in.getInt();
    	byte[] data = new byte[length];
    	in.get(data, 0, length);
    	
//    	System.out.println("position=[" + position + "] keyframe=" + isKeyFrame + " length= " + length);
    	
    	CaptureUpdateBlockEvent event = new CaptureUpdateBlockEvent(room, position, data, isKeyFrame);
    	out.write(event);
    }
}
