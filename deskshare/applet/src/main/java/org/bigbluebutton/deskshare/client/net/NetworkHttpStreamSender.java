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
package org.bigbluebutton.deskshare.client.net;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.bigbluebutton.deskshare.common.CaptureEvents;
import org.bigbluebutton.deskshare.common.Dimension;

import com.myjavatools.web.ClientHttpRequest;

public class NetworkHttpStreamSender implements Runnable {
	private String host = "localhost";
	private static final String SCREEN_CAPTURE__URL = "/deskshare/tunnel/screenCapture";
	private URL url;
	URLConnection conn;
	private String room;
	private Dimension screenDim;
	private Dimension blockDim;
	private final NextBlockRetriever retriever;
	private volatile boolean processBlocks = false;

	
	public NetworkHttpStreamSender(NextBlockRetriever retriever, String room, Dimension screenDim, Dimension blockDim) {
		this.retriever = retriever;
		this.room = room;
		this.screenDim = screenDim;
		this.blockDim = blockDim;
	}
	
	public void connect(String host) throws ConnectionException {
		this.host = host;
		System.out.println("Starting NetworkHttpStreamSender to " + host);
		openConnection();
	}

	private void openConnection() throws ConnectionException {
		/**
		 * Need to re-establish connection each time, otherwise, 
		 * we get java.net.ProtocolException: Cannot write output after reading input.
		 * 
		 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4382944
		 * 
		 */				
		try {			
			url = new URL("http://" + host + SCREEN_CAPTURE__URL);
			conn = url.openConnection();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new ConnectionException("MalformedURLException " + url.toString());
		} catch (IOException e) {
			e.printStackTrace();
			throw new ConnectionException("IOException while connecting to " + url.toString());
		}
	}
	
	public void sendStartStreamMessage() {
		try {
			openConnection();
			sendCaptureStartEvent(screenDim, blockDim);
		} catch (ConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sendCaptureStartEvent(Dimension screen, Dimension block) throws ConnectionException {
		ClientHttpRequest chr;
		try {
			chr = new ClientHttpRequest(conn);
			chr.setParameter("room", room);
			
			String screenInfo = Integer.toString(screen.getWidth())
								+ "x" + Integer.toString(screen.getHeight());

			chr.setParameter("screenInfo", screenInfo);
			
			String blockInfo = Integer.toString(block.getWidth())
								+ "x" + Integer.toString(block.getHeight());

			chr.setParameter("blockInfo", blockInfo);

			chr.setParameter("event", CaptureEvents.CAPTURE_START.getEvent());
			chr.post();
		} catch (IOException e) {
			e.printStackTrace();
			throw new ConnectionException("IOException while sending capture start event.");
		}

	}
	
	public void disconnect() throws ConnectionException {
		try {
			openConnection();
			sendCaptureEndEvent();
		} catch (ConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} finally {
			processBlocks = false;
		}
	}

	private void sendCaptureEndEvent() throws ConnectionException {
		ClientHttpRequest chr;
		try {
			chr = new ClientHttpRequest(conn);
			chr.setParameter("room", room);
			
			chr.setParameter("event", CaptureEvents.CAPTURE_END.getEvent());
			chr.post();
		} catch (IOException e) {
			e.printStackTrace();
			throw new ConnectionException("IOException while sending capture end event.");
		}
	}
	
	public void run() {
		processBlocks = true;
		
		while (processBlocks) {
			EncodedBlockData block;
			try {
				block = retriever.fetchNextBlockToSend();
				BlockVideoData	bv = new BlockVideoData(room, block.getPosition(), block.getVideoData(), false);	
				sendBlockData(bv);	
			} catch (InterruptedException e) {
				System.out.println("ERROR: Interrupted exception while proccessing block for sending.");
			}								
		}
	}
	
	private void sendBlockData(BlockVideoData blockData) {
	    ClientHttpRequest chr;
		try {
			openConnection();
			chr = new ClientHttpRequest(conn);
		    chr.setParameter("room", blockData.getRoom());
		    chr.setParameter("position", blockData.getPosition());
		    chr.setParameter("keyframe", blockData.isKeyFrame());
		    chr.setParameter("event", CaptureEvents.CAPTURE_UPDATE.getEvent());
			ByteArrayInputStream block = new ByteArrayInputStream(blockData.getVideoData());				
			chr.setParameter("blockdata", "block", block);
			chr.post();		
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ConnectionException e) {
			System.out.println("ERROR: Failed to send block data.");
		}
	}		
}
