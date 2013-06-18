package org.bigbluebutton.core.apps.poll

import scala.collection.mutable.HashMap
import org.bigbluebutton.core.api.InMessage
import org.bigbluebutton.core.api.MessageOutGateway
import scala.collection.mutable.ArrayBuffer

class PollApp(meetingID: String, recorded: Boolean, outGW: MessageOutGateway) {
  import org.bigbluebutton.core.apps.poll.messages._
  
  private val polls = new HashMap[String, Poll]()
	
  def handleMessage(msg: InMessage):Unit = {
    msg match {
      case createPoll: CreatePoll => handleCreatePoll(createPoll)
      case updatePoll: UpdatePoll => handleUpdatePoll(updatePoll)
      case destroyPoll: DestroyPoll => handleDestroyPoll(destroyPoll)
      case removePoll: RemovePoll => handleRemovePoll(removePoll)
      case sharePoll: SharePoll => handleSharePoll(sharePoll)
      case stopPoll: StopPoll => handleStopPoll(stopPoll)
      case startPoll: StartPoll => handleStartPoll(startPoll)
      case clearPoll: ClearPoll => handleClearPoll(clearPoll)
    }    
  }
  
  private def handleClearPoll(msg: ClearPoll) {
    polls.get(msg.pollID) match {
      case None => // send poll not found message
      case Some(p) => {
        p.clear
        outGW.send(new PollClearedOutMsg(meetingID, recorded, msg.pollID))
      }
    }
  }
  
  private def handleStartPoll(msg: StartPoll) {
    polls.get(msg.pollID) match {
      case None => // send poll not found message
      case Some(p) => {
        p.activate
        outGW.send(new PollStartedOutMsg(meetingID, recorded, msg.pollID))
      }
    }    
  }
  
  private def handleStopPoll(msg: StopPoll) {
    polls.get(msg.pollID) match {
      case None => // send poll not found message
      case Some(p) => {
        p.deactivate
        outGW.send(new PollStoppedOutMsg(meetingID, recorded, msg.pollID))
      }
    }     
  }
  
  private def handleSharePoll(msg: SharePoll) {
    
  }
  
  private def handleRemovePoll(msg: RemovePoll) {
    polls.get(msg.pollID) match {
      case None => // send poll not found message
      case Some(p) => {
        polls -= p.id
        outGW.send(new PollRemovedOutMsg(meetingID, recorded, msg.pollID))
      }
    }     
  }
  
  private def handleDestroyPoll(msg: DestroyPoll) {
    
  }
  
  private def handleUpdatePoll(msg: UpdatePoll) {
    val pollVO = msg.poll
    
    val questions = new ArrayBuffer[Question]
    
    pollVO.questions.foreach(qv => {
           
      val responses = new ArrayBuffer[Response]
      
      qv.responses.foreach(rv => {
    	  val response = new Response(rv.id, rv.text)
    	  responses += response
      })
      
      val rArray = new Array[Response](responses.length)
      responses.copyToArray(rArray)
      questions += new Question(qv.id, qv.questionType, qv.question, rArray)
    })
    
    val qArray = new Array[Question](questions.length)
    questions.copyToArray(qArray)
    
    val poll = new Poll(msg.poll.id, msg.poll.title, qArray)
    
    polls += poll.id -> poll    
  }
  
  private def handleCreatePoll(msg: CreatePoll) {
    val pollVO = msg.poll
    
    val questions = new ArrayBuffer[Question]
    
    pollVO.questions.foreach(qv => {
           
      val responses = new ArrayBuffer[Response]
      
      qv.responses.foreach(rv => {
    	  val response = new Response(rv.id, rv.text)
    	  responses += response
      })
      
      val rArray = new Array[Response](responses.length)
      responses.copyToArray(rArray)
      questions += new Question(qv.id, qv.questionType, qv.question, rArray)
    })
    
    val qArray = new Array[Question](questions.length)
    questions.copyToArray(qArray)
    
    val poll = new Poll(msg.poll.id, msg.poll.title, qArray)
    
    polls += poll.id -> poll
  }
  
  
}