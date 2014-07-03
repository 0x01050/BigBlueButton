Meteor.methods
  validate: (meetingId, userId, authToken) ->
    Meteor.redisPubSub.sendValidateToken(meetingId, userId, authToken)

  userLogout: (meetingId, userId) ->
    console.log "a user is logging out:" + userId
    #remove from the collection
    Meteor.call("removeUserFromCollection", meetingId, userId)

    #dispatch a message to redis
    Meteor.redisPubSub.sendUserLeavingRequest(meetingId, userId)

  publishChatMessage: (meetingId, messageObject) ->
    Meteor.redisPubSub.publishingChatMessage(meetingId, messageObject)


class Meteor.RedisPubSub
  constructor: (callback) ->
    console.log "constructor RedisPubSub"

    @pubClient = redis.createClient()
    @subClient = redis.createClient()
        
    console.log("RPC: Subscribing message on channel: #{Meteor.config.redis.channels.fromBBBApps}")

    #log.info      
    @subClient.on "psubscribe", Meteor.bindEnvironment(@_onSubscribe)
    @subClient.on "pmessage", Meteor.bindEnvironment(@_onMessage)

    @subClient.psubscribe(Meteor.config.redis.channels.fromBBBApps)
    callback @

  # Construct and send a message to bbb-web to validate the user
  sendValidateToken: (meetingId, userId, authToken) ->
    console.log "\n\n i am sending a validate_auth_token with " + userId + "" + meetingId

    message = {
      "payload": {
        "auth_token": authToken
        "userid": userId
        "meeting_id": meetingId
      },
      "header": {
        "timestamp": new Date().getTime()
        "reply_to": meetingId + "/" + userId
        "name": "validate_auth_token"
      }
    }
    if authToken? and userId? and meetingId?
      @pubClient.publish(Meteor.config.redis.channels.toBBBApps.meeting, JSON.stringify(message))
    else
      console.log "did not have enough information to send a validate_auth_token message"

  sendUserLeavingRequest: (meetingId, userId) ->
    console.log "\n\n sending a user_leaving_request for #{meetingId}:#{userId}"
    message = {
      "payload": {
        "meeting_id": meetingId
        "userid": userId
      },
      "header": {
        "timestamp": new Date().getTime()
        "name": "user_leaving_request"
        "version": "0.0.1"
      }
    }
    if userId? and meetingId?
      @pubClient.publish(Meteor.config.redis.channels.toBBBApps.users, JSON.stringify(message))
    else
      console.log "did not have enough information to send a user_leaving_request"

  _onSubscribe: (channel, count) =>
    console.log "Subscribed to #{channel}"
    @invokeGetAllMeetingsRequest()

  _onMessage: (pattern, channel, jsonMsg) =>
    # TODO: this has to be in a try/catch block, otherwise the server will
    # crash if the message has a bad format

    message = JSON.parse(jsonMsg)
    correlationId = message.payload?.reply_to or message.header?.reply_to

    unless message.header?.name is "keep_alive_reply"
      #console.log "\nchannel=" + channel
      #console.log "correlationId=" + correlationId if correlationId?
      console.log "eventType=" + message.header?.name #+ "\n"
      #log.debug({ pattern: pattern, channel: channel, message: message}, "Received a message from redis")
      console.log jsonMsg

    if message.header?.name is "get_all_meetings_reply"
      console.log "Let's store some data for the running meetings so that 
      when an HTML5 client joins everything is ready!"
      listOfMeetings = message.payload?.meetings
      for meeting in listOfMeetings
        Meteor.call("addMeetingToCollection", meeting.meetingID, meeting.meetingName, meeting.recorded)

    if message.header?.name is "get_users_reply" and message.payload?.requester_id is "nodeJSapp"
      unless Meteor.Meetings.findOne({MeetingId: message.payload?.meeting_id})?
        meetingId = message.payload?.meeting_id
        users = message.payload?.users
        for user in users
          Meteor.call("addUserToCollection", meetingId, user)

    if message.header?.name is "user_joined_message"
      meetingId = message.payload.meeting_id
      user = message.payload.user
      Meteor.call("addUserToCollection", meetingId, user)

    if message.header?.name is "user_left_message"
      userId = message.payload?.user?.userid
      meetingId = message.payload?.meeting_id
      if userId? and meetingId?
        Meteor.call("removeUserFromCollection", meetingId, userId)

    if message.header?.name is "get_chat_history_reply" and message.payload?.requester_id is "nodeJSapp"
      unless Meteor.Meetings.findOne({MeetingId: message.payload?.meeting_id})?
        meetingId = message.payload?.meeting_id
        for chatMessage in message.payload?.chat_history
          Meteor.call("addChatToCollection", meetingId, chatMessage)

    if message.header?.name is "send_public_chat_message"
      messageObject = message.payload?.message
      meetingId = message.payload?.meeting_id
      Meteor.call("addChatToCollection", meetingId, messageObject)

    if message.header?.name is "meeting_created_message"
      # the event message contains very little info, so we will
      # request for information for all the meetings and in
      # this way can keep the Meetings collection up to date
      @invokeGetAllMeetingsRequest()

    if message.header?.name in ["meeting_ended_message", "meeting_destroyed_event", 
      "end_and_kick_all_message", "disconnect_all_users_message"]
      meetingId = message.payload?.meeting_id
      if Meteor.Meetings.findOne({meetingId: meetingId})?
        console.log "there are #{Meteor.Users.find({meetingId: meetingId}).count()} users in the meeting"
        for user in Meteor.Users.find({meetingId: meetingId}).fetch()
          console.log "jaja"
          Meteor.call("removeUserFromCollection", user.userId)
        unless message.header?.name is "disconnect_all_users_message"
          Meteor.call("removeMeetingFromCollection", meetingId)

  publish: (channel, message) ->
    console.log "Publishing channel=#{channel}, message=#{JSON.stringify(message)}"
    @pubClient.publish(channel, JSON.stringify(message), (err, res) ->
      console.log "err=" + err
      console.log "res=" + res
    )

  publishingChatMessage: (meetingId, chatObject) =>
    console.log "publishing a chat message to bbb-apps"
    message = {
      header : {
        "timestamp": new Date().getTime()
        "name": "send_public_chat_message_request"
      }
      payload: {
        "message" : chatObject
        "meeting_id": meetingId
        "requester_id": chatObject.from_userid
      }
    }
    console.log "publishing:" + JSON.stringify (message)
    @pubClient.publish(Meteor.config.redis.channels.toBBBApps.chat, JSON.stringify (message))

  invokeGetAllMeetingsRequest: =>
    #grab data about all active meetings on the server
    message = {
      "header": {
        "name": "get_all_meetings_request"
      }
      "payload": {
      }
    }
    @pubClient.publish(Meteor.config.redis.channels.toBBBApps.meeting, JSON.stringify (message))
