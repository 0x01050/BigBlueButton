package org.bigbluebutton.common.messages;

import java.util.HashMap;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class RequestWhiteboardAnnotationHistoryRequestMessage implements ISubscribedMessage {
	public static final String REQUEST_WHITEBOARD_ANNOTATION_HISTORY_REQUEST = "request_whiteboard_annotation_history_request";
	public static final String VERSION = "0.0.1";

	public final String meetingId;
	public final String whiteboardId;
	public final String requesterId;
	public final String replyTo;


	public RequestWhiteboardAnnotationHistoryRequestMessage(String meetingId,
			String requesterId, String whiteboardId, String replyTo) {
		this.meetingId = meetingId;
		this.whiteboardId = whiteboardId;
		this.requesterId = requesterId;
		this.replyTo = replyTo;
	}

	public String toJson() {
		HashMap<String, Object> payload = new HashMap<String, Object>();
		payload.put(Constants.MEETING_ID, meetingId);
		payload.put(Constants.WHITEBOARD_ID, whiteboardId);
		payload.put(Constants.REQUESTER_ID, requesterId);
		payload.put(Constants.REPLY_TO, replyTo);

		System.out.println("RequestWhiteboardAnnotationHistoryRequestMessage toJson");
		java.util.HashMap<String, Object> header = MessageBuilder.buildHeader(REQUEST_WHITEBOARD_ANNOTATION_HISTORY_REQUEST, VERSION, null);
		return MessageBuilder.buildJson(header, payload);
	}

	public static RequestWhiteboardAnnotationHistoryRequestMessage fromJson(String message) {
		JsonParser parser = new JsonParser();
		JsonObject obj = (JsonObject) parser.parse(message);
		if (obj.has("header") && obj.has("payload")) {
			JsonObject header = (JsonObject) obj.get("header");
			JsonObject payload = (JsonObject) obj.get("payload");

			if (header.has("name")) {
				String messageName = header.get("name").getAsString();
				if (REQUEST_WHITEBOARD_ANNOTATION_HISTORY_REQUEST.equals(messageName)) {
					System.out.println("4"+payload.toString());
					if (payload.has(Constants.MEETING_ID) 
							&& payload.has(Constants.WHITEBOARD_ID)
							&& payload.has(Constants.REPLY_TO)
							&& payload.has(Constants.REQUESTER_ID)) {
						String meetingId = payload.get(Constants.MEETING_ID).getAsString();
						String whiteboardId = payload.get(Constants.WHITEBOARD_ID).getAsString();
						String requesterId = payload.get(Constants.REQUESTER_ID).getAsString();
						String replyTo = payload.get(Constants.REPLY_TO).getAsString();

						System.out.println("RequestWhiteboardAnnotationHistoryRequestMessage fromJson");
						return new RequestWhiteboardAnnotationHistoryRequestMessage(meetingId, requesterId, whiteboardId, replyTo);
					}
				}
			}
		}
		return null;
	}
}
