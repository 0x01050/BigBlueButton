package org.bigbluebutton.modules.screenshare.events
{
  import flash.events.Event;
  
  public class ShareStartedEvent extends Event
  {
    public static const SHARE_STATED:String = "screenshare share started event";
    
    public var streamId:String;
    
    public function ShareStartedEvent(streamId: String)
    {
      super(SHARE_STATED, true, false);
      this.streamId = streamId;
    }
  }
}