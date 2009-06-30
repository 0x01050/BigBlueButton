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
package org.bigbluebutton.deskshare;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.red5.logging.Red5LoggerFactory;
import org.red5.server.api.IScope;
import org.red5.server.api.so.ISharedObject;
import org.slf4j.Logger;

/**
 * The ClientProxy receives images from the client which captures the screen
 * @author Snap
 *
 */
public class ClientProxy implements Runnable, IImageListener {
	final private Logger log = Red5LoggerFactory.getLogger(ClientProxy.class, "deskshare");
	
	private ServerSocket serverSocket;
	private boolean keepCapturing = true;
	
	private ArrayList<RoomThread> roomList;
	
	private IScope scope;
	private Application application;
	
	/**
	 * The default constructor
	 */
	public ClientProxy(Application app){
		roomList = new ArrayList<RoomThread>();
		this.application = app;
		this.scope = app.getAppScope();
		try{
			serverSocket = new ServerSocket(DeskShareConstants.PORT);
		} catch(IOException e){
			log.error(e.getMessage());
			e.printStackTrace(System.out);
		}
	}
	
	/**
	 * The run method for this thread. Should not be called directly
	 */
	public void run(){
		while(keepCapturing){
			try{
				acceptRoomConnection(serverSocket.accept());
			} catch(IOException e){
				log.error(e.getMessage());
				e.printStackTrace(System.out);
			}
		}
	}

	/**
	 * Stops this application from receiving images from the client
	 */
	public void stopCapture(){
		keepCapturing = false;
	}
	
	private void acceptRoomConnection(Socket socket){
		try{
			//Get the name of the room the client is trying to publish a stream to
			BufferedReader inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String roomNum = inStream.readLine();
			
			//Get the screen dimensions from the client, i.e. the resolution of the video we need to create
			String[] screenDimensions = inStream.readLine().split("x");
			int width = Integer.parseInt(screenDimensions[0]);
			int height = Integer.parseInt(screenDimensions[1]);
			int frameRate = Integer.parseInt(screenDimensions[2]);
			
			//Create a new room thread and a new streamer object to go with the thread
			RoomThread room = new RoomThread(roomNum, socket, width, height);
			IScope roomSpecificScope = scope.getScope(roomNum);
			Red5Streamer streamPublisher = new Red5Streamer(roomSpecificScope, roomNum, width, height, frameRate);
			room.registerListener(streamPublisher);
			room.registerListener(this);
			
			//Add the room to our list of rooms and start the room thread
			roomList.add(room);
			Thread thread = new Thread(room);
			thread.start();
			
			//notify the clients in the room that the stream has now started broadcasting.
			ISharedObject deskSO = application.getSharedObject(roomSpecificScope, "deskSO");
			deskSO.sendMessage("appletStarted" , new ArrayList<Object>());

		} catch(IOException e){
			e.printStackTrace(System.out);
		}
		
	}
	
	public boolean isStreaming(String room){
		for (int i=0; i< roomList.size(); i++){
			if (roomList.get(i).getStreamName().equalsIgnoreCase(room)) return true;
		}
		return false;
	}

	public void imageReceived(BufferedImage image) {
		// TODO Auto-generated method stub
		
	}

	public void streamEnded(String streamName) {
		for (int i = 0; i<roomList.size(); i++){
			if (roomList.get(i).getStreamName().equalsIgnoreCase(streamName)){
				log.info("Removing stream " + streamName);
				roomList.remove(i);
			}
		}
	}
	
	public int getRoomVideoWidth(String room){
		for (int i=0; i < roomList.size(); i++){
			RoomThread rm = roomList.get(i);
			if (rm.getStreamName().equalsIgnoreCase(room)) return rm.getScreenWidth();
		}
		return 0;
	}
	
	public int getRoomVideoHeight(String room){
		for (int i=0; i < roomList.size(); i++){
			RoomThread rm = roomList.get(i);
			if (rm.getStreamName().equalsIgnoreCase(room)) return rm.getScreenHeight();
		}
		return 0;
	}
	
	/**
	 * Closes the server socket which listens to new connections
	 */
	public void closeSockets(){
		try{
			this.serverSocket.close();
		} catch(IOException e){
			e.printStackTrace(System.out);
		}
	}
	
}
