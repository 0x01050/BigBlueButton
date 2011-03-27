require 'rubygems'
require 'redis'
require 'builder'

module BigBlueButton
  # Class to wrap Redis so we can mock
  # for testing
  class RedisWrapper
    def initialize(host, port)
      @host, @port = host, port
      @redis = Redis.new(:host => @host, :port => @port)
    end
    
    def connect      
      @redis.client.connect    
    end
    
    def disconnect
      @redis.client.disconnect
    end
    
    def connected?
      @redis.client.connected?
    end
    
    def metadata_for(meeting_id)
      @redis.hgetall("meeting:#{meeting_id}:metadata")
    end
    
    def num_events_for(meeting_id)
      @redis.llen("meeting:#{meeting_id}:recordings")
    end
    
    def events_for(meeting_id)
      @redis.lrange("meeting:#{meeting_id}:recordings", 0, num_events_for(meeting_id))
    end
    
    def event_info_for(meeting_id, event)
      @redis.hgetall("recording:#{meeting_id}:#{event}")
    end    
  end

  class RedisEventsArchiver
    TIMESTAMP = 'timestamp'
    MODULE = 'module'
    EVENTNAME = 'eventName'
    MEETINGID = 'meetingId'
    
    def initialize(redis)
      @redis = redis
    end
    
    def store_events(meeting_id)
      xml = Builder::XmlMarkup.new( :indent => 2 )
      result = xml.instruct! :xml, :version => "1.0"
      
      meeting_metadata = @redis.metadata_for(meeting_id)

      if (meeting_metadata != nil)
          xml.recording(:meeting_id => meeting_id) {
            xml.metadata {
              meeting_metadata.each do |key, val|
                xml.method_missing(key, val)
              end
            }
                        
            msgs = @redis.events_for(meeting_id)                      
            msgs.each do |msg|
              res = @redis.event_info_for(meeting_id, msg)
              xml.event(:timestamp => res[TIMESTAMP], :module => res[MODULE], :eventname => res[EVENTNAME]) {
                res.each do |key, val|
                  if not [TIMESTAMP, MODULE, EVENTNAME, MEETINGID].include?(key)
                    xml.method_missing(key,  val)
                  end
                end
              }
            end
          }
      end  
      xml.target!
    end
    
    def save_events_to_file(directory, result)
      a_file = File.new("#{directory}/events.xml","w+")
      a_file.write(result)
      a_file.close
    end
  end
end

