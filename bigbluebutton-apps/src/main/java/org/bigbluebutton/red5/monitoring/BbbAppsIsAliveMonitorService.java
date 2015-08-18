package org.bigbluebutton.red5.monitoring;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.bigbluebutton.common.messages.MessageHeader;
import org.bigbluebutton.common.messages.MessagingConstants;
import org.bigbluebutton.common.messages.PubSubPingMessage;
import org.bigbluebutton.common.messages.payload.PubSubPingMessagePayload;
import org.bigbluebutton.red5.client.messaging.ConnectionInvokerService;
import org.bigbluebutton.red5.client.messaging.DisconnectAllMessage;
import org.bigbluebutton.red5.pubsub.redis.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class BbbAppsIsAliveMonitorService {
	private static Logger log = LoggerFactory.getLogger(BbbAppsIsAliveMonitorService.class);
	
	private static final Executor msgSenderExec = Executors.newFixedThreadPool(1);
	private static final Executor runExec = Executors.newFixedThreadPool(1);
	
	private ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);
	
	private BlockingQueue<IKeepAliveMessage> messages = new LinkedBlockingQueue<IKeepAliveMessage>();
	private volatile boolean processMessages = false;
	private KeepAliveTask task = new KeepAliveTask();
	
	private ConnectionInvokerService service;
	private Long lastKeepAliveMessage = 0L;
	
	private MessageSender sender;
	
	public void setMessageSender(MessageSender sender) {
		this.sender = sender;
	}
	
	public void setConnectionInvokerService(ConnectionInvokerService s) {
		this.service = s;
	}
	
	public void start() {	
		scheduledThreadPool.scheduleWithFixedDelay(task, 5000, 10000, TimeUnit.MILLISECONDS);
		processKeepAliveMessage();
	}
	
	public void stop() {
		processMessages = false;
		scheduledThreadPool.shutdownNow();
	}
	
	public void handleKeepAliveMessage(Long startedOn, Long timestamp) {
		queueMessage(new KeepAliveMessage(startedOn, timestamp));
	}
	
	private void queueMessage(IKeepAliveMessage msg) {
		messages.add(msg);
	}
    
  private void processKeepAliveMessage() {
  	processMessages = true;
  	Runnable sender = new Runnable() {
  		public void run() {
  			while (processMessages) {
  				IKeepAliveMessage message;
  				try {
  					message = messages.take();
  					processMessage(message);	
  				} catch (InterruptedException e) {
  					// TODO Auto-generated catch block
  					e.printStackTrace();
  				}	catch (Exception e) {
  					//log.error("Catching exception [{}]", e.toString());
  				}
  			}
  		}
  	};
  	msgSenderExec.execute(sender);		
  } 
  	
  private void processMessage(final IKeepAliveMessage msg) {
  	Runnable task = new Runnable() {
  		public void run() {
	  	  	if (msg instanceof KeepAliveMessage) {
	  	  		processKeepAliveMessage((KeepAliveMessage) msg);
	  	  	} else if (msg instanceof CheckIsAliveTimer) {
	  	  		processCheckIsAliveTimer((CheckIsAliveTimer) msg);
	  	  	}  			
  		}
  	};
  	
    runExec.execute(task);
  }
  
  private void processKeepAliveMessage(KeepAliveMessage msg) {
	  lastKeepAliveMessage = msg.timestamp;
  }
  
  private void processCheckIsAliveTimer(CheckIsAliveTimer msg) {
	  if (lastKeepAliveMessage != 0 && (System.currentTimeMillis() - lastKeepAliveMessage > 10000)) {
		  log.warn("BBB Apps is down. Disconnecting all clients.");
		  service.sendMessage(new DisconnectAllMessage());
	  }
  }
  
	class KeepAliveTask implements Runnable {
	    public void run() {
	     	CheckIsAliveTimer ping = new CheckIsAliveTimer();
	     	queueMessage(ping);
	     	
	     	PubSubPingMessage msg = new PubSubPingMessage();
	     	MessageHeader header = new MessageHeader();
	     	header.name = PubSubPingMessage.PUBSUB_PING;
	     	header.timestamp = System.nanoTime();
	     	header.replyTo = "BbbRed5";
	     	header.version = "0.0.1";
	     	PubSubPingMessagePayload payload = new PubSubPingMessagePayload();
	     	payload.system = "BbbAppsRed5";
	     	payload.timestamp = System.currentTimeMillis();
	     	msg.header = header;
	     	msg.payload = payload;
	     	Gson gson = new Gson();
	     	sender.send(MessagingConstants.TO_SYSTEM_CHANNEL, gson.toJson(msg));
	    }
	  }
}
