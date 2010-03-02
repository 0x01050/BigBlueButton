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

import org.bigbluebutton.deskshare.server.session.ISessionManagerGateway;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.bigbluebutton.deskshare.server.events.CaptureEndBlockEvent;
import org.bigbluebutton.deskshare.server.events.CaptureStartBlockEvent;
import org.bigbluebutton.deskshare.server.events.CaptureUpdateBlockEvent;
import org.bigbluebutton.deskshare.server.session.SessionManager;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;

public class BlockStreamEventMessageHandler extends IoHandlerAdapter {
	final private Logger log = Red5LoggerFactory.getLogger(BlockStreamEventMessageHandler.class, "deskshare");
	
//	private SessionManager sessionManager;
	
	private ISessionManagerGateway sessionManager;
	
    @Override
    public void exceptionCaught( IoSession session, Throwable cause ) throws Exception
    {
        log.warn(cause.toString() + " \n " + cause.getMessage());
        cause.printStackTrace();
    }

    @Override
    public void messageReceived( IoSession session, Object message ) throws Exception
    {
    	if (message instanceof CaptureStartBlockEvent) {
    		System.out.println("Got CaptureStartBlockEvent");
    		CaptureStartBlockEvent event = (CaptureStartBlockEvent) message;
    		sessionManager.createSession(event.getRoom(), event.getScreenDimension(), event.getBlockDimension());
    	} else if (message instanceof CaptureUpdateBlockEvent) {
//    		System.out.println("Got CaptureUpdateBlockEvent");
    		CaptureUpdateBlockEvent event = (CaptureUpdateBlockEvent) message;
    		sessionManager.updateBlock(event.getRoom(), event.getPosition(), event.getVideoData(), event.isKeyFrame());
    	} else if (message instanceof CaptureEndBlockEvent) {
    		CaptureEndBlockEvent event = (CaptureEndBlockEvent) message;
    		sessionManager.removeSession(event.getRoom());
    	}
    }

    @Override
    public void sessionIdle( IoSession session, IdleStatus status ) throws Exception
    {
    	log.debug( "IDLE " + session.getIdleCount( status ));
    }
    
    @Override
    public void sessionCreated(IoSession session) throws Exception {
    	log.debug("Session Created");
    }
    
    @Override
    public void sessionOpened(IoSession session) throws Exception {
    	log.debug("Session Opened.");
    }
    
    @Override
    public void sessionClosed(IoSession session) throws Exception {
    	log.debug("Session Closed.");
    	
    	String room = (String) session.getAttribute("ROOM");
    	System.out.println("Session Closed for room " + room);
    	sessionManager.removeSession(room);
    }
    
//    public void setSessionManager(SessionManager sm) {
 //   	sessionManager = sm;
 //   }
    
    public void setSessionManagerGateway(ISessionManagerGateway sm) {
    	sessionManager = sm;
    }
}
