package org.bigbluebutton.conference.service.presentation;

import java.util.HashMap;
import java.util.Map;

import org.bigbluebutton.conference.service.messaging.redis.MessageHandler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.bigbluebutton.core.api.IBigBlueButtonInGW;
import org.bigbluebutton.red5.pub.messages.*;

import com.google.gson.JsonParser;
import com.google.gson.JsonObject;

public class PresentationMessageListener implements MessageHandler {
        
        public static final String OFFICE_DOC_CONVERSION_SUCCESS_KEY = "OFFICE_DOC_CONVERSION_SUCCESS";
        public static final String OFFICE_DOC_CONVERSION_FAILED_KEY = "OFFICE_DOC_CONVERSION_FAILED";
        public static final String SUPPORTED_DOCUMENT_KEY = "SUPPORTED_DOCUMENT";
        public static final String UNSUPPORTED_DOCUMENT_KEY = "UNSUPPORTED_DOCUMENT";
        public static final String PAGE_COUNT_FAILED_KEY = "PAGE_COUNT_FAILED";
        public static final String PAGE_COUNT_EXCEEDED_KEY = "PAGE_COUNT_EXCEEDED";
        public static final String GENERATED_SLIDE_KEY = "GENERATED_SLIDE";
        public static final String GENERATING_THUMBNAIL_KEY = "GENERATING_THUMBNAIL";
        public static final String GENERATED_THUMBNAIL_KEY = "GENERATED_THUMBNAIL";
        public static final String CONVERSION_COMPLETED_KEY = "CONVERSION_COMPLETED";
        
        private ConversionUpdatesProcessor conversionUpdatesProcessor;
        private IBigBlueButtonInGW bbbInGW;
        
        public void setConversionUpdatesProcessor(ConversionUpdatesProcessor p) {
                conversionUpdatesProcessor = p;
        }
        
        public void setBigBlueButtonInGW(IBigBlueButtonInGW inGW) {
                bbbInGW = inGW;
        }
        
        private void sendConversionUpdate(String messageKey, String conference,
                String code, String presId, String filename) {
                
                conversionUpdatesProcessor.sendConversionUpdate(messageKey, conference,
                        code, presId, filename);
        }
        
        public void sendPageCountError(String messageKey, String conference,
                String code, String presId, Integer numberOfPages,
                Integer maxNumberPages, String filename) {
                
                conversionUpdatesProcessor.sendPageCountError(messageKey, conference,
                        code, presId, numberOfPages,
                        maxNumberPages, filename);
        }
        
        private void sendSlideGenerated(String messageKey, String conference,
                String code, String presId, Integer numberOfPages,
                Integer pagesCompleted, String filename) {
                
                conversionUpdatesProcessor.sendSlideGenerated(messageKey, conference,
                        code, presId, numberOfPages,
                        pagesCompleted, filename);
        }
        
        private void sendConversionCompleted(String messageKey, String conference,
                String code, String presId, Integer numberOfPages,
                String filename, String presBaseUrl) {
                
                conversionUpdatesProcessor.sendConversionCompleted(messageKey, conference,
                        code, presId, numberOfPages, filename, presBaseUrl);
        }

        @Override
    	public void handleMessage(String pattern, String channel, String message) {
    		if (channel.equalsIgnoreCase(MessagingConstants.TO_PRESENTATION_CHANNEL)) {
    			JsonParser parser = new JsonParser();
    			JsonObject obj = (JsonObject) parser.parse(message);

    			if (obj.has("header") && obj.has("payload")) {
    				JsonObject header = (JsonObject) obj.get("header");

    				if (header.has("name")) {
    					String messageName = header.get("name").getAsString();
    					if (SendConversionUpdateMessage.SEND_CONVERSION_UPDATE.equals(messageName)) {
    						SendConversionUpdateMessage msg = SendConversionUpdateMessage.fromJson(message);
    						System.out.println("in messageHandler - sendConversionCompleted");

//    						sendConversionUpdate(msg.messageKey, msg.meetingId, msg.code,
//    								msg.presId, msg.presName);
    						bbbInGW.sendConversionUpdate(msg.messageKey, msg.meetingId,
    								msg.code, msg.presId, msg.presName);
    					} else if (ResizeAndMoveSlideMessage.RESIZE_AND_MOVE_SLIDE.equals(messageName)) {
    						System.out.println("in messageHandler - resizeAndMoveSlide");
    						ResizeAndMoveSlideMessage msg = ResizeAndMoveSlideMessage.fromJson(message);

    						bbbInGW.resizeAndMoveSlide(msg.meetingId, msg.xOffset, msg.yOffset,
    								msg.widthRatio, msg.heightRatio);
    					} else if (GetPresentationInfoMessage.GET_PRESENTATION_INFO.equals(messageName)) {
    						System.out.println("in messageHandler - getPresentationInfo");
    						GetPresentationInfoMessage msg = GetPresentationInfoMessage.fromJson(message);

    						bbbInGW.getPresentationInfo(msg.meetingId, msg.requesterId, msg.replyTo);
    					} else if (SendConversionCompletedMessage.SEND_CONVERSION_COMPLETED.equals(messageName)) {
    						System.out.println("in messageHandler - sendConversionCompleted");
    						SendConversionCompletedMessage msg = SendConversionCompletedMessage.fromJson(message);

//    						sendConversionCompleted(msg.messageKey, msg.meetingId, msg.code,
//    								msg.presId, msg.numPages, msg.presName, msg.presBaseUrl);
    						bbbInGW.sendConversionCompleted(msg.messageKey, msg.meetingId, msg.code,
    								msg.presId, msg.numPages, msg.presName, msg.presBaseUrl);
    					} else if (SendPageCountErrorMessage.SEND_PAGE_COUNT_ERROR.equals(messageName)) {
    						System.out.println("in messageHandler - sendPageCountError");
    						SendPageCountErrorMessage msg = SendPageCountErrorMessage.fromJson(message);

//    						sendPageCountError(msg.messageKey, msg.meetingId, msg.code,
//    								msg.presId, msg.numberOfPages, msg.maxNumberPages, msg.presName);
    						bbbInGW.sendPageCountError(msg.messageKey, msg.meetingId, msg.code,
    								msg.presId, msg.numberOfPages, msg.maxNumberPages, msg.presName);
    					} else if (GoToSlideMessage.GO_TO_SLIDE.equals(messageName)) {
    						System.out.println("in messageHandler - goToSlide");
    						GoToSlideMessage msg = GoToSlideMessage.fromJson(message);

    						bbbInGW.gotoSlide(msg.meetingId, msg.page);
    					} else if (RemovePresentationMessage.REMOVE_PRESENTATION.equals(messageName)) {
    						System.out.println("in messageHandler - removePresentation");
    						RemovePresentationMessage msg = RemovePresentationMessage.fromJson(message);

    						bbbInGW.removePresentation(msg.meetingId, msg.presentationId);
    					} else if (SendCursorUpdateMessage.SEND_CURSOR_UPDATE.equals(messageName)) {
    						SendCursorUpdateMessage msg = SendCursorUpdateMessage.fromJson(message);

    						bbbInGW.sendCursorUpdate(msg.meetingId, msg.xPercent, msg.yPercent);
    					} else if (SharePresentationMessage.SHARE_PRESENTATION.equals(messageName)) {
    						System.out.println("in messageHandler - sharePresentation");
    						SharePresentationMessage msg = SharePresentationMessage.fromJson(message);

    						bbbInGW.sharePresentation(msg.meetingId, msg.presentationId, msg.share);
    					} else if (GetSlideInfoMessage.GET_SLIDE_INFO.equals(messageName)) {
    						System.out.println("in messageHandler - GetSlideInfo");
    						GetSlideInfoMessage msg = GetSlideInfoMessage.fromJson(message);

    						bbbInGW.getSlideInfo(msg.meetingId, msg.requesterId, msg.replyTo);
    					} else if (SendSlideGeneratedMessage.SEND_SLIDE_GENERATED.equals(messageName)) {
    						System.out.println("in messageHandler - SendSlideGenerated");
    						SendSlideGeneratedMessage msg = SendSlideGeneratedMessage.fromJson(message);

    						bbbInGW.sendSlideGenerated(msg.messageKey, msg.meetingId, msg.code,
    								msg.presId, msg.numberOfPages, msg.pagesCompleted, msg.presName);
    					}
    				}
    			}
    			else {
    				Gson gson = new Gson();
    				HashMap<String,String> map = gson.fromJson(message, new TypeToken<Map<String, String>>() {}.getType());
    				String code = (String) map.get("returnCode");
    				String presId = (String) map.get("presentationId");
    				String filename = (String) map.get("filename");
    				String conference = (String) map.get("conference");
    				String messageKey = (String) map.get("messageKey");

    				if (messageKey.equalsIgnoreCase(OFFICE_DOC_CONVERSION_SUCCESS_KEY) ||
    						messageKey.equalsIgnoreCase(OFFICE_DOC_CONVERSION_FAILED_KEY) ||
    						messageKey.equalsIgnoreCase(SUPPORTED_DOCUMENT_KEY) ||
    						messageKey.equalsIgnoreCase(UNSUPPORTED_DOCUMENT_KEY) ||
    						messageKey.equalsIgnoreCase(GENERATING_THUMBNAIL_KEY) ||
    						messageKey.equalsIgnoreCase(GENERATED_THUMBNAIL_KEY) ||
    						messageKey.equalsIgnoreCase(PAGE_COUNT_FAILED_KEY)){

    					sendConversionUpdate(messageKey, conference, code, presId, filename);
    				} else if(messageKey.equalsIgnoreCase(PAGE_COUNT_EXCEEDED_KEY)){

    					Integer numberOfPages = new Integer((String) map.get("numberOfPages"));
    					Integer maxNumberPages = new Integer((String) map.get("maxNumberPages"));
    					sendPageCountError(messageKey, conference, code,
    							presId, numberOfPages, maxNumberPages, filename);

    				} else if(messageKey.equalsIgnoreCase(GENERATED_SLIDE_KEY)){
    					Integer numberOfPages = new Integer((String) map.get("numberOfPages"));
    					Integer pagesCompleted = new Integer((String) map.get("pagesCompleted"));

    					sendSlideGenerated(messageKey, conference, code,
    							presId, numberOfPages, pagesCompleted, filename);

    				} else if(messageKey.equalsIgnoreCase(CONVERSION_COMPLETED_KEY)){
    					Integer numberOfPages = new Integer((String) map.get("numberOfPages"));
    					String presBaseUrl = (String) map.get("presentationBaseUrl");

    					sendConversionCompleted(messageKey, conference, code,
    							presId, numberOfPages, filename, presBaseUrl);
    				}
    			}
    		}
    	}
//        @Override
//        public void handleMessage(String pattern, String channel, String message) {
//                
//                if (channel.equalsIgnoreCase(MessagingConstants.TO_PRESENTATION_CHANNEL)) {
//                        
//                        JsonParser parser = new JsonParser();
//                        JsonObject obj = (JsonObject) parser.parse(message);
//                        
//                        if(obj.has("payload") && obj.has("header")) {
//                                
//                                JsonObject headerObject = (JsonObject) obj.get("header");
//                                JsonObject payloadObject = (JsonObject) obj.get("payload");
//                                
//                                String eventName =  headerObject.get("name").toString().replace("\"", "");
//                                
//                                if(eventName.equalsIgnoreCase("presentation_page_changed_message") ||
//                                        eventName.equalsIgnoreCase("presentation_page_resized_message")) {
//                                        
//                                        JsonObject pageObject = (JsonObject) payloadObject.get("page");
//                                        String roomName = payloadObject.get("meeting_id").toString().replace("\"", "");
//                                        
//                                        if(eventName.equalsIgnoreCase("presentation_page_changed_message")) {
//                                                String pageId = pageObject.get("id").toString().replace("\"", "");
//                                                bbbInGW.gotoSlide(roomName, pageId);
//                                        } else if(eventName.equalsIgnoreCase("presentation_page_resized_message")) {
//                                                String xOffset = pageObject.get("x_offset").toString().replace("\"", "");
//                                                String yOffset = pageObject.get("y_offset").toString().replace("\"", "");
//                                                String widthRatio = pageObject.get("width_ratio").toString().replace("\"", "");
//                                                String heightRatio = pageObject.get("height_ratio").toString().replace("\"", "");
//                                                bbbInGW.resizeAndMoveSlide(roomName, Double.parseDouble(xOffset), Double.parseDouble(yOffset), Double.parseDouble(widthRatio), Double.parseDouble(heightRatio));
//                                        }
//                                }
//                        }
//                        
//                }
//        }
}

