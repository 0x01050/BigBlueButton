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

package org.bigbluebutton.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bigbluebutton.api.domain.Meeting;
import org.bigbluebutton.api.util.ParamsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParamsProcessorUtil {
    private static Logger log = LoggerFactory.getLogger(ParamsProcessorUtil.class);

    private static final String URLDECODER_SEPARATOR=",";
    private static final String FILTERDECODER_SEPARATOR_ELEMENTS=":";
    private static final String FILTERDECODER_SEPARATOR_OPERATORS="\\|";
    
    private static final String SERVER_URL = "%%SERVERURL%%";
    private static final String DIAL_NUM = "%%DIALNUM%%";
    private static final String CONF_NUM = "%%CONFNUM%%";
    private static final String CONF_NAME = "%%CONFNAME%%";

    private String apiVersion;
    private boolean serviceEnabled = false;
    private String securitySalt;
    private int defaultMaxUsers = 20;
    private String defaultWelcomeMessage;
    private String defaultWelcomeMessageFooter;
    private String defaultDialAccessNumber;
    private String testVoiceBridge;
    private String testConferenceMock;
    private String defaultLogoutUrl;
    private String defaultServerUrl;
    private int defaultNumDigitsForTelVoice;
    private String defaultClientUrl;
    private String defaultGuestWaitURL;
    private String html5ClientUrl;
    private Boolean moderatorsJoinViaHTML5Client;
    private Boolean attendeesJoinViaHTML5Client;
    private String defaultAvatarURL;
    private String defaultConfigURL;
    private String defaultGuestPolicy;
    private int defaultMeetingDuration;
    private boolean disableRecordingDefault;
    private boolean autoStartRecording;
    private boolean allowStartStopRecording;
    private boolean webcamsOnlyForModerator;
    private boolean defaultMuteOnStart = false;

    private String defaultConfigXML = null;

    private Long maxPresentationFileUpload = 30000000L; // 30MB

    private Integer maxInactivityTimeoutMinutes = 120;
    private Integer clientLogoutTimerInMinutes = 0;
		private Integer warnMinutesBeforeMax = 5;
		private Integer meetingExpireIfNoUserJoinedInMinutes = 5;
		private Integer meetingExpireWhenLastUserLeftInMinutes = 1;

    private String substituteKeywords(String message, String dialNumber, String telVoice, String meetingName) {
        String welcomeMessage = message;

        ArrayList<String> keywordList = new ArrayList<>();
        keywordList.add(DIAL_NUM);
        keywordList.add(CONF_NUM);
        keywordList.add(CONF_NAME);
        keywordList.add(SERVER_URL);

        for (String keyword : keywordList) {
            if (keyword.equals(DIAL_NUM)) {
                welcomeMessage = welcomeMessage.replaceAll(DIAL_NUM, dialNumber);
            } else if (keyword.equals(CONF_NUM)) {
                welcomeMessage = welcomeMessage.replaceAll(CONF_NUM, telVoice);
            } else if (keyword.equals(CONF_NAME)) {
                welcomeMessage = welcomeMessage.replaceAll(CONF_NAME, meetingName);
            } else if (keyword.equals(SERVER_URL)) {
                welcomeMessage = welcomeMessage.replaceAll(SERVER_URL, defaultServerUrl);
            }
        }
        return  welcomeMessage;
    }

    public void processRequiredCreateParams(Map<String, String> params, ApiErrors errors) {
        // Do we have a checksum? If not, complain.
        if (StringUtils.isEmpty(params.get(ApiParams.CHECKSUM))) {
            errors.missingParamError(ApiParams.CHECKSUM);
        }

        // Do we have a meeting id? If not, complain.
        if (!StringUtils.isEmpty(params.get(ApiParams.MEETING_ID))) {
            String meetingId = StringUtils.strip(params.get(ApiParams.MEETING_ID));
            if (StringUtils.isEmpty(meetingId)) {
                errors.missingParamError(ApiParams.MEETING_ID);
            } else {
                if (!ParamsUtil.isValidMeetingId(meetingId)) {
                    errors.addError(new String[] { "invalidFormat", "Meeting id contains invalid characters." });
                }
            }
        } else {
            errors.missingParamError(ApiParams.MEETING_ID);
        }
    }

	public void updateMeeting(Map<String, Object> updateParams, Meeting existing) {
		// TODO: Assign new values to meeting.
/*	    
	    String meetingName = (String) updateParams.get("name");
	    if (meetingName != null) {
	    	existing.setM("name", meetingName);
	    }
	    	    
	    String viewerPass = params.get("attendeePW");
	    if (! StringUtils.isEmpty(viewerPass) ) {
	    	newParams.put("attendeePW", viewerPass);
	    }
	    
	    String modPass = params.get("moderatorPW"); 
	    if (! StringUtils.isEmpty(modPass) ) {
	    	newParams.put("moderatorPW", modPass);
	    }
	    
	    String telVoice = params.get("voiceBridge");
	    if (! StringUtils.isEmpty(telVoice) ) {
	    	newParams.put("voiceBridge", telVoice);
	    }	    
	    
	    String webVoice = params.get("webVoice");
	    if (! StringUtils.isEmpty(webVoice)) {
	    	newParams.put("webVoice", webVoice);
	    }
	    
	    String dialNumber = params.get("dialNumber");
	    if (! StringUtils.isEmpty(dialNumber)) {
	    	newParams.put("dialNumber", dialNumber);
	    }	    
	    
	    String logoutUrl = params.get("logoutURL"); 
	    if (! StringUtils.isEmpty(logoutUrl)) {
	    	newParams.put("logoutURL", logoutUrl);
	    }	
	    
	    String record = params.get("record");
	    if (! StringUtils.isEmpty(record)) {
	    	newParams.put("record", record);
	    }	
	    
	    String maxUsers = params.get("maxParticipants");
	    if (! StringUtils.isEmpty(maxUsers)) {
	    	newParams.put("maxParticipants", maxUsers);
	    }	
	    
	    String meetingDuration = params.get("duration");
	    if (! StringUtils.isEmpty(meetingDuration)) {
	    	newParams.put("duration", meetingDuration);
	    }
	    
	    String welcomeMessage = params.get("welcome");
	    if (! StringUtils.isEmpty(welcomeMessage)) {
	    	newParams.put("welcome", welcomeMessage);
	    }
	    	    	    
	    // Collect metadata for this meeting that the third-party app wants to store if meeting is recorded.
	    Map<String, String> meetingInfo = new HashMap<String, String>();
	    for (String key: params.keySet()) {
	    	if (key.contains("meta")){
	    		String[] meta = key.split("_");
			    if(meta.length == 2){
			    	meetingInfo.put(meta[1], params.get(key));
			    }
			}   
	    }

	    if (! meetingInfo.isEmpty()) {
	    	newParams.put("metadata", meetingInfo);
	    }		
*/
	}
	
	public Map<String, Object> processUpdateCreateParams(Map<String, String> params) {
		Map<String, Object> newParams = new HashMap<>();
		    
	    // Do we have a meeting name? If not, complain.
	    String meetingName = params.get(ApiParams.NAME);
	    if (! StringUtils.isEmpty(meetingName) ) {
	    	newParams.put(ApiParams.NAME, meetingName);
	    }
	    	    
	    String viewerPass = params.get(ApiParams.ATTENDEE_PW);
	    if (! StringUtils.isEmpty(viewerPass) ) {
	    	newParams.put(ApiParams.ATTENDEE_PW, viewerPass);
	    }
	    
	    String modPass = params.get(ApiParams.MODERATOR_PW); 
	    if (! StringUtils.isEmpty(modPass) ) {
	    	newParams.put(ApiParams.MODERATOR_PW, modPass);
	    }
	    
	    String telVoice = params.get(ApiParams.VOICE_BRIDGE);
	    if (! StringUtils.isEmpty(telVoice) ) {
	    	newParams.put(ApiParams.VOICE_BRIDGE, telVoice);
	    }	    
	    
	    String webVoice = params.get(ApiParams.WEB_VOICE);
	    if (! StringUtils.isEmpty(webVoice)) {
	    	newParams.put(ApiParams.WEB_VOICE, webVoice);
	    }
	    
	    String dialNumber = params.get(ApiParams.DIAL_NUMBER);
	    if (! StringUtils.isEmpty(dialNumber)) {
	    	newParams.put(ApiParams.DIAL_NUMBER, dialNumber);
	    }	    
	    
	    String logoutUrl = params.get(ApiParams.LOGOUT_URL); 
	    if (! StringUtils.isEmpty(logoutUrl)) {
	    	newParams.put(ApiParams.LOGOUT_URL, logoutUrl);
	    }	
	    
	    String record = params.get(ApiParams.RECORD);
	    if (! StringUtils.isEmpty(record)) {
	    	newParams.put(ApiParams.RECORD, record);
	    }	
	    
	    String maxUsers = params.get(ApiParams.MAX_PARTICIPANTS);
	    if (! StringUtils.isEmpty(maxUsers)) {
	    	newParams.put(ApiParams.MAX_PARTICIPANTS, maxUsers);
	    }	
	    
	    String meetingDuration = params.get(ApiParams.DURATION);
	    if (! StringUtils.isEmpty(meetingDuration)) {
	    	newParams.put(ApiParams.DURATION, meetingDuration);
	    }
	    
	    String welcomeMessage = params.get(ApiParams.WELCOME);
	    if (! StringUtils.isEmpty(welcomeMessage)) {
	    	newParams.put(ApiParams.WELCOME, welcomeMessage);
	    }
	    	    	    
	    // Collect metadata for this meeting that the third-party app wants to store if meeting is recorded.
	    Map<String, String> meetingInfo = new HashMap<>();
	    for (String key: params.keySet()) {
	    	if (key.contains(ApiParams.META)){
	    		String[] meta = key.split("_");
			    if(meta.length == 2){
			    	meetingInfo.put(meta[1], params.get(key));
			    }
			}   
	    }

        if (!meetingInfo.isEmpty()) {
            newParams.put("metadata", meetingInfo);
        }
	    
	    return newParams;
	}
	
	private static final Pattern META_VAR_PATTERN = Pattern.compile("meta_[a-zA-Z][a-zA-Z0-9-]*$");	
	public static Boolean isMetaValid(String param) {
		Matcher metaMatcher = META_VAR_PATTERN.matcher(param);
    if (metaMatcher.matches()) {
    	return true;
    }	
		return false;
	}
	
	public static String removeMetaString(String param) {
		return StringUtils.removeStart(param, "meta_");
	}
	
    public static Map<String, String> processMetaParam(Map<String, String> params) {
        Map<String, String> metas = new HashMap<>();
        for (String key : params.keySet()) {
            if (isMetaValid(key)) {
                // Need to lowercase to maintain backward compatibility with
                // 0.81
                String metaName = removeMetaString(key).toLowerCase();
                metas.put(metaName, params.get(key));
            }
        }

        return metas;
    }
	
    public Meeting processCreateParams(Map<String, String> params) {

        String meetingName = params.get(ApiParams.NAME);
        if (meetingName == null) {
            meetingName = "";
        }

        meetingName = ParamsUtil.stripControlChars(meetingName);

        String externalMeetingId = params.get(ApiParams.MEETING_ID);

        String viewerPass = processPassword(params.get(ApiParams.ATTENDEE_PW));
        String modPass = processPassword(params.get(ApiParams.MODERATOR_PW));

        // Get the digits for voice conference for users joining through the
        // phone.
        // If none is provided, generate one.
        String telVoice = processTelVoice(params.get(ApiParams.VOICE_BRIDGE));

        // Get the voice conference digits/chars for users joing through VOIP on
        // the client.
        // If none is provided, make it the same as the telVoice. If one has
        // been provided,
        // we expect that the users will be joined in the same voice conference.
        String webVoice = params.get(ApiParams.WEB_VOICE);
        if (StringUtils.isEmpty(webVoice)) {
            webVoice = telVoice;
        }

        // Get all the other relevant parameters and generate defaults if none
        // has been provided.
        String dialNumber = processDialNumber(params.get(ApiParams.DIAL_NUMBER));
        String logoutUrl = processLogoutUrl(params.get(ApiParams.LOGOUT_URL));
        boolean record = processRecordMeeting(params.get(ApiParams.RECORD));
        int maxUsers = processMaxUser(params.get(ApiParams.MAX_PARTICIPANTS));
        int meetingDuration = processMeetingDuration(params.get(ApiParams.DURATION));
        int logoutTimer = processLogoutTimer(params.get(ApiParams.LOGOUT_TIMER));
        
        // Banner parameters
        String bannerText = params.get(ApiParams.BANNER_TEXT);
        String bannerColor = params.get(ApiParams.BANNER_COLOR);
        
        // set is breakout room property
        boolean isBreakout = false;
        if (!StringUtils.isEmpty(params.get(ApiParams.IS_BREAKOUT))) {
            isBreakout = Boolean.valueOf(params.get(ApiParams.IS_BREAKOUT));
        }

        String welcomeMessageTemplate = processWelcomeMessage(
                params.get(ApiParams.WELCOME), isBreakout);
        String welcomeMessage = substituteKeywords(welcomeMessageTemplate,
                dialNumber, telVoice, meetingName);

        String internalMeetingId = convertToInternalMeetingId(externalMeetingId);

        // Check if this is a test meeting. NOTE: This should not belong here.
        // Extract this out.
        if (isTestMeeting(telVoice)) {
            internalMeetingId = getIntMeetingIdForTestMeeting(telVoice);
        }

        boolean autoStartRec = autoStartRecording;
        if (!StringUtils.isEmpty(params.get(ApiParams.AUTO_START_RECORDING))) {
            try {
                autoStartRec = Boolean.parseBoolean(params
                        .get(ApiParams.AUTO_START_RECORDING));
            } catch (Exception ex) {
                log.warn("Invalid param [autoStartRecording] for meeting=[{}]",
                        internalMeetingId);
            }
        }

        boolean allowStartStoptRec = allowStartStopRecording;
        if (!StringUtils.isEmpty(params.get(ApiParams.ALLOW_START_STOP_RECORDING))) {
            try {
                allowStartStoptRec = Boolean.parseBoolean(params
                        .get(ApiParams.ALLOW_START_STOP_RECORDING));
            } catch (Exception ex) {
                log.warn(
                        "Invalid param [allowStartStopRecording] for meeting=[{}]",
                        internalMeetingId);
            }
        }

        boolean webcamsOnlyForMod = webcamsOnlyForModerator;
        if (!StringUtils.isEmpty(params.get(ApiParams.WEBCAMS_ONLY_FOR_MODERATOR))) {
            try {
                webcamsOnlyForMod = Boolean.parseBoolean(params
                        .get(ApiParams.WEBCAMS_ONLY_FOR_MODERATOR));
            } catch (Exception ex) {
                log.warn(
                        "Invalid param [webcamsOnlyForModerator] for meeting=[{}]",
                        internalMeetingId);
            }
        }

        String guestPolicy = defaultGuestPolicy;
        if (!StringUtils.isEmpty(params.get(ApiParams.GUEST_POLICY))) {
        	guestPolicy = params.get(ApiParams.GUEST_POLICY);
		}
        
        // Collect metadata for this meeting that the third-party app wants to
        // store if meeting is recorded.
        Map<String, String> meetingInfo = processMetaParam(params);

        // Create a unique internal id by appending the current time. This way,
        // the 3rd-party
        // app can reuse the external meeting id.
        long createTime = System.currentTimeMillis();
        internalMeetingId = internalMeetingId + "-" + Long.toString(createTime);

        // If this create meeting request is for a breakout room, we just used
        // we need to generate a unique internal and external id and keep
        // tracks of the parent meeting id
        String parentMeetingId = "";
        if (isBreakout) {
            internalMeetingId = params.get(ApiParams.MEETING_ID);
            parentMeetingId = params.get(ApiParams.PARENT_MEETING_ID);
            // We rebuild the the external meeting using the has of the parent
            // meeting, the shared timestamp and the sequence number
            String timeStamp = StringUtils.substringAfter(internalMeetingId, "-");
            String externalHash = DigestUtils
                    .sha1Hex((parentMeetingId + "-" + timeStamp + "-" + params.get("sequence")));
            externalMeetingId = externalHash + "-" + timeStamp;
        }

        // Create the meeting with all passed in parameters.
        Meeting meeting = new Meeting.Builder(externalMeetingId,
                internalMeetingId, createTime).withName(meetingName)
                .withMaxUsers(maxUsers).withModeratorPass(modPass)
                .withViewerPass(viewerPass).withRecording(record)
                .withDuration(meetingDuration).withLogoutUrl(logoutUrl)
                .withLogoutTimer(logoutTimer)
                .withBannerText(bannerText).withBannerColor(bannerColor)
                .withTelVoice(telVoice).withWebVoice(webVoice)
                .withDialNumber(dialNumber)
                .withDefaultAvatarURL(defaultAvatarURL)
                .withAutoStartRecording(autoStartRec)
                .withAllowStartStopRecording(allowStartStoptRec)
                .withWebcamsOnlyForModerator(webcamsOnlyForMod)
                .withMetadata(meetingInfo)
                .withWelcomeMessageTemplate(welcomeMessageTemplate)
                .withWelcomeMessage(welcomeMessage).isBreakout(isBreakout)
                .withGuestPolicy(guestPolicy)
                .build();

        String configXML = getDefaultConfigXML();
        meeting.storeConfig(true, configXML);

        if (!StringUtils.isEmpty(params.get(ApiParams.MODERATOR_ONLY_MESSAGE))) {
            String moderatorOnlyMessageTemplate = params.get("moderatorOnlyMessage");
            String moderatorOnlyMessage = substituteKeywords(moderatorOnlyMessageTemplate,
                    dialNumber, telVoice, meetingName);
            meeting.setModeratorOnlyMessage(moderatorOnlyMessage);
        }

        meeting.setMaxInactivityTimeoutMinutes(maxInactivityTimeoutMinutes);
        meeting.setWarnMinutesBeforeMax(warnMinutesBeforeMax);
        meeting.setMeetingExpireIfNoUserJoinedInMinutes(meetingExpireIfNoUserJoinedInMinutes);
				meeting.setMeetingExpireWhenLastUserLeftInMinutes(meetingExpireWhenLastUserLeftInMinutes);

        // Add extra parameters for breakout room
        if (isBreakout) {
            meeting.setSequence(Integer.parseInt(params.get("sequence")));
            meeting.setFreeJoin(Boolean.parseBoolean(params.get("freeJoin")));
            meeting.setParentMeetingId(parentMeetingId);
        }

		if (!StringUtils.isEmpty(params.get("logo"))) {
			meeting.setCustomLogoURL(params.get("logo"));
		}

		if (!StringUtils.isEmpty(params.get("copyright"))) {
			meeting.setCustomCopyright(params.get("copyright"));
		}
		Boolean muteOnStart = defaultMuteOnStart;
		if (!StringUtils.isEmpty(params.get("muteOnStart"))) {
        	muteOnStart = Boolean.parseBoolean(params.get("muteOnStart"));
        }

		meeting.setMuteOnStart(muteOnStart);
        return meeting;
    }
	
	public String getApiVersion() {
		return apiVersion;
	}
	
	public boolean isServiceEnabled() {
		return serviceEnabled;
	}
	
	public String getDefaultClientUrl() {
		return defaultClientUrl;
	}

	public String getDefaultGuestWaitURL() {
		return defaultGuestWaitURL;
        }

	public String getHTML5ClientUrl() {
		return html5ClientUrl;
	}

	public Boolean getAttendeesJoinViaHTML5Client() {
		return attendeesJoinViaHTML5Client;
	}

	public Boolean getModeratorsJoinViaHTML5Client() {
		return moderatorsJoinViaHTML5Client;
	}

	public String getDefaultConfigXML() {
		defaultConfigXML = getConfig(defaultConfigURL);
		
		return defaultConfigXML;
	}
	
	private String getConfig(String url) {
		String configXML = "";

		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			HttpGet httpget = new HttpGet(url);

			// Create a custom response handler
			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

				@Override
				public String handleResponse(
						final HttpResponse response) throws IOException {
					int status = response.getStatusLine().getStatusCode();
					if (status >= 200 && status < 300) {
						HttpEntity entity = response.getEntity();
						return entity != null ? EntityUtils.toString(entity, StandardCharsets.UTF_8) : null;
					} else {
						throw new ClientProtocolException("Unexpected response status: " + status);
					}
				}
			};

			String responseBody = httpclient.execute(httpget, responseHandler);
			configXML = responseBody;
		} catch(IOException ex) {
			// IOException
		} finally {
			try {
				httpclient.close();
			} catch(IOException ex) {
				// do nothing
			}
		}

		return configXML;
	  }
	
	public String getDefaultConfigURL() {
		return defaultConfigURL;
	}
	
	public String getDefaultLogoutUrl() {
		 if ((StringUtils.isEmpty(defaultLogoutUrl)) || "default".equalsIgnoreCase(defaultLogoutUrl)) {
     		return defaultServerUrl;
     	} else {
     		return defaultLogoutUrl;
     	}
	}
	
    public String processWelcomeMessage(String message, Boolean isBreakout) {
        String welcomeMessage = message;
        if (StringUtils.isEmpty(message)) {
            welcomeMessage = defaultWelcomeMessage;
        }
        if (!StringUtils.isEmpty(defaultWelcomeMessageFooter) && !isBreakout)
            welcomeMessage += "<br><br>" + defaultWelcomeMessageFooter;
        return welcomeMessage;
    }

	public String convertToInternalMeetingId(String extMeetingId) {
		return DigestUtils.sha1Hex(extMeetingId);
	}
	
	public String processPassword(String pass) {
		return StringUtils.isEmpty(pass) ? RandomStringUtils.randomAlphanumeric(8) : pass;
	}

	public boolean hasChecksumAndQueryString(String checksum, String queryString) {
		return (! StringUtils.isEmpty(checksum) && StringUtils.isEmpty(queryString));
	}
		
	public String processTelVoice(String telNum) {
		return StringUtils.isEmpty(telNum) ? RandomStringUtils.randomNumeric(defaultNumDigitsForTelVoice) : telNum;
	}
		
	public String processDialNumber(String dial) {
		return StringUtils.isEmpty(dial) ? defaultDialAccessNumber : dial;	
	}
	
	public String processLogoutUrl(String logoutUrl) {
		if (StringUtils.isEmpty(logoutUrl)) {
	        if ((StringUtils.isEmpty(defaultLogoutUrl)) || "default".equalsIgnoreCase(defaultLogoutUrl)) {
        		return defaultServerUrl;
        	} else {
        		return defaultLogoutUrl;
        	}	
		}
		
		return logoutUrl;
	}
	
	public boolean processRecordMeeting(String record) {
		// The administrator has turned off recording for all meetings.
		if (disableRecordingDefault) {
			log.info("Recording is turned OFF by default.");
			return false;
		}
		
		boolean rec = false;			
		if(! StringUtils.isEmpty(record)){
			try {
				rec = Boolean.parseBoolean(record);
			} catch(Exception ex){ 
				rec = false;
			}
		}
		
		return rec;
	}
		
	public int processMaxUser(String maxUsers) {
		int mUsers = -1;
		
		try {
			mUsers = Integer.parseInt(maxUsers);
		} catch(Exception ex) { 
			mUsers = defaultMaxUsers;
		}		
		
		return mUsers;
	}	

  public int processMeetingDuration(String duration) {
    int mDuration = -1;
    
    try {
      mDuration = Integer.parseInt(duration);
    } catch(Exception ex) { 
      mDuration = defaultMeetingDuration;
    }   
    
    return mDuration;
  }

	public int processLogoutTimer(String logoutTimer) {
		int mDuration = clientLogoutTimerInMinutes;

		try {
			mDuration = Integer.parseInt(logoutTimer);
		} catch(Exception ex) {
			mDuration = clientLogoutTimerInMinutes;
		}

		return mDuration;
	}

	public boolean isTestMeeting(String telVoice) {	
		return ((! StringUtils.isEmpty(telVoice)) && 
				(! StringUtils.isEmpty(testVoiceBridge)) && 
				(telVoice == testVoiceBridge));	
	}
		
	public String getIntMeetingIdForTestMeeting(String telVoice) {		
		if ((testVoiceBridge != null) && (telVoice == testVoiceBridge)) {
			if (StringUtils.isEmpty(testConferenceMock)) 
				return testConferenceMock;
		} 
		
		return "";	
	}
	
	public boolean isConfigXMLChecksumSame(String meetingID, String configXML, String checksum) {
		if (StringUtils.isEmpty(securitySalt)) {
			log.warn("Security is disabled in this service. Make sure this is intentional.");
			return true;
		}

		log.info("CONFIGXML CHECKSUM={} length={}", checksum, checksum.length());

		String data = meetingID + configXML + securitySalt;
		String cs = DigestUtils.sha1Hex(data);
		if (checksum.length() == 64) {
			cs = DigestUtils.sha256Hex(data);
			log.info("CONFIGXML SHA256 {}", cs);
		}

		if (cs == null || !cs.equals(checksum)) {
			log.info("checksumError: configXML checksum. our: [{}], client: [{}]", cs, checksum);
			return false;
		}
		return true;
	}
	
	public boolean isChecksumSame(String apiCall, String checksum, String queryString) {
		if (StringUtils.isEmpty(securitySalt)) {
			log.warn("Security is disabled in this service. Make sure this is intentional.");
			return true;
		}

		if( queryString == null ) {
		    queryString = "";
		} else {
		    // handle either checksum as first or middle / end parameter
		    // TODO: this is hackish - should be done better
		    queryString = queryString.replace("&checksum=" + checksum, "");
		    queryString = queryString.replace("checksum=" + checksum + "&", "");
		    queryString = queryString.replace("checksum=" + checksum, "");
		}

		log.info("CHECKSUM={} length={}", checksum, checksum.length());

		String data = apiCall + queryString + securitySalt;
		String cs = DigestUtils.sha1Hex(data);
		if (checksum.length() == 64) {
			cs = DigestUtils.sha256Hex(data);
			log.info("SHA256 {}", cs);
		}
		if (cs == null || cs.equals(checksum) == false) {
			log.info("query string after checksum removed: [{}]", queryString);
			log.info("checksumError: query string checksum failed. our: [{}], client: [{}]", cs, checksum);
			return false;
		}

		return true; 
	}
	
	public boolean isPostChecksumSame(String apiCall, HashMap<String, String[]> params) {
		if (StringUtils.isEmpty(securitySalt)) {
			log.warn("Security is disabled in this service. Make sure this is intentional.");
			return true;
		}

		StringBuilder csbuf = new StringBuilder();
		csbuf.append(apiCall);
 
		SortedSet<String> keys = new TreeSet<>(params.keySet());
 
		boolean first = true;
		String checksum = null;
		for (String key: keys) {
			if (ApiParams.CHECKSUM.equals(key)) {
				// Don't include the "checksum" parameter in the checksum
				checksum = params.get(key)[0];
				continue;
			}
 
			for (String value: params.get(key)) {
				if (first) {
					first = false;
				} else {
					csbuf.append("&");
				}
				csbuf.append(key);
				csbuf.append("=");
				String encResult;

				encResult = value;
				
/*****
 * Seems like Grails 2.3.6 decodes the string. So we need to re-encode it.
 * We'll remove this later. richard (aug 5, 2014)						
*/				try {       
					// we need to re-encode the values because Grails unencoded it
					// when it received the 'POST'ed data. Might not need to do in a GET request.
					encResult = URLEncoder.encode(value, StandardCharsets.UTF_8.name());  
				} catch (UnsupportedEncodingException e) {       
					encResult = value;     
				} 					

				csbuf.append(encResult);
			}
		}
		csbuf.append(securitySalt);

		String baseString = csbuf.toString();				
		String cs = DigestUtils.sha1Hex(baseString);
		
		if (cs == null || !cs.equals(checksum)) {
			log.info("POST basestring = {}", baseString);
			log.info("checksumError: failed checksum. our checksum: [{}], client: [{}]", cs, checksum);
			return false;
		}

		return true;
	}

	/*************************************************
	 * Setters
	 ************************************************/
	
	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	public void setServiceEnabled(boolean e) {
		serviceEnabled = e;
	}
	
	public void setSecuritySalt(String securitySalt) {
		this.securitySalt = securitySalt;
	}

	public void setDefaultMaxUsers(int defaultMaxUsers) {
		this.defaultMaxUsers = defaultMaxUsers;
	}

	public void setDefaultWelcomeMessage(String defaultWelcomeMessage) {
		this.defaultWelcomeMessage = defaultWelcomeMessage;
	}
	
	public void setDefaultWelcomeMessageFooter(String defaultWelcomeMessageFooter) {
	    this.defaultWelcomeMessageFooter = defaultWelcomeMessageFooter;
	}

	public void setDefaultDialAccessNumber(String defaultDialAccessNumber) {
		this.defaultDialAccessNumber = defaultDialAccessNumber;
	}

	public void setTestVoiceBridge(String testVoiceBridge) {
		this.testVoiceBridge = testVoiceBridge;
	}

	public void setTestConferenceMock(String testConferenceMock) {
		this.testConferenceMock = testConferenceMock;
	}

	public void setDefaultLogoutUrl(String defaultLogoutUrl) {
		this.defaultLogoutUrl = defaultLogoutUrl;
	}

	public void setDefaultConfigURL(String defaultConfigUrl) {
		this.defaultConfigURL = defaultConfigUrl;
	}
	
	public void setDefaultServerUrl(String defaultServerUrl) {
		this.defaultServerUrl = defaultServerUrl;
	}

	public void setDefaultNumDigitsForTelVoice(int defaultNumDigitsForTelVoice) {
		this.defaultNumDigitsForTelVoice = defaultNumDigitsForTelVoice;
	}

	public void setDefaultClientUrl(String defaultClientUrl) {
		this.defaultClientUrl = defaultClientUrl;
	}

	public void setDefaultGuestWaitURL(String url) {
		this.defaultGuestWaitURL = url;
        }

	public void setHtml5ClientUrl(String html5ClientUrl) {
		this.html5ClientUrl = html5ClientUrl;
	}

	public void setModeratorsJoinViaHTML5Client(Boolean moderatorsJoinViaHTML5Client) {
		this.moderatorsJoinViaHTML5Client = moderatorsJoinViaHTML5Client;
	}

	public void setAttendeesJoinViaHTML5Client(Boolean attendeesJoinViaHTML5Client) {
		this.attendeesJoinViaHTML5Client = attendeesJoinViaHTML5Client;
	}

	public void setDefaultMeetingDuration(int defaultMeetingDuration) {
		this.defaultMeetingDuration = defaultMeetingDuration;
	}

	public void setDisableRecordingDefault(boolean disabled) {
		this.disableRecordingDefault = disabled;
	}
	
	public void setAutoStartRecording(boolean start) {
		this.autoStartRecording = start;
	}

    public void setAllowStartStopRecording(boolean allowStartStopRecording) {
        this.allowStartStopRecording = allowStartStopRecording;
    }
	
    public void setWebcamsOnlyForModerator(boolean webcamsOnlyForModerator) {
        this.webcamsOnlyForModerator = webcamsOnlyForModerator;
    }
	
	public void setdefaultAvatarURL(String url) {
		this.defaultAvatarURL = url;
	}

	public void setDefaultGuestPolicy(String guestPolicy) {
		this.defaultGuestPolicy =  guestPolicy;
	}

	public void setMaxInactivityTimeoutMinutes(Integer value) {
		maxInactivityTimeoutMinutes = value;
	}

	public void setClientLogoutTimerInMinutes(Integer value) {
		clientLogoutTimerInMinutes = value;
	}

	public void setWarnMinutesBeforeMax(Integer value) {
		warnMinutesBeforeMax = value;
	}

	public Integer getMaxInactivityTimeoutMinutes() {
		return maxInactivityTimeoutMinutes;
	}

	public Integer getWarnMinutesBeforeMax() {
		return warnMinutesBeforeMax;
	}


	public void setMeetingExpireWhenLastUserLeftInMinutes(Integer value) {
		meetingExpireWhenLastUserLeftInMinutes = value;
	}

	public Integer getmeetingExpireWhenLastUserLeftInMinutes() {
		return meetingExpireWhenLastUserLeftInMinutes;
	}

	public void setMeetingExpireIfNoUserJoinedInMinutes(Integer value) {
		meetingExpireIfNoUserJoinedInMinutes = value;
	}

	public void setMaxPresentationFileUpload(Long maxFileSize) {
		maxPresentationFileUpload = maxFileSize;
	}

	public Long getMaxPresentationFileUpload() {
		return maxPresentationFileUpload;
	}

	public void setMuteOnStart(Boolean mute) {
		defaultMuteOnStart = mute;
	}

	public Boolean getMuteOnStart() {
		return defaultMuteOnStart;
	}


	public List<String> decodeIds(String encodeid) {
		ArrayList<String> ids=new ArrayList<>();
		try {
			ids.addAll(Arrays.asList(URLDecoder.decode(encodeid, StandardCharsets.UTF_8.name()).split(URLDECODER_SEPARATOR)));
		} catch (UnsupportedEncodingException e) {
			log.error("Couldn't decode the IDs");
		}
		
		return ids;
	}

    public List<String> convertToInternalMeetingId(List<String> extMeetingIds) {
        ArrayList<String> internalMeetingIds = new ArrayList<>();
        for (String extid : extMeetingIds) {
            internalMeetingIds.add(convertToInternalMeetingId(extid));
        }
        return internalMeetingIds;
    }
	
    public Map<String, String> getUserCustomData(Map<String, String> params) {
        Map<String, String> resp = new HashMap<>();

        for (String key : params.keySet()) {
            if (key.contains("userdata") && key.indexOf("userdata") == 0) {
                String[] userdata = key.split("-");
                if (userdata.length == 2) {
                    log.debug("Got user custom data {} = {}", key, params.get(key));
                    resp.put(userdata[1], params.get(key));
                }
            }
        }

        return resp;
    }

	public Map<String, Map<String, Object>> decodeFilters(String encodedFilters) {
        Map<String, Map<String, Object>> filters = new LinkedHashMap<>();

        try {
            String[] sFilters = encodedFilters.split(URLDECODER_SEPARATOR);
            for( String sFilter: sFilters) {
                String[] filterElements = sFilter.split(FILTERDECODER_SEPARATOR_ELEMENTS, 3);
                Map<String, Object> filter = new LinkedHashMap<>();
                filter.put("op", filterElements[1]);
                String[] fValues = filterElements[2].split(FILTERDECODER_SEPARATOR_OPERATORS);
                filter.put("values", fValues );
                filters.put(filterElements[0], filter);
            }
        } catch (Exception e) {
            log.error("Couldn't decode the filters");
        }

        return filters;
    }

}
