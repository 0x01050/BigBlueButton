package org.bigbluebutton.deskshare.client;

import java.awt.image.BufferedImage;

import org.bigbluebutton.deskshare.client.blocks.BlockManager;
import org.bigbluebutton.deskshare.client.blocks.ChangedBlocksListener;
import org.bigbluebutton.deskshare.client.net.NetworkStreamSender;
import org.bigbluebutton.deskshare.common.Dimension;
import org.bigbluebutton.deskshare.client.net.ConnectionException;

class DeskshareClient implements IScreenCaptureListener, ChangedBlocksListener {
	private static final String LICENSE_HEADER = "This program is free software: you can redistribute it and/or modify\n" +
	"it under the terms of the GNU AFFERO General Public License as published by\n" +
	"the Free Software Foundation, either version 3 of the License, or\n" +
	"(at your option) any later version.\n\n" +
	"This program is distributed in the hope that it will be useful,\n" +
	"but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
	"MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" +
	"GNU General Public License for more details.\n\n" +
	"You should have received a copy of the GNU AFFERO General Public License\n" +
	"along with this program.  If not, see <http://www.gnu.org/licenses/>.\n\n" +
	"To download the source of this program, see. \n" +
	"http://code.google.com/p/bigbluebutton/wiki/InstallingDesktopSharing\n\n" +
	"Copyright 2010 Blindside Networks. All Rights Reserved.\n\n";
	
	private ScreenCaptureTaker captureTaker;
	private ScreenCapture capture;
	private Thread captureTakerThread;
	private BlockManager blockManager;
	private int blockWidth = 64;
	private int blockHeight = 64;	
	boolean connected = false;
	private boolean started = false;
	private NetworkStreamSender sender;
	
   	private String host;
   	private int port;
   	private String room;
   	private int width;
   	private int height;
	private int x;
	private int y;
	private boolean httpTunnel;
	
	public void start() {	
		System.out.println(LICENSE_HEADER);
		System.out.println("Desktop Sharing v0.64");
		System.out.println("Start");
		System.out.println("Connecting to " + host + ":" + port + " room " + room);
		System.out.println("Sharing " + width + "x" + height + " at " + x + "," + y);
		System.out.println("Http Tunnel: " + httpTunnel);
		
		startCapture();		
		started = true;
	}

	public void startCapture() {
		capture = new ScreenCapture(x, y, width, height);
		captureTaker = new ScreenCaptureTaker(capture);
		
		Dimension screenDim = new Dimension(width, height);
		Dimension tileDim = new Dimension(blockWidth, blockHeight);
		blockManager = new BlockManager();
		blockManager.addListener(this);
		blockManager.initialize(screenDim, tileDim);
	
		sender = new NetworkStreamSender(blockManager, host, room, screenDim, tileDim);
		connected = sender.connect();
		if (connected) {
			captureTaker.addListener(this);
			captureTaker.setCapture(true);
			
			captureTakerThread = new Thread(captureTaker, "ScreenCaptureTaker");
			captureTakerThread.start();	
			sender.start();
		}		
	}
		
	/**
	 * This method is called when the user closes the browser window containing the applet
	 * It is very important that the connection to the server is closed at this point. That way the server knows to
	 * close the stream.
	 */
	public void destroy() {
		System.out.println("Destroy");
		stop();
	}

	public void stop() {
		System.out.println("Stop");
		captureTaker.setCapture(false);
		if (connected && started) {
			try {
				sender.stop();
				started = false;
				connected = false;
			} catch (ConnectionException e) {
				e.printStackTrace();
			}
		}			
	}
	
	public void setScreenCoordinates(int x, int y) {
		capture.setX(x);
		capture.setY(y);
	}
	
	public void onScreenCaptured(BufferedImage screen) {
		blockManager.processCapturedScreen(screen);		
	}
	
	
	public void screenCaptureStopped() {
		System.out.println("Screencapture stopped");
		destroy();
	}

	public void onChangedBlock(Integer blockPosition) {
		sender.send(blockPosition);
	}



	private DeskshareClient(Builder builder) {
       	room = builder.room;
       	host = builder.host;
       	port = builder.port;
       	width = builder.width;  
       	height = builder.height;
       	x = builder.x;
       	y = builder.y;
       	httpTunnel = builder.httpTunnel;
    }

	
	/********************************************
	 * Helper class
	 ********************************************/
	
    public static class Builder {
       	private String host;
       	private int port;
       	private String room;
       	private int width;
       	private int height;
    	private int x;
    	private int y;
    	private boolean httpTunnel;
    	
    	public Builder() {}
    	
    	public Builder host(String host) {
    		this.host = host;
    		return this;
    	}
    	
    	public Builder port(int port) {  		
	    	this.port = port;
	    	return this;
	    }
    	
    	public Builder room(String room) {
    		this.room = room;
    		return this;
    	}
    	
    	public Builder width(int width) {
    		this.width = width;
    		return this;
    	}

    	public Builder height(int height) {
    		this.height = height;
    		return this;
    	}
    	
    	public Builder x(int x) {
    		this.x = x;
    		return this;
    	}
    	
    	public Builder y(int y) {
    		this.y = y;
    		return this;
    	}
    	
    	public Builder httpTunnel(boolean httpTunnel) {
    		this.httpTunnel = httpTunnel;
    		return this;
    	}
    	
    	public DeskshareClient build() {
    		return new DeskshareClient(this);
    	}
    }

}
