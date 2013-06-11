package org.bigbluebutton.conference.service.presentation;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.bigbluebutton.conference.service.messaging.MessagingConstants;
import org.bigbluebutton.conference.service.messaging.redis.MessageHandler;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
	
	private ConversionUpdatesMessageListener listener;
	
	
	public void setPresentationListener(ConversionUpdatesMessageListener listener) {
		this.listener = listener;
	}


	@Override
	public void handleMessage(String pattern, String channel, String message) {
		if (channel.equalsIgnoreCase(MessagingConstants.PRESENTATION_CHANNEL)) {
			Gson gson = new Gson();
			HashMap<String,String> map = gson.fromJson(message, new TypeToken<Map<String, String>>() {}.getType());

			String code = (String) map.get("returnCode");
	    	String room = (String) map.get("room");
	    	String presentationName = (String) map.get("presentationName");
	    	String conference = (String) map.get("conference");
	    	String messageKey = (String) map.get("messageKey");
	    	
			Map message = new HashMap();
	    	message.put("conference", conference);
			message.put("room", room);
			message.put("code", code);
			message.put("presentationName", presentationName);
			message.put("messageKey", messageKey);
			
			log.debug("Message: " + messageKey + "[ " + presentationName + "]");
			
			if(messageKey.equalsIgnoreCase(OFFICE_DOC_CONVERSION_SUCCESS_KEY)||
					messageKey.equalsIgnoreCase(OFFICE_DOC_CONVERSION_FAILED_KEY)||
					messageKey.equalsIgnoreCase(SUPPORTED_DOCUMENT_KEY)||
					messageKey.equalsIgnoreCase(UNSUPPORTED_DOCUMENT_KEY)||
					messageKey.equalsIgnoreCase(GENERATING_THUMBNAIL_KEY)||
					messageKey.equalsIgnoreCase(GENERATED_THUMBNAIL_KEY)||
					messageKey.equalsIgnoreCase(PAGE_COUNT_FAILED_KEY)){
				
				conversionUpdatesProcessor.process(message);
			}
			else if(messageKey.equalsIgnoreCase(PAGE_COUNT_EXCEEDED_KEY)){
				Integer numberOfPages = new Integer((String) mapMessage.get("numberOfPages"));
				Integer maxNumberPages = new Integer((String) mapMessage.get("maxNumberPages"));
				message.put("numberOfPages", numberOfPages);
				message.put("maxNumberPages", maxNumberPages);
				conversionUpdatesProcessor.process(message);
			}
			else if(messageKey.equalsIgnoreCase(GENERATED_SLIDE_KEY)){
				Integer numberOfPages = new Integer((String)mapMessage.get("numberOfPages"));
				Integer pagesCompleted = new Integer((String)mapMessage.get("pagesCompleted"));
				message.put("numberOfPages", numberOfPages);
				message.put("pagesCompleted", pagesCompleted);
				
				conversionUpdatesProcessor.process(message);
			}
			else if(messageKey.equalsIgnoreCase(CONVERSION_COMPLETED_KEY)){
				String slidesInfo = (String) mapMessage.get("slidesInfo");
				message.put("slidesInfo", StringEscapeUtils.unescapeXml(slidesInfo));				
				conversionUpdatesProcessor.process(message);
			}
			else{
				log.error("Cannot handle recieved message.");
			}
		}
	}
}
