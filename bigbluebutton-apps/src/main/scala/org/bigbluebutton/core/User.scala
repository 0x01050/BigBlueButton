package org.bigbluebutton.core

import org.bigbluebutton.core.api.UserVO
import org.bigbluebutton.core.api.Role._

class User(val intUserID: String, val extUserID: String, val name: String, val role: Role) {
     
  private var presenter = false
  private var handRaised = false
  private var hasStream = false
  private var voiceId:String = _
  
  def isPresenter():Boolean = {
    return presenter;
  }
  
  def becomePresenter() {
    presenter = true
  }
  
  def unbecomePresenter() {
    presenter = false
  }
  
  def toUserVO():UserVO = {
    new UserVO(intUserID, extUserID, name, role.toString, handRaised, isPresenter, hasStream)
  }
}