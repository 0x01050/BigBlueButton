package org.bigbluebutton.core.apps.presentation

case class Presentation(id: String, name: String, current: Boolean = false, 
                        pages: scala.collection.immutable.HashMap[String, Page])
                        
case class Page(id: String, num: Int, current: Boolean = false, 
                thumbnail: String = "",
                xOffset: Double = 0, yOffset: Double = 0,
                widthRatio: Double = 0D, heightRatio: Double = 0D)
                
class PresentationModel {
  private var presentations = new scala.collection.immutable.HashMap[String, Presentation]
  
  def addPresentation(pres: Presentation) {
      savePresentation(pres)
  }
  
  def getPresentations():Seq[Presentation] = {
    presentations.values.toSeq
  }
  
  def getCurrentPresentation():Option[Presentation] = {
    presentations.values find (p => p.current)
  }
  
  def getCurrentPage(pres: Presentation):Option[Page] = {
    pres.pages.values find (p => p.current)
  }

  def remove(presId: String):Option[Presentation] = {
    val pres = presentations.get(presId)
    pres foreach (p => presentations -= p.id)    
    pres
  }

  def sharePresentation(presId: String):Option[Presentation] = {
    getCurrentPresentation foreach (curPres => {
      if (curPres.id != presId) {
        val newPres = curPres.copy(current = false)
        savePresentation(newPres)
      }
    })
    
    presentations.get(presId) match {
      case Some(pres) => {
        val cp = pres.copy(current = true)
        savePresentation(cp)
        Some(cp)
      }
      case None => None
    }
  }
  
  private def savePresentation(pres: Presentation) {
    presentations += pres.id -> pres
  }
    
  private def deactivateCurrentPage(pres: Presentation) {
    getCurrentPage(pres) match {
      case Some(cp) => {
        val page = cp.copy(current = false)
        val nPages = pres.pages + (page.id -> page)
        val newPres = pres.copy(pages= nPages)
        savePresentation(newPres)
        Some(page)
      }
      case None => None
    }
  }
  
  private def resizeCurrentPage(pres: Presentation, 
                                xOffset: Double, yOffset: Double, 
                                widthRatio: Double, 
                                heightRatio: Double):Option[Page] = {
    getCurrentPage(pres) match {
      case Some(cp) => {
        val page = cp.copy(xOffset = xOffset, yOffset = yOffset,
                           widthRatio= widthRatio, heightRatio = heightRatio)
        val nPages = pres.pages + (page.id -> page)
        val newPres = pres.copy(pages= nPages)
        savePresentation(newPres)   
        Some(page)
      }
      case None => None
    } 
  }
  
  def resizePage(xOffset: Double, yOffset: Double, 
                 widthRatio: Double, heightRatio: Double):Option[Page] = {
    for {
      curPres <- getCurrentPresentation
      page <- resizeCurrentPage(curPres, xOffset, yOffset, widthRatio, heightRatio)
    } yield page
  } 
  
  private def makePageCurrent(pres: Presentation, page: String):Option[Page] = {
    deactivateCurrentPage(pres)
    pres.pages.values find (p => p.id == page) match {
      case Some(newCurPage) => {
        val page = newCurPage.copy(current=true)
        val newPages = pres.pages + (page.id -> page)
        val newPres = pres.copy(pages= newPages)
        savePresentation(newPres)
        Some(page)
      }
      case None => None
    }    
  }
  
  def changePage(pageId: String):Option[Page] = {
    for {
      pres <- getCurrentPresentation      
      page <- makePageCurrent(pres, pageId)
    } yield page
  }
  
}