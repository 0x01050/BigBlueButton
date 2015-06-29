package org.bigbluebutton.modules.polling.service
{

  import flash.events.IEventDispatcher;
  import org.bigbluebutton.core.UsersUtil;
  import org.bigbluebutton.modules.polling.events.PollShowResultEvent;
  import org.bigbluebutton.modules.polling.events.PollStartedEvent;
  import org.bigbluebutton.modules.polling.events.PollStoppedEvent;
  import org.bigbluebutton.modules.polling.events.PollVotedEvent;
  import org.bigbluebutton.modules.polling.model.PollingModel;
  import org.bigbluebutton.modules.polling.model.SimpleAnswer;
  import org.bigbluebutton.modules.polling.model.SimpleAnswerResult;
  import org.bigbluebutton.modules.polling.model.SimplePoll;
  import org.bigbluebutton.modules.polling.model.SimplePollResult;

  public class PollDataProcessor
  {
    private static const LOG:String = "Poll::PollDataProcessor - ";
    
    /* Injected by Mate */
    public var model:PollingModel;
    public var dispatcher:IEventDispatcher;
           
    public function handlePollStartedMesage(msg:Object):void {
      trace(LOG + "*** Poll started " + msg.msg + " **** \n");
      
      var map:Object = JSON.parse(msg.msg);
      if (map.hasOwnProperty("poll")) {
        var poll:Object = map.poll;
        if (poll.hasOwnProperty("id") && poll.hasOwnProperty("answers")) {
          var pollId:String = poll.id;
          
          var answers:Array = poll.answers as Array;
          
          var ans:Array = new Array();
          
          for (var j:int = 0; j < answers.length; j++) {
            var a:Object = answers[j];
            ans.push(new SimpleAnswer(Number(String(a.id)), a.key));
          }
          
          model.setCurrentPoll(new SimplePoll(pollId, ans));
          dispatcher.dispatchEvent(new PollStartedEvent(new SimplePoll(pollId, ans)));            
        }      
      }
    }
    
    public function handlePollStoppedMesage(msg:Object):void {
      trace(LOG + "*** Poll stopped " + msg.msg + " **** \n");
      var map:Object = JSON.parse(msg.msg);
      dispatcher.dispatchEvent(new PollStoppedEvent());
    }
    
    public function handlePollShowResultMessage(msg:Object):void {
      trace(LOG + "*** Poll show result " + msg.msg + " **** \n");
      var map:Object = JSON.parse(msg.msg);
      if (map.hasOwnProperty("poll")) {
        var poll:Object = map.poll;
        if (poll.hasOwnProperty("id") && poll.hasOwnProperty("answers")) {
          var pollId:String = poll.id;
          
          var answers:Array = poll.answers as Array;
          
          var ans:Array = new Array();
          
          for (var j:int = 0; j < answers.length; j++) {
            var a:Object = answers[j];
            ans.push(new SimpleAnswerResult(a.id as Number, a.key, a.num_votes as Number));
          }
          
          dispatcher.dispatchEvent(new PollShowResultEvent(new SimplePollResult(pollId, ans)));            
        }      
      }    
    }
    
    public function handlePollUserVotedMessage(msg:Object):void {
      trace(LOG + "*** Poll user voted " + msg.msg + " **** \n");
      var map:Object = JSON.parse(msg.msg);
      if (map.hasOwnProperty("poll")) {
        var poll:Object = map.poll;
        if (poll.hasOwnProperty("id") && poll.hasOwnProperty("answers")) {
          var pollId:String = poll.id;
          
          var answers:Array = poll.answers as Array;
          
          var ans:Array = new Array();
          
          for (var j:int = 0; j < answers.length; j++) {
            var a:Object = answers[j];
            ans.push(new SimpleAnswerResult(a.id as Number, a.key, a.num_votes as Number));
          }
          
          dispatcher.dispatchEvent(new PollVotedEvent(new SimplePollResult(pollId, ans)));            
        }      
      }
      
    }
  }
}