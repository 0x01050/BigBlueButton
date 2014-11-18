# --------------------------------------------------------------------------------------------
# Public methods on server
# All these method must first authenticate the user before it calls the private function counterpart below
# which sends the request to bbbApps. If the method is modifying the media the current user is sharing,
# you should perform the request before sending the request to bbbApps. This allows the user request to be performed
# immediately, since they do not require permission for things such as muting themsevles. 
# --------------------------------------------------------------------------------------------
Meteor.methods
  # meetingId: the meetingId of the meeting the user[s] is in
  # toMuteUserId: the userId of the user to be [un]muted
  # requesterUserId: the userId of the requester
  # requesterSecret: the userSecret of the requester
  # mutedBoolean: true for muting, false for unmuting
  muteUser: (meetingId, toMuteUserId, requesterUserId, requesterSecret, mutedBoolean) ->
    action = ->
      if mutedBoolean
        if toMuteUserId is requesterUserId
          return 'muteSelf'
        else
          return 'muteOther'
      else
        if toMuteUserId is requesterUserId
          return 'unmuteSelf'
        else
          return 'unmuteOther'

    if isAllowedTo(action(), meetingId, requesterUserId, requesterSecret)
      message =
        payload:
          userid: toMuteUserId
          meeting_id: meetingId
          mute: mutedBoolean
          requester_id: requesterUserId
        header:
          timestamp: new Date().getTime()
          name: "mute_user_request"
          version: "0.0.1"

      Meteor.log.info "publishing a user mute #{mutedBoolean} request for #{toMuteUserId}"

      publish Meteor.config.redis.channels.toBBBApps.voice, message
      updateVoiceUser meetingId, {'web_userid': toMuteUserId, talking:false, muted:mutedBoolean}
    return

  # meetingId: the meetingId which both users are in 
  # toLowerUserId: the userid of the user to have their hand lowered
  # loweredByUserId: userId of person lowering
  # loweredBySecret: the secret of the requestor
  userLowerHand: (meetingId, toLowerUserId, loweredByUserId, loweredBySecret) ->
    action = ->
      if toLowerUserId is loweredByUserId
        return 'lowerOwnHand'
      else
        return 'lowerOthersHand'

    if isAllowedTo(action(), meetingId, loweredByUserId, loweredBySecret)
      message =
        payload:
          userid: toLowerUserId
          meeting_id: meetingId
          raise_hand: false
          lowered_by: loweredByUserId
        header:
          timestamp: new Date().getTime()
          name: "user_lowered_hand_message"
          version: "0.0.1"

      # publish to pubsub
      publish Meteor.config.redis.channels.toBBBApps.users, message
    return

  # meetingId: the meetingId which both users are in 
  # toRaiseUserId: the userid of the user to have their hand lowered
  # raisedByUserId: userId of person lowering
  # raisedBySecret: the secret of the requestor
  userRaiseHand: (meetingId, toRaiseUserId, raisedByUserId, raisedBySecret) ->
    action = ->
      if toRaiseUserId is raisedByUserId
        return 'raiseOwnHand'
      else
        return 'raiseOthersHand'

    if isAllowedTo(action(), meetingId, raisedByUserId, raisedBySecret)
      message =
        payload:
          userid: toRaiseUserId
          meeting_id: meetingId
          raise_hand: false
          lowered_by: raisedByUserId
        header:
          timestamp: new Date().getTime()
          name: "user_raised_hand_message"
          version: "0.0.1"

      # publish to pubsub
      publish Meteor.config.redis.channels.toBBBApps.users, message
    return

  # meetingId: the meeting where the user is
  # userId: the userid of the user logging out
  # userSecret: the authentication string of the user
  userLogout: (meetingId, userId, userSecret) ->
    if isAllowedTo('logoutSelf', meetingId, userId, userSecret)
      Meteor.log.info "a user is logging out from #{meetingId}:" + userId
      requestUserLeaving meetingId, userId

# --------------------------------------------------------------------------------------------
# Private methods on server
# --------------------------------------------------------------------------------------------

# Only callable from server
# Received information from BBB-Apps that a user left
# Need to update the collection
# params: meetingid, userid as defined in BBB-Apps
@removeUserFromMeeting = (meetingId, userId) ->
  u = Meteor.Users.findOne({'meetingId': meetingId, 'userId': userId})
  if u?
    Meteor.Users.remove(u._id)
    Meteor.log.info "----removed user[" + userId + "] from " + meetingId
  else
    Meteor.log.info "did not find a user [userId] to delete in meetingid:#{meetingId}"

# Corresponds to a valid action on the HTML clientside
# After authorization, publish a user_leaving_request in redis
# params: meetingid, userid as defined in BBB-App
@requestUserLeaving = (meetingId, userId) ->
  if Meteor.Users.findOne({'meetingId': meetingId, 'userId': userId})?
    message =
      payload:
        meeting_id: meetingId
        userid: userId
      header:
        timestamp: new Date().getTime()
        name: "user_leaving_request"
        version: "0.0.1"

    if userId? and meetingId?
      Meteor.log.info "sending a user_leaving_request for #{meetingId}:#{userId}"
      publish Meteor.config.redis.channels.toBBBApps.users, message
    else
      Meteor.log.info "did not have enough information to send a user_leaving_request"

#update a voiceUser - a helper method
@updateVoiceUser = (meetingId, voiceUserObject) ->
  u = Meteor.Users.findOne userId: voiceUserObject.web_userid
  if u?
    if voiceUserObject.talking?
      Meteor.Users.update({meetingId: meetingId ,userId: voiceUserObject.web_userid}, {$set: {'user.voiceUser.talking':voiceUserObject.talking}}, {multi: false}) # talking
    if voiceUserObject.joined?
      Meteor.Users.update({meetingId: meetingId ,userId: voiceUserObject.web_userid}, {$set: {'user.voiceUser.joined':voiceUserObject.joined}}, {multi: false}) # joined
    if voiceUserObject.locked?
      Meteor.Users.update({meetingId: meetingId ,userId: voiceUserObject.web_userid}, {$set: {'user.voiceUser.locked':voiceUserObject.locked}}, {multi: false}) # locked
    if voiceUserObject.muted?
      Meteor.Users.update({meetingId: meetingId ,userId: voiceUserObject.web_userid}, {$set: {'user.voiceUser.muted':voiceUserObject.muted}}, {multi: false}) # muted
    if voiceUserObject.listenOnly?
      Meteor.Users.update({meetingId: meetingId ,userId: voiceUserObject.web_userid}, {$set: {'user.listenOnly':voiceUserObject.listenOnly}}, {multi: false}) # muted
  else
    Meteor.log.info "ERROR! did not find such voiceUser!"

@addUserToCollection = (meetingId, user) ->
  userId = user.userid
  #check if the user is already in the meeting
  unless Meteor.Users.findOne({userId:userId, meetingId: meetingId})?
    entry =
      meetingId: meetingId
      userId: userId
      userSecret: Math.random().toString(36).substring(2,13)
      user:
        userid: user.userid
        presenter: user.presenter
        name: user.name
        phone_user: user.phone_user
        raise_hand: user.raise_hand
        has_stream: user.has_stream
        role: user.role
        listenOnly: user.listenOnly
        extern_userid: user.extern_userid
        permissions: user.permissions
        locked: user.locked
        time_of_joining: user.timeOfJoining
        connection_status: "" # TODO consider other default value
        voiceUser:
          web_userid: user.voiceUser.web_userid
          callernum: user.voiceUser.callernum
          userid: user.voiceUser.userid
          talking: user.voiceUser.talking
          joined: user.voiceUser.joined
          callername: user.voiceUser.callername
          locked: user.voiceUser.locked
          muted: user.voiceUser.muted
        webcam_stream: user.webcam_stream

    id = Meteor.Users.insert(entry)
    Meteor.log.info "added user userSecret=#{entry.userSecret} id=[#{id}]:#{user.name}. Users.size is now #{Meteor.Users.find({meetingId: meetingId}).count()}"
