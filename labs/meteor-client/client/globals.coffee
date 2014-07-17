Handlebars.registerHelper 'equals', (a, b) -> # equals operator was dropped in Meteor's migration from Handlebars to Spacebars
  a is b

# Allow access through all templates
Handlebars.registerHelper "setInSession", (k, v) -> SessionAmplify.set k,v #Session.set k, v
Handlebars.registerHelper "getInSession", (k) -> SessionAmplify.get k #Session.get k
# Allow access throughout all coffeescript/js files
@setInSession = (k, v) -> SessionAmplify.set k,v #Session.set k, v
@getInSession = (k) -> SessionAmplify.get k

# retrieve account for selected user
@getCurrentUserFromSession = ->
  Meteor.Users.findOne("userId": SessionAmplify.get("userId"))

# retrieve account for selected user
Handlebars.registerHelper "getCurrentUser", =>
	# @window.getCurrentUserFromSession()
  id = SessionAmplify.get("userId")
  Meteor.Users.findOne("userId": SessionAmplify.get("userId"))

# toggle state of field in the database
@toggleCam = (event) ->
	# Meteor.Users.update {_id: context._id} , {$set:{"user.sharingVideo": !context.sharingVideo}}
  # Meteor.call('userToggleCam', context._id, !context.sharingVideo)

@toggleMic = (event) -> 
  if getInSession "isSharingAudio"
    callback = -> 
      setInSession "isSharingAudio", false # update to no longer sharing
      console.log "left voice conference"
    webrtc_hangup callback # sign out of call
  else
    # create voice call params
    username = "#{Session.get("userId")}-bbbID-#{getUsersName()}"
    voiceBridge = "70827"
    server = null
    callback = (message) -> 
      console.log JSON.stringify message
      setInSession "isSharingAudio", true
    webrtc_call(username, voiceBridge, server, callback) # make the call

# toggle state of session variable
@toggleUsersList = ->
	setInSession "display_usersList", !getInSession "display_usersList"

@toggleNavbar = ->
	setInSession "display_navbar", !getInSession "display_navbar"

@toggleChatbar = ->
	setInSession "display_chatbar", !getInSession "display_chatbar"

Meteor.methods
  sendMeetingInfoToClient: (meetingId, userId) ->
    console.log "inside sendMeetingInfoToClient"
    Session.set("userId", userId)
    Session.set("meetingId", meetingId)
    Session.set("currentChatId", meetingId)
    Session.set("meetingName", null)
    Session.set("bbbServerVersion", "0.90")
    Session.set("userName", null) 

    SessionAmplify.set("userId", userId)
    SessionAmplify.set("meetingId", meetingId)
    SessionAmplify.set("currentChatId", meetingId)
    SessionAmplify.set("meetingName", null)
    SessionAmplify.set("bbbServerVersion", "0.90")
    SessionAmplify.set("userName", null) 

@getUsersName = ->
  name = Session.get("userName") # check if we actually have one in the session
  if name? then name # great return it, no database query
  else # we need it from the database
    user = Meteor.Users.findOne({'userId': Session.get("userId")})
    if user?.user?.name
      Session.set "userName", user.user.name # store in session for fast access next time
      user.user.name
    else null

@getMeetingName = ->
  meetName = Session.get("meetingName") # check if we actually have one in the session
  if meetName? then meetName # great return it, no database query
  else # we need it from the database
    meet = Meteor.Meetings.findOne({})
    if meet?.meetingName
      Session.set "meetingName", meet?.meetingName # store in session for fast access next time
      meet?.meetingName
    else null

Handlebars.registerHelper "getMeetingName", ->
  window.getMeetingName()

Handlebars.registerHelper "isUserSharingAudio", (u) ->
  # u.voiceUser.talking
  getInSession "isSharingAudio"

Handlebars.registerHelper "isUserSharingVideo", (u) ->
  u.webcam_stream.length isnt 0

Handlebars.registerHelper "isCurrentUser", (id) ->
  id is Session.get "userId"

# retrieves all users in the meeting
Handlebars.registerHelper "getUsersInMeeting", ->
  Meteor.Users.find({})

@getTime = -> # returns epoch in ms
  (new Date).valueOf()
