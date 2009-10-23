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
package org.bigbluebutton.deskshare.server;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.red5.logging.Red5LoggerFactory;
import org.red5.server.api.IScope;
import org.red5.server.api.event.IEvent;
import org.red5.server.api.stream.IBroadcastStream;
import org.red5.server.api.stream.IStreamCodecInfo;
import org.red5.server.api.stream.IStreamListener;
import org.red5.server.api.stream.IVideoStreamCodec;
import org.red5.server.api.stream.ResourceExistException;
import org.red5.server.api.stream.ResourceNotFoundException;
import org.red5.server.messaging.IMessageComponent;
import org.red5.server.messaging.IPipe;
import org.red5.server.messaging.IPipeConnectionListener;
import org.red5.server.messaging.IProvider;
import org.red5.server.messaging.OOBControlMessage;
import org.red5.server.messaging.PipeConnectionEvent;
import org.red5.server.net.rtmp.event.IRTMPEvent;
import org.red5.server.net.rtmp.event.VideoData;
import org.bigbluebutton.deskshare.server.ScreenVideo;
import org.red5.server.stream.codec.StreamCodecInfo;
import org.red5.server.stream.message.RTMPMessage;

import org.slf4j.Logger;

import org.red5.server.api.stream.IStreamPacket;;

public class ScreenVideoBroadcastStream implements IBroadcastStream, IProvider, IPipeConnectionListener
{
  /** Listeners to get notified about received packets. */
  private Set<IStreamListener> streamListeners = new CopyOnWriteArraySet<IStreamListener>();
  final private Logger log = Red5LoggerFactory.getLogger(ScreenVideoBroadcastStream.class, "deskshare");

  private String publishedStreamName;
  private IPipe livePipe;
  private IScope mScope;

  // Codec handling stuff for frame dropping
  private StreamCodecInfo streamCodecInfo;
  private Long mCreationTime;
  
  public ScreenVideoBroadcastStream(String name)
  {
    publishedStreamName = name;
    livePipe = null;
    log.trace("name: {}", name);

    // we want to create a video codec when we get our
    // first video packet.
    streamCodecInfo = new StreamCodecInfo();
    mCreationTime = null;
  }

  public IProvider getProvider()
  {
    log.trace("getProvider()");
    return this;
  }

  public String getPublishedName()
  {
    log.trace("getPublishedName()");
    return publishedStreamName;
  }

  public String getSaveFilename()
  {
    log.trace("getSaveFilename()");
    throw new Error("unimplemented method");
  }

  public void addStreamListener(IStreamListener listener)
  {
    log.trace("addStreamListener(listener: {})", listener);
    streamListeners.add(listener);
  }

  public Collection<IStreamListener> getStreamListeners()
  {
    log.trace("getStreamListeners()");
    return streamListeners;
  }

  public void removeStreamListener(IStreamListener listener)
  {
    log.trace("removeStreamListener({})", listener);
    streamListeners.remove(listener);
  }

  public void saveAs(String filePath, boolean isAppend) throws IOException,
  ResourceNotFoundException, ResourceExistException
  {
    log.trace("saveAs(filepath:{}, isAppend:{})", filePath, isAppend);
    throw new Error("unimplemented method");
  }

  public void setPublishedName(String name)
  {
    log.trace("setPublishedName(name:{})", name);
    publishedStreamName = name;
  }

  public void close()
  {
    log.trace("close()");
  }

  public IStreamCodecInfo getCodecInfo()
  {
    log.trace("getCodecInfo()");
    // we don't support this right now.
    return streamCodecInfo;
  }

  public String getName()
  {
    log.trace("getName(): {}", publishedStreamName);
    // for now, just return the published name
    return publishedStreamName;
  }

  public void setScope(IScope scope)
  {
    mScope = scope;
  }

  public IScope getScope()
  {
    log.trace("getScope(): {}", mScope);
    return mScope;
  }

  public void start()
  {
    log.trace("start()");
  }

  public void stop()
  {
    log.trace("stop");
  }

  public void onOOBControlMessage(IMessageComponent source, IPipe pipe,
      OOBControlMessage oobCtrlMsg)
  {
    log.trace("onOOBControlMessage");
  }

  public void onPipeConnectionEvent(PipeConnectionEvent event)
  {
    log.trace("onPipeConnectionEvent(event:{})", event);
    switch (event.getType())
    {
	    case PipeConnectionEvent.PROVIDER_CONNECT_PUSH:
	    	log.trace("PipeConnectionEvent.PROVIDER_CONNECT_PUSH");
		      if (event.getProvider() == this
		          && (event.getParamMap() == null || !event.getParamMap()
		              .containsKey("record")))
		      {
		    	  log.trace("Creating a live pipe");
		    	  System.out.println("Creating a live pipe");
		        this.livePipe = (IPipe) event.getSource();
		      }
	      break;
	    case PipeConnectionEvent.PROVIDER_DISCONNECT:
	    	log.trace("PipeConnectionEvent.PROVIDER_DISCONNECT");
	      if (this.livePipe == event.getSource())
	      {
	    	  log.trace("PipeConnectionEvent.PROVIDER_DISCONNECT - this.mLivePipe = null;");
	    	  System.out.println("PipeConnectionEvent.PROVIDER_DISCONNECT - this.mLivePipe = null;");
	        this.livePipe = null;
	      }
	      break;
	    case PipeConnectionEvent.CONSUMER_CONNECT_PUSH:
	    	log.trace("PipeConnectionEvent.CONSUMER_CONNECT_PUSH");
	    	System.out.println("PipeConnectionEvent.CONSUMER_CONNECT_PUSH");
	      break;
	    case PipeConnectionEvent.CONSUMER_DISCONNECT:
	    	log.trace("PipeConnectionEvent.CONSUMER_DISCONNECT");
	    	System.out.println("PipeConnectionEvent.CONSUMER_DISCONNECT");
	      break;
	    default:
	    	log.trace("PipeConnectionEvent default");
	    	System.out.println("PipeConnectionEvent default");
	      break;
    }
  }

  public void dispatchEvent(IEvent event)
  {
    try {
      log.trace("dispatchEvent(event:{})", event);
      if (event instanceof IRTMPEvent)
      {
        IRTMPEvent rtmpEvent = (IRTMPEvent) event;
        if (livePipe != null)
        {
          RTMPMessage msg = new RTMPMessage();

          msg.setBody(rtmpEvent);
          
          if (mCreationTime == null)
            mCreationTime = (long)rtmpEvent.getTimestamp();
          
          try
          {
        	  IVideoStreamCodec videoStreamCodec = new ScreenVideo();
        	  streamCodecInfo.setHasVideo(true);
        	  streamCodecInfo.setVideoCodec(videoStreamCodec);
        	  videoStreamCodec.reset();
        	  videoStreamCodec.addData(((VideoData) rtmpEvent).getData());
        	  livePipe.pushMessage(msg);

              // Notify listeners about received packet
              if (rtmpEvent instanceof IStreamPacket)
              {
                for (IStreamListener listener : getStreamListeners())
                {
                  try
                  {
                    listener.packetReceived(this, (IStreamPacket) rtmpEvent);
                  }
                  catch (Exception e)
                  {
                    log.error("Error while notifying listener " + listener, e);
                  }
                }
              } 	  
        	  
          }
          catch (IOException ex)
          {
            // ignore
            log.error("Got exception: {}", ex);
          }
        }
      }
    } finally {
      
    }
  }

  public long getCreationTime()
  {
    return mCreationTime != null ? mCreationTime : 0L;
  }
}
