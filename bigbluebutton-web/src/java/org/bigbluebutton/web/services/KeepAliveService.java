/**
* BigBlueButton open source conferencing system - http://www.bigbluebutton.org/
* 
* Copyright (c) 2012 BigBlueButton Inc. and by respective authors (see below).
*
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License as published by the Free Software
* Foundation; either version 3.0 of the License, or (at your option) any later
* version.
* 
* BigBlueButton is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
* PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License along
* with BigBlueButton; if not, see <http://www.gnu.org/licenses/>.
*
*/

package org.bigbluebutton.web.services;

import org.bigbluebutton.api.messaging.MessagingService;
import org.bigbluebutton.api.messaging.MessagingConstants;
import org.bigbluebutton.api.messaging.RedisMessagingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import com.google.gson.Gson;

public class KeepAliveService {
	private static Logger log = LoggerFactory.getLogger(KeepAliveService.class);
	private final String KEEP_ALIVE_REQUEST = "KEEP_ALIVE_REQUEST";
	private MessagingService service;
	private Timer cleanupTimer;
	private long runEvery = 10000;
	private int maxLives = 5;
	private KeepAliveTask task = null;
	private volatile boolean processMessages = false;
	private ArrayList<String> pingMessages;
	volatile boolean available = true;
	
	private static final int SENDERTHREADS = 1;
	private static final Executor msgSenderExec = Executors.newFixedThreadPool(SENDERTHREADS);
	
	private BlockingQueue<KeepAliveMessage> messages = new LinkedBlockingQueue<KeepAliveMessage>();
	
	public void start() {
		cleanupTimer = new Timer("keep-alive-task", true);
		task = new KeepAliveTask();
		pingMessages = new ArrayList<String>();
		cleanupTimer.scheduleAtFixedRate(task, 5000, runEvery);
		processKeepAliveMessage();
	}
	
	public void stop() {
		processMessages = false;
		cleanupTimer.cancel();	
	}
	
	public void setRunEvery(long v) {
		runEvery = v * 1000;
	}

	public void setMessagingService(MessagingService service){
		this.service = service;
	}
	
	class KeepAliveTask extends TimerTask {
    public void run() {
     	String aliveId = Long.toString(System.currentTimeMillis());
     	KeepAlivePing ping = new KeepAlivePing(aliveId);
     	queueMessage(ping);
    }
  }

  public void keepAliveReply(String aliveId) {
   	log.debug("Received keep alive msg reply from bbb-apps. id [{}]", aliveId);
   	KeepAlivePong pong = new KeepAlivePong(aliveId);
   	queueMessage(pong);
  }

  public boolean isDown(){
  	return !available;
  }
    
  private void queueMessage(KeepAliveMessage msg) {
   	try {
		  messages.offer(msg, 5, TimeUnit.SECONDS);
	  } catch (InterruptedException e) {
		  // TODO Auto-generated catch block
		  e.printStackTrace();
	  }    	
  }
    
  private void processKeepAliveMessage() {
  	processMessages = true;
  	Runnable sender = new Runnable() {
  		public void run() {
  			while (processMessages) {
  				KeepAliveMessage message;
  				try {
  					message = messages.take();
  					processMessage(message);	
  				} catch (InterruptedException e) {
  					// TODO Auto-generated catch block
  					e.printStackTrace();
  				}								
  			}
  		}
  	};
  	msgSenderExec.execute(sender);		
  } 
  	
  private void processMessage(KeepAliveMessage msg) {
  	if (msg instanceof KeepAlivePing) {
  		processPing((KeepAlivePing) msg);
  	} else if (msg instanceof KeepAlivePong) {
  		processPong((KeepAlivePong) msg);
  	}
  }
  	
  private void processPing(KeepAlivePing msg) {
   	if (pingMessages.size() < maxLives) {
     	HashMap<String,String> map = new HashMap<String,String>();
     	map.put("messageId", KEEP_ALIVE_REQUEST);
     	map.put("aliveId", msg.getId());
     	Gson gson = new Gson();

     	pingMessages.add(msg.getId());
     	log.debug("Sending keep alive message to bbb-apps. keep-alive id [{}]", msg.getId());
     	service.send(MessagingConstants.SYSTEM_CHANNEL, gson.toJson(map));
   	} else {
   		// BBB-Apps has gone down. Mark it as unavailable and clear
   		// pending ping messages. This allows us to continue to send ping messages
   		// in case BBB-Apps comes back up. (ralam - april 29, 2014)
   		available = false;
   		pingMessages.clear();
   		log.warn("bbb-apps is down!");
   	}  		
  }
  	
  private void processPong(KeepAlivePong msg) {
   	int count = 0;
   	boolean found = false;

   	while (count < pingMessages.size() || !found){
   		if (pingMessages.get(count).equals(msg.getId())){
   			pingMessages.remove(count);
   			if (!available) {
   				available = true;
   				pingMessages.clear();
   			  log.info("Received Keep Alive Reply. BBB-Apps has recovered.");
   			}
   			found = true;
   		}
   		count++;
   	}
   	if (!found){
   		log.info("Received invalid keep alive response from bbb-apps:" + msg.getId());
   	}  		
  }
}