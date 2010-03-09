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
package org.bigbluebutton.deskshare.server.servlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bigbluebutton.deskshare.common.Dimension;
import org.bigbluebutton.deskshare.server.session.ISessionManagerGateway;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

public class HttpTunnelStreamController extends MultiActionController {

	private boolean hasSessionManager = false;
	private ISessionManagerGateway sessionManager;
	
	public ModelAndView screenCaptureHandler(HttpServletRequest request, HttpServletResponse response) throws Exception {		
		System.out.println("**** In deskshare tunneling ******");
		String event = request.getParameterValues("event")[0];	
		int captureRequest = Integer.parseInt(event);

		if (0 == captureRequest) {
			handleCaptureStartRequest(request, response);
		} else if (1 == captureRequest) {
			handleCaptureUpdateRequest(request, response);
		} else if (2 == captureRequest) {
			handleCaptureEndRequest(request, response);
		} else {
			System.out.println("****Cannot handle screen capture event " + captureRequest);
		}
		return null;
	}	
	
	private void handleCaptureStartRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {		
		System.out.println("**** In handleCaptureStartRequest deskshare tunneling ******");
		String room = request.getParameterValues("room")[0];
		String screenInfo = request.getParameterValues("screenInfo")[0];
		String blockInfo = request.getParameterValues("blockInfo")[0];
		
		String[] screen = screenInfo.split("x");
		String[] block = blockInfo.split("x");
		
		Dimension screenDim, blockDim;
		screenDim = new Dimension(Integer.parseInt(screen[0]), Integer.parseInt(screen[1]));
		blockDim = new Dimension(Integer.parseInt(block[0]), Integer.parseInt(block[1]));
		
		if (! hasSessionManager) {
			sessionManager = getSessionManager();
			hasSessionManager = true;
		}
		System.out.println("Http received. Creating session ");
		sessionManager.createSession(room, screenDim, blockDim);		
	}	
	
	private void handleCaptureUpdateRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		System.out.println("**** In handleCaptureUpdateRequest deskshare tunneling ******");
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		// MultipartFile is a copy of file in memory, not in file system
		MultipartFile multipartFile = multipartRequest.getFile("blockdata");
	
		long startRx = System.currentTimeMillis();
		
		byte[] blockData = multipartFile.getBytes();
		String room = request.getParameterValues("room")[0];
		String keyframe = request.getParameterValues("keyframe")[0];
		String position = request.getParameterValues("position")[0];
				
		if (! hasSessionManager) {
			sessionManager = getSessionManager();
			hasSessionManager = true;
		}
			
		sessionManager.updateBlock(room, Integer.valueOf(position), blockData, Boolean.parseBoolean(keyframe));
			
		long completeRx = System.currentTimeMillis();
		System.out.println("Http receive and send took " + (completeRx - startRx) + "ms.");
	}
	
	private void handleCaptureEndRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {	
		System.out.println("**** In handleCaptureEndRequest deskshare tunneling ******");
		String room = request.getParameterValues("room")[0];
		if (! hasSessionManager) {
			sessionManager = getSessionManager();
			hasSessionManager = true;
		}
		System.out.println("HttpTunnel: Received Capture Enfd Event.");
    	sessionManager.removeSession(room);
	}
	    
	private ISessionManagerGateway getSessionManager() {
		//Get the servlet context
		ServletContext ctx = getServletContext();
		System.out.println("****Got the servlet context:}****");
		
		//Grab a reference to the application context
		ApplicationContext appCtx = (ApplicationContext) ctx.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		System.out.println("**** Got the application context: {} ****");
		
		//Get the bean holding the parameter
		ISessionManagerGateway manager = (ISessionManagerGateway) appCtx.getBean("sessionManagerGateway");
		if (manager != null) {
			System.out.println("****Got the SessionManager context: *****");
		}
		return manager;
	}
}
