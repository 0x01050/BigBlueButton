/**
* BigBlueButton open source conferencing system - http://www.bigbluebutton.org/
* 
* Copyright (c) 2018 BigBlueButton Inc. and by respective authors (see below).
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

package org.bigbluebutton.common2.redis;

import java.util.Map;

import org.bigbluebutton.common2.redis.commands.MeetingCommands;
import org.bigbluebutton.common2.redis.commands.RecordingCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.dynamic.RedisCommandFactory;

public class RedisStorageService extends RedisAwareCommunicator {

    private static Logger log = LoggerFactory.getLogger(RedisStorageService.class);

    private long keyExpiry;

    MeetingCommands meetingCommands;
    RecordingCommands recordingCommands;

    private StatefulRedisConnection<String, String> connection;

    public void start() {
        log.info("Starting RedisStorageService");
        RedisURI redisUri = RedisURI.Builder.redis(this.host, this.port).withClientName(this.clientName).build();
        // @todo Add password if provided
        // if (!this.password.isEmpty()) {
        // redisUri.setPassword(this.password);
        // }

        redisClient = RedisClient.create(redisUri);
        redisClient.setOptions(ClientOptions.builder().autoReconnect(true).build());

        connection = redisClient.connect();
        RedisCommandFactory factory = new RedisCommandFactory(connection);
        initCommands(factory);
    }

    private void initCommands(RedisCommandFactory factory) {
        meetingCommands = factory.getCommands(MeetingCommands.class);
        recordingCommands = factory.getCommands(RecordingCommands.class);
    }

    public void stop() {
        connection.close();
        redisClient.shutdown();
        log.info("RedisStorageService Stopped");
    }

    public void recordMeetingInfo(String meetingId, Map<String, String> info) {
        log.debug("Storing meeting {} metadata {}", meetingId, info);
        recordMeeting(Keys.MEETING_INFO + meetingId, info);
    }

    public void recordBreakoutInfo(String meetingId, Map<String, String> breakoutInfo) {
        log.debug("Saving breakout metadata in {}", meetingId);
        recordMeeting(Keys.BREAKOUT_MEETING + meetingId, breakoutInfo);
    }

    public void addBreakoutRoom(String parentId, String breakoutId) {
        log.debug("Saving breakout room for meeting {}", parentId);
        meetingCommands.addBreakoutRooms(Keys.BREAKOUT_ROOMS + parentId, breakoutId);
    }

    public void record(String meetingId, Map<String, String> event) {
        log.debug("Recording meeting event {} inside a transaction", meetingId);
        RedisCommands<String, String> commands = connection.sync();
        commands.multi();
        // @fixme increment Long msgid =
        // recordingCommands.incrementRecords("global:nextRecordedMsgId");
        Long msgid = 999L;
        recordingCommands.recordEventName("recording:" + meetingId + ":" + msgid, event);
        recordingCommands.recordEventValues("meeting:" + meetingId + ":" + "recordings", Long.toString(msgid));
        commands.exec();
    }

    // @fixme: not used anywhere
    public void removeMeeting(String meetingId) {
        log.debug("Removing meeting meeting {} inside a transaction", meetingId);
        RedisCommands<String, String> commands = connection.sync();
        commands.multi();
        meetingCommands.deleteMeeting(Keys.MEETING + meetingId);
        meetingCommands.deleteMeetings(Keys.MEETINGS + meetingId);
        commands.exec();
    }

    public void recordAndExpire(String meetingId, Map<String, String> event) {
        log.debug("Recording meeting event {} inside a transaction", meetingId);
        RedisCommands<String, String> commands = connection.sync();
        commands.multi();

        // @fixme increment Long msgid =
        // recordingCommands.incrementRecords("global:nextRecordedMsgId");
        Long msgid = 999L;
        recordingCommands.recordEventName("recording:" + meetingId + ":" + msgid, event);
        recordingCommands.recordEventValues("meeting:" + meetingId + ":recordings", Long.toString(msgid));
        /**
         * We set the key to expire after 14 days as we are still recording the
         * event into redis even if the meeting is not recorded. (ralam sept 23,
         * 2015)
         */
        // @fixme buggy recordingCommands.expireKey("meeting:" + meetingId +
        // ":recordings", keyExpiry);
        recordingCommands.recordEventValues("meeting:" + meetingId + ":recordings", Long.toString(msgid));
        // @fixme buggy recordingCommands.expireKey("meeting:" + meetingId +
        // ":recordings", keyExpiry);
        commands.exec();
    }

    private String recordMeeting(String key, Map<String, String> info) {
        return meetingCommands.recordMeetingInfo(key, info);
    }

}
