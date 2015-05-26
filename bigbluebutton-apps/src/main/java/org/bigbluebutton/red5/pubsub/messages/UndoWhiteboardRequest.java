package org.bigbluebutton.red5.pubsub.messages;

import java.util.HashMap;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class UndoWhiteboardRequest implements IMessage {
	public static final String UNDO_WHITEBOARD_REQUEST = "undo_whiteboard_request";
	public static final String VERSION = "0.0.1";

	public final String meetingId;
	public final String whiteboardId;
	public final String requesterId;


	public UndoWhiteboardRequest(String meetingId, String requesterId, String whiteboardId) {
		this.meetingId = meetingId;
		this.whiteboardId = whiteboardId;
		this.requesterId = requesterId;
	}

	public String toJson() {
		HashMap<String, Object> payload = new HashMap<String, Object>();
		payload.put(Constants.MEETING_ID, meetingId);
		payload.put(Constants.WHITEBOARD_ID, whiteboardId);
		payload.put(Constants.REQUESTER_ID, requesterId);

		System.out.println("UndoWhiteboardRequest toJson");
		java.util.HashMap<String, Object> header = MessageBuilder.buildHeader(UNDO_WHITEBOARD_REQUEST, VERSION, null);
		return MessageBuilder.buildJson(header, payload);
	}

	public static UndoWhiteboardRequest fromJson(String message) {
		JsonParser parser = new JsonParser();
		JsonObject obj = (JsonObject) parser.parse(message);
		if (obj.has("header") && obj.has("payload")) {
			JsonObject header = (JsonObject) obj.get("header");
			JsonObject payload = (JsonObject) obj.get("payload");

			if (header.has("name")) {
				String messageName = header.get("name").getAsString();
				if (UNDO_WHITEBOARD_REQUEST.equals(messageName)) {
					System.out.println("4"+payload.toString());
					if (payload.has(Constants.MEETING_ID) 
							&& payload.has(Constants.WHITEBOARD_ID)
							&& payload.has(Constants.REQUESTER_ID)) {
						String meetingId = payload.get(Constants.MEETING_ID).getAsString();
						String whiteboardId = payload.get(Constants.WHITEBOARD_ID).getAsString();
						String requesterId = payload.get(Constants.REQUESTER_ID).getAsString();

						System.out.println("UndoWhiteboardRequest fromJson");
						return new UndoWhiteboardRequest(meetingId, requesterId, whiteboardId);
					}
				}
			}
		}
		return null;
	}
}
