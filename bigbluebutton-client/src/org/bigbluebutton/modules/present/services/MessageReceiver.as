/**
 * BigBlueButton open source conferencing system - http://www.bigbluebutton.org/
 * 
 * Copyright (c) 2012 BigBlueButton Inc. and by respective authors (see below).
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 * 
 * BigBlueButton is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with BigBlueButton; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.bigbluebutton.modules.present.services
{
  import com.asfusion.mate.events.Dispatcher;
  
  import mx.collections.ArrayCollection;
  
  import org.bigbluebutton.common.LogUtil;
  import org.bigbluebutton.core.BBB;
  import org.bigbluebutton.core.UsersUtil;
  import org.bigbluebutton.core.managers.UserManager;
  import org.bigbluebutton.main.events.BBBEvent;
  import org.bigbluebutton.main.events.MadePresenterEvent;
  import org.bigbluebutton.main.model.users.BBBUser;
  import org.bigbluebutton.main.model.users.Conference;
  import org.bigbluebutton.main.model.users.IMessageListener;
  import org.bigbluebutton.modules.present.events.CursorEvent;
  import org.bigbluebutton.modules.present.events.MoveEvent;
  import org.bigbluebutton.modules.present.events.NavigationEvent;
  import org.bigbluebutton.modules.present.events.RemovePresentationEvent;
  import org.bigbluebutton.modules.present.events.UploadEvent;
  import org.bigbluebutton.modules.present.model.Page;
  import org.bigbluebutton.modules.present.model.Presentation;
  import org.bigbluebutton.modules.present.model.PresentationModel;
  import org.bigbluebutton.modules.present.model.Presenter;
  
  public class MessageReceiver implements IMessageListener
  {
    private static const LOG:String = "Present::MessageReceiver - ";
    
    private static const OFFICE_DOC_CONVERSION_SUCCESS_KEY:String = "OFFICE_DOC_CONVERSION_SUCCESS";
    private static const OFFICE_DOC_CONVERSION_FAILED_KEY:String = "OFFICE_DOC_CONVERSION_FAILED";
    private static const SUPPORTED_DOCUMENT_KEY:String = "SUPPORTED_DOCUMENT";
    private static const UNSUPPORTED_DOCUMENT_KEY:String = "UNSUPPORTED_DOCUMENT";
    private static const PAGE_COUNT_FAILED_KEY:String = "PAGE_COUNT_FAILED";
    private static const PAGE_COUNT_EXCEEDED_KEY:String = "PAGE_COUNT_EXCEEDED";    	
    private static const GENERATED_SLIDE_KEY:String = "GENERATED_SLIDE";
    private static const GENERATING_THUMBNAIL_KEY:String = "GENERATING_THUMBNAIL";
    private static const GENERATED_THUMBNAIL_KEY:String = "GENERATED_THUMBNAIL";
    private static const CONVERSION_COMPLETED_KEY:String = "CONVERSION_COMPLETED";
    
    private var dispatcher:Dispatcher;
    
    private var presModel:PresentationModel;
    
    public function MessageReceiver(presModel: PresentationModel) {
      this.presModel = presModel;
      BBB.initConnectionManager().addMessageListener(this);
      this.dispatcher = new Dispatcher();
    }
    
    public function onMessage(messageName:String, message:Object):void {
//      trace("Presentation: received message " + messageName);
      
      switch (messageName) {
        case "PresentationCursorUpdateCommand":
          handlePresentationCursorUpdateCommand(message);
          break;			
        case "goToSlideCallback":
          handleGotoSlideCallback(message);
          break;			
        case "moveCallback":
          handleMoveCallback(message);
          break;	
        case "sharePresentationCallback":
          handleSharePresentationCallback(message);
          break;
        case "removePresentationCallback":
          handleRemovePresentationCallback(message);
          break;
        case "conversionCompletedUpdateMessageCallback":
          handleConversionCompletedUpdateMessageCallback(message);
          break;
        case "generatedSlideUpdateMessageCallback":
          handleGeneratedSlideUpdateMessageCallback(message);
          break;
        case "pageCountExceededUpdateMessageCallback":
          handlePageCountExceededUpdateMessageCallback(message);
          break;
        case "conversionUpdateMessageCallback":
          handleConversionUpdateMessageCallback(message);
          break;
        case "getPresentationInfoReply":
          handleGetPresentationInfoReply(message);
          break;
        case "getSlideInfoReply":
          handleGetSlideInfoReply(message);
          break;
      }
    }  
    
    private function handleGetSlideInfoReply(msg:Object):void {
      trace(LOG + "*** handleGetSlideInfoReply " + msg.msg + " **** \n");
      
      var map:Object = JSON.parse(msg.msg);
      var e:MoveEvent = new MoveEvent(MoveEvent.CUR_SLIDE_SETTING);
      e.xOffset = map.xOffset;
      e.yOffset = map.yOffset;
      e.slideToCanvasWidthRatio = map.widthRatio;
      e.slideToCanvasHeightRatio = map.heightRatio;
      dispatcher.dispatchEvent(e);	  
    }
    
    private function handlePresentationCursorUpdateCommand(msg:Object):void {    
//      trace(LOG + "*** handlePresentationCursorUpdateCommand " + msg.msg + " **** \n");
      
      var map:Object = JSON.parse(msg.msg);
      
      var e:CursorEvent = new CursorEvent(CursorEvent.UPDATE_CURSOR);
      e.xPercent = map.xPercent;
      e.yPercent = map.yPercent;
      var dispatcher:Dispatcher = new Dispatcher();
      dispatcher.dispatchEvent(e);
    }
    
    private function handleGotoSlideCallback(msg:Object) : void {
      trace(LOG + "*** handleGotoSlideCallback " + msg.msg + " **** \n");
      var map:Object = JSON.parse(msg.msg);
      
      trace(LOG + "*** handleGotoSlideCallback GOTO_PAGE[" + (map.num - 1) + "] **** \n");
      var e:NavigationEvent = new NavigationEvent(NavigationEvent.GOTO_PAGE)
      e.pageNumber = map.num;
      dispatcher.dispatchEvent(e);
    }
    
    private function handleMoveCallback(msg:Object):void{
      trace(LOG + "*** handleMoveCallback " + msg.msg + " **** \n");
      
      var map:Object = JSON.parse(msg.msg);
      
  //    trace(LOG + "handleMoveCallback [" + msg.xOffset + "," +  msg.yOffset + "][" +  msg.widthRatio + "," + msg.heightRatio + "]");
      var e:MoveEvent = new MoveEvent(MoveEvent.MOVE);
      e.xOffset = map.xOffset;
      e.yOffset = map.yOffset;
      e.slideToCanvasWidthRatio = map.widthRatio;
      e.slideToCanvasHeightRatio = map.heightRatio;
      dispatcher.dispatchEvent(e);
    }
    
    private function handleSharePresentationCallback(msg:Object):void {
      trace(LOG + "*** handleSharePresentationCallback " + msg.msg + " **** \n");
      
      var map:Object = JSON.parse(msg.msg);
      
//      if (msg.share) {
        var e:UploadEvent = new UploadEvent(UploadEvent.PRESENTATION_READY);
        e.presentationName = map.presentationID;
        dispatcher.dispatchEvent(e);
//      } else {
//        dispatcher.dispatchEvent(new UploadEvent(UploadEvent.CLEAR_PRESENTATION));
//      }
    }
    
    private function handleRemovePresentationCallback(msg:Object):void {
      var e:RemovePresentationEvent = new RemovePresentationEvent(RemovePresentationEvent.PRESENTATION_REMOVED_EVENT);
      e.presentationName = msg.presentationID;
      dispatcher.dispatchEvent(e);
    }
    
    private function handleConversionCompletedUpdateMessageCallback(msg:Object) : void {
      trace(LOG + "*** handleConversionCompletedUpdateMessageCallback " + msg.msg + " **** \n");
      
      var map:Object = JSON.parse(msg.msg);
      
      pocessUploadedPresentation(map);
      
      var uploadEvent:UploadEvent = new UploadEvent(UploadEvent.CONVERT_SUCCESS);
      uploadEvent.data = CONVERSION_COMPLETED_KEY;
      uploadEvent.presentationName = map.id;
      dispatcher.dispatchEvent(uploadEvent);
      
      dispatcher.dispatchEvent(new BBBEvent(BBBEvent.PRESENTATION_CONVERTED));
      var readyEvent:UploadEvent = new UploadEvent(UploadEvent.PRESENTATION_READY);
      readyEvent.presentationName = map.id;
      dispatcher.dispatchEvent(readyEvent);
    }
    
    private function pocessUploadedPresentation(presentation:Object):void {
      var presoPages:ArrayCollection = new ArrayCollection();
      
      var pages:ArrayCollection = presentation.pages as ArrayCollection;
      for (var k:int = 0; k < pages.length; k++) {
        var page:Object = pages[k];
        var pg:Page = new Page(page.id, page.num, page.current,
          page.swfUri, page.thumbUri, page.txtUri,
          page.pngUri, page.xOffset, page.yOffset,
          page.withRatio, page.heightRatio)
        
        presoPages.addItem(pg);
      }
      
      var preso:Presentation = new Presentation(presentation.id, presentation.name, 
        presentation.current, pages);
      PresentationModel.getInstance().addPresentation(preso);
    }
    
    private function handleGeneratedSlideUpdateMessageCallback(msg:Object) : void {		
      trace(LOG + "*** handleGeneratedSlideUpdateMessageCallback " + msg.msg + " **** \n");
      
      var map:Object = JSON.parse(msg.msg);
      
      var uploadEvent:UploadEvent = new UploadEvent(UploadEvent.CONVERT_UPDATE);
      uploadEvent.totalSlides = map.numberOfPages as Number;
      uploadEvent.completedSlides = msg.pagesCompleted as Number;
      dispatcher.dispatchEvent(uploadEvent);	
    }
    
    private function handlePageCountExceededUpdateMessageCallback(msg:Object) : void {
      trace(LOG + "*** handlePageCountExceededUpdateMessageCallback " + msg.msg + " **** \n");
      
      var map:Object = JSON.parse(msg.msg);
      
      var uploadEvent:UploadEvent = new UploadEvent(UploadEvent.PAGE_COUNT_EXCEEDED);
      uploadEvent.maximumSupportedNumberOfSlides = map.maxNumberPages as Number;
      dispatcher.dispatchEvent(uploadEvent);
    }
    
    private function handleConversionUpdateMessageCallback(msg:Object) : void {
      trace(LOG + "*** handleConversionUpdateMessageCallback " + msg.msg + " **** \n");
      
      var map:Object = JSON.parse(msg.msg);
      
      var uploadEvent:UploadEvent;
      
      switch (map.messageKey) {
        case OFFICE_DOC_CONVERSION_SUCCESS_KEY :
          uploadEvent = new UploadEvent(UploadEvent.OFFICE_DOC_CONVERSION_SUCCESS);
          dispatcher.dispatchEvent(uploadEvent);
          break;
        case OFFICE_DOC_CONVERSION_FAILED_KEY :
          uploadEvent = new UploadEvent(UploadEvent.OFFICE_DOC_CONVERSION_FAILED);
          dispatcher.dispatchEvent(uploadEvent);
          break;
        case SUPPORTED_DOCUMENT_KEY :
          uploadEvent = new UploadEvent(UploadEvent.SUPPORTED_DOCUMENT);
          dispatcher.dispatchEvent(uploadEvent);
          break;
        case UNSUPPORTED_DOCUMENT_KEY :
          uploadEvent = new UploadEvent(UploadEvent.UNSUPPORTED_DOCUMENT);
          dispatcher.dispatchEvent(uploadEvent);
          break;
        case GENERATING_THUMBNAIL_KEY :	
          dispatcher.dispatchEvent(new UploadEvent(UploadEvent.THUMBNAILS_UPDATE));
          break;		
        case PAGE_COUNT_FAILED_KEY :
          uploadEvent = new UploadEvent(UploadEvent.PAGE_COUNT_FAILED);
          dispatcher.dispatchEvent(uploadEvent);
          break;	
        case GENERATED_THUMBNAIL_KEY :
          break;
        default:
          break;
      }														
    }	
    
    private var currentSlide:Number = -1;
    
    private function handleGetPresentationInfoReply(msg:Object) : void {
      trace(LOG + "*** handleGetPresentationInfoReply " + msg.msg + " **** \n");
      var map:Object = JSON.parse(msg.msg);
      
      var presenterMap:Object = map.presenter;
      
      var presenter: Presenter = new Presenter(presenterMap.userId, presenterMap.name, presenterMap.assignedBy);
      PresentationModel.getInstance().setPresenter(presenter);
            
      var presentations:Array = map.presentations as Array;
      for (var j:int = 0; j < presentations.length; j++) {
        var presentation:Object = presentations[j];        
        pocessUploadedPresentation(presentation);
      }
           
      var myUserId: String = UsersUtil.getMyUserID();
      
      
      if (presenter.userId != myUserId) {
        trace(LOG + " Making self viewer. myId=[" + myUserId + "] presenter=[" + presenter.userId + "]");
        dispatcher.dispatchEvent(new MadePresenterEvent(MadePresenterEvent.SWITCH_TO_VIEWER_MODE));						
      }	else {
        trace(LOG + " Making self presenter. myId=[" + myUserId + "] presenter=[" + presenter.userId + "]");
        dispatcher.dispatchEvent(new MadePresenterEvent(MadePresenterEvent.SWITCH_TO_PRESENTER_MODE));
      }
      
      var presNames:ArrayCollection = PresentationModel.getInstance().getPresentationNames();
          
      if (presNames) {
        trace(LOG + " ************ Getting list of presentations *************");
        for (var x:int = 0; x < presNames.length; x++) {
          sendPresentationName(presNames[x] as String);
        }
      }
           
      var curPresName:String = PresentationModel.getInstance().getCurrentPresentationName();
      
      var shareEvent:UploadEvent = new UploadEvent(UploadEvent.PRESENTATION_READY);
      shareEvent.presentationName = curPresName;
      dispatcher.dispatchEvent(shareEvent);
    }
    
    private function sendPresentationName(presentationName:String):void {
      trace(LOG + " **************** Sending presentation names");
      var uploadEvent:UploadEvent = new UploadEvent(UploadEvent.CONVERT_SUCCESS);
      uploadEvent.presentationName = presentationName;
      dispatcher.dispatchEvent(uploadEvent)
    }
    
  }
}