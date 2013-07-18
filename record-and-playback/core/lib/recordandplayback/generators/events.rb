# Set encoding to utf-8
# encoding: UTF-8

#
# BigBlueButton open source conferencing system - http://www.bigbluebutton.org/
#
# Copyright (c) 2012 BigBlueButton Inc. and by respective authors (see below).
#
# This program is free software; you can redistribute it and/or modify it under the
# terms of the GNU Lesser General Public License as published by the Free Software
# Foundation; either version 3.0 of the License, or (at your option) any later
# version.
#
# BigBlueButton is distributed in the hope that it will be useful, but WITHOUT ANY
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
# PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public License along
# with BigBlueButton; if not, see <http://www.gnu.org/licenses/>.
#


require 'rubygems'
require 'nokogiri'

module BigBlueButton
  module Events
  
    # Get the meeting metadata
    def self.get_meeting_metadata(events_xml)
      BigBlueButton.logger.info("Task: Getting meeting metadata")
      doc = Nokogiri::XML(File.open(events_xml))
      metadata = {}
      doc.xpath("//metadata").each do |e| 
        e.keys.each do |k| 
          metadata[k] = e.attribute(k)
        end
      end  
      metadata
    end
    
    # Get the timestamp of the first event.
    def self.first_event_timestamp(events_xml)
      BigBlueButton.logger.info("Task: Getting the timestamp of the first event.")
      doc = Nokogiri::XML(File.open(events_xml))
      doc.xpath("recording/event").first["timestamp"].to_i
    end
    
    # Get the timestamp of the last event.
    def self.last_event_timestamp(events_xml)
      BigBlueButton.logger.info("Task: Getting the timestamp of the last event")
      doc = Nokogiri::XML(File.open(events_xml))
      doc.xpath("recording/event").last["timestamp"].to_i
    end  
    
    # Determine if the start and stop event matched.
    def self.find_video_event_matched(start_events, stop)      
      BigBlueButton.logger.info("Task: Finding video events that match")
      start_events.each do |start|
        if (start[:stream] == stop[:stream])
          return start
        end      
      end
      return nil
    end
    
    # Match the start and stop events.
    def self.match_start_and_stop_video_events(start_events, stop_events)
      BigBlueButton.logger.info("Task: Matching the start and stop events")
      matched_events = []
      stop_events.each do |stop|
        start_evt = find_video_event_matched(start_events, stop)
        if start_evt
          start_evt[:stop_timestamp] = stop[:stop_timestamp]
          matched_events << start_evt
        else
          matched_events << stop
        end
      end      
      matched_events.sort { |a, b| a[:start_timestamp] <=> b[:start_timestamp] }
    end
      
    # Get start video events  
    def self.get_start_video_events(events_xml)
      BigBlueButton.logger.info("Task: Getting start video events")
      start_events = []
      doc = Nokogiri::XML(File.open(events_xml))
      doc.xpath("//event[@eventname='StartWebcamShareEvent']").each do |start_event|
        start_events << {:start_timestamp => start_event['timestamp'].to_i, :stream => start_event.xpath('stream').text}
      end
      start_events
    end

    # Get stop video events
    def self.get_stop_video_events(events_xml)
      BigBlueButton.logger.info("Task: Getting stop video events")
      stop_events = []
      doc = Nokogiri::XML(File.open(events_xml))
      doc.xpath("//event[@eventname='StopWebcamShareEvent']").each do |stop_event|
        stop_events << {:stop_timestamp => stop_event['timestamp'].to_i, :stream => stop_event.xpath('stream').text}
      end
      stop_events
    end
        
    # Determine if the start and stop event matched.
    def self.deskshare_event_matched?(stop_events, start)
      BigBlueButton.logger.info("Task: Determining if the start and stop DESKSHARE events matched")      
      stop_events.each do |stop|
        if (start[:stream] == stop[:stream])
          start[:matched] = true
          start[:stop_timestamp] = stop[:stop_timestamp]
          return true
        end      
      end
      return false
    end
    
    # Match the start and stop events.
    def self.match_start_and_stop_deskshare_events(start_events, stop_events)
      BigBlueButton.logger.info("Task: Matching start and stop DESKSHARE events")      
      combined_events = []
      start_events.each do |start|
        if not video_event_matched?(stop_events, start) 
          stop_event = {:stop_timestamp => stop[:stop_timestamp], :stream => stop[:stream], :matched => false}
          combined_events << stop_event
        else
          stop_events = stop_events - [stop_event]
        end
      end      
      return combined_events.concat(start_events)
    end    
    
    def self.get_start_deskshare_events(events_xml)
      BigBlueButton.logger.info("Task: Getting start DESKSHARE events")      
      start_events = []
      doc = Nokogiri::XML(File.open(events_xml))
      doc.xpath("//event[@eventname='DeskshareStartedEvent']").each do |start_event|
        s = {:start_timestamp => start_event['timestamp'].to_i, :stream => start_event.xpath('file').text.sub(/(.+)\//, "")}
        start_events << s
      end
      start_events.sort {|a, b| a[:start_timestamp] <=> b[:start_timestamp]}
    end

    def self.get_stop_deskshare_events(events_xml)
      BigBlueButton.logger.info("Task: Getting stop DESKSHARE events")      
      stop_events = []
      doc = Nokogiri::XML(File.open(events_xml))
      doc.xpath("//event[@eventname='DeskshareStoppedEvent']").each do |stop_event|
        s = {:stop_timestamp => stop_event['timestamp'].to_i, :stream => stop_event.xpath('file').text.sub(/(.+)\//, "")}
        stop_events << s
      end
      stop_events.sort {|a, b| a[:stop_timestamp] <=> b[:stop_timestamp]}
    end

    def self.linkify( text )
      generic_URL_regexp = Regexp.new( '(^|[\n ])([\w]+?://[\w]+[^ \"\n\r\t<]*)', Regexp::MULTILINE | Regexp::IGNORECASE )
      starts_with_www_regexp = Regexp.new( '(^|[\n ])((www)\.[^ \"\t\n\r<]*)', Regexp::MULTILINE | Regexp::IGNORECASE )
      
      s = text.to_s
      s.gsub!( generic_URL_regexp, '\1<a href="\2">\2</a>' )
      s.gsub!( starts_with_www_regexp, '\1<a href="http://\2">\2</a>' )
      s
    end

    #		
    # Get events when the moderator wants the recording to start or stop
    #
    def self.get_start_and_stop_rec_events(events_xml)
      BigBlueButton.logger.info "Getting start and stop rec button events"
      rec_events = []
      doc = Nokogiri::XML(File.open(events_xml))

      #Comment this code below in production
      a=doc.xpath("//event[@eventname='PublicChatEvent']")
      BigBlueButton.logger.info "Extracting PublicChatEvent events " + a.size.to_s
      a.each do |event|
    	if not event.xpath("message[text()='START' or text()='STOP']").empty?
    		  #rec_events << a             
    		  event.attributes["eventname"].value ='RecordStatusEvent'
    		  BigBlueButton.logger.info event.text()
    	end
      end
    
      BigBlueButton.logger.info ("Finished extracting record events")
      #Comment this code above in production

      doc.xpath("//event[@eventname='RecordStatusEvent']").each do |event|
    	s = {:start_timestamp => event['timestamp'].to_i}
    	rec_events << s
      end
      if rec_events.size.odd?
    	#User forgot to click Record Button to stop the recording
    	rec_events << { :start_timestamp => BigBlueButton::Events.last_event_timestamp(events_xml) }
      end      
    
      BigBlueButton.logger.info ("Finished processing record events")
    
      rec_events.sort {|a, b| a[:start_timestamp] <=> b[:start_timestamp]}  
    end
    
    #
    # Match recording start and stop events
    #
    
    def self.match_start_and_stop_rec_events(rec_events)
      BigBlueButton.logger.info ("Matching record events")
      matched_rec_events = []
      rec_events.each_with_index { |evt,i|
    	if i.even?
    		   evt[:stop_timestamp] = rec_events[i+1][:start_timestamp]
    		   matched_rec_events << evt
    	end       
      }
      matched_rec_events     
    end
  end
end
