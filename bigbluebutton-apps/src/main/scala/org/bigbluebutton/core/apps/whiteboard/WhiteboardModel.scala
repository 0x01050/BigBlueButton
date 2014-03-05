package org.bigbluebutton.core.apps.whiteboard

import org.bigbluebutton.core.apps.whiteboard.vo.AnnotationVO
import scala.collection.mutable.ArrayBuffer

class WhiteboardModel {
  private var _whiteboards = new scala.collection.immutable.HashMap[String, Whiteboard]()
  
  private var _enabled = true
    
  private def saveWhiteboard(wb: Whiteboard) {
    _whiteboards += wb.id -> wb
  }
  
  def getWhiteboard(id: String):Option[Whiteboard] = {
    _whiteboards.values.find(wb => wb.id == id)
  }
  
  
  def addAnnotationToShape(wb: Whiteboard, shape: AnnotationVO) = {
    println("Adding shape to wb [" + wb.id + "]. Before numShapes=[" + wb.shapes.length + "].")
    val newWb = wb.copy(shapes=(wb.shapes :+ shape))
    println("Adding shape to page [" + wb.id + "]. After numShapes=[" + newWb.shapes.length + "].")
    saveWhiteboard(newWb)
  }
  
  def addAnnotation(wbId:String, shape: AnnotationVO) {
    getWhiteboard(wbId) foreach { wb =>
        addAnnotationToShape(wb, shape) 
    }     
  }
  
  private def modifyTextInPage(wb: Whiteboard, shape: AnnotationVO) = {
    val removedLastText = wb.shapes.dropRight(1)
    val addedNewText = removedLastText :+ shape
    val newWb = wb.copy(shapes=addedNewText)
    saveWhiteboard(newWb)   
  }
  
  def modifyText(wbId:String, shape: AnnotationVO) {
     getWhiteboard(wbId) foreach { wb =>
        modifyTextInPage(wb, shape) 
    }   
  }
  
 
  def history(wbId:String):Option[Whiteboard] = {
    getWhiteboard(wbId)
  }
  
  def clearWhiteboard(wbId:String) {
    getWhiteboard(wbId) foreach { wb =>
        val clearedShapes = wb.shapes.drop(wb.shapes.length)
        val newWb = wb.copy(shapes= clearedShapes)
        saveWhiteboard(newWb)         
    }    
  }
  
  def undoWhiteboard(wbId:String) {
    getWhiteboard(wbId) foreach { wb =>
        val droppedShapes = wb.shapes.drop(wb.shapes.length-1)
        val newWb = wb.copy(shapes= droppedShapes)
        saveWhiteboard(newWb)          
    }  
  }
    
  def enableWhiteboard(enable: Boolean) {
    _enabled = enable
  }
  
  def isWhiteboardEnabled():Boolean = {
    _enabled
  }
}