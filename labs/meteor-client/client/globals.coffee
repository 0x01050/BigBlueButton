Handlebars.registerHelper 'equals', (a, b) -> # equals operator was dropped in Meteor's migration from Handlebars to Spacebars
  a is b

# Allow access through all templates
Handlebars.registerHelper "setInSession", (k, v) -> SessionAmplify.set k, v 
Handlebars.registerHelper "getInSession", (k) -> SessionAmplify.get k
# Allow access throughout all coffeescript/js files
@setInSession = (k, v) -> SessionAmplify.set k, v 
@getInSession = (k) -> SessionAmplify.get k

# retrieve account for selected user
@getCurrentUserFromSession = ->
  Meteor.Users.findOne("userId": getInSession("userId"))

# retrieve account for selected user
Handlebars.registerHelper "getCurrentUser", =>
	@window.getCurrentUserFromSession()

# toggle state of field in the database
@toggleCam = (event) ->
	# Meteor.Users.update {_id: context._id} , {$set:{"user.sharingVideo": !context.sharingVideo}}
  # Meteor.call('userToggleCam', context._id, !context.sharingVideo)

@toggleMic = (event) -> 
  if getInSession "isSharingAudio"
    callback = -> 
      setInSession "isSharingAudio", false # update to no longer sharing
      console.log "left voice conference"
      # sometimes we can hangup before the message that the user stopped talking is received so lets set it manually, otherwise they might leave the audio call but still be registered as talking
      # Meteor.call("hangupUser")
    webrtc_hangup callback # sign out of call
  else
    # create voice call params
    username = "#{getInSession("userId")}-bbbID-#{getUsersName()}"
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
    setInSession("userId", userId)
    setInSession("meetingId", meetingId)
    setInSession("currentChatId", meetingId)
    setInSession("meetingName", null)
    setInSession("bbbServerVersion", "0.90")
    setInSession("userName", null) 
    setInSession("validUser", true) # got info from server, user is a valid user

@getUsersName = ->
  name = getInSession("userName") # check if we actually have one in the session
  if name? then name # great return it, no database query
  else # we need it from the database
    user = Meteor.Users.findOne({'userId': getInSession("userId")})
    if user?.user?.name
      setInSession "userName", user.user.name # store in session for fast access next time
      user.user.name
    else null

@getMeetingName = ->
  meetName = getInSession("meetingName") # check if we actually have one in the session
  if meetName? then meetName # great return it, no database query
  else # we need it from the database
    meet = Meteor.Meetings.findOne({})
    if meet?.meetingName
      setInSession "meetingName", meet?.meetingName # store in session for fast access next time
      meet?.meetingName
    else null

Handlebars.registerHelper "getMeetingName", ->
  window.getMeetingName()

Handlebars.registerHelper "isUserSharingVideo", (u) ->
  u.webcam_stream.length isnt 0

Handlebars.registerHelper "isCurrentUser", (id) ->
  id is getInSession("userId")

# retrieves all users in the meeting
Handlebars.registerHelper "getUsersInMeeting", ->
  Meteor.Users.find({})

@getTime = -> # returns epoch in ms
  (new Date).valueOf()

Handlebars.registerHelper "isUserTalking", (u) ->
  console.log "inside isUserTalking"
  console.log u
  if u?
    u.voiceUser?.talking
  else
    return false

Handlebars.registerHelper "isUserSharingAudio", ->
  getInSession "isSharingAudio"

# Starts the entire logout procedure. Can be called for signout out or kicking out
# meeting: the meeting the user is in
# the user's userId
# selfSignout: if true, the user is logging themselves out and we should invalidate and redirect them, if false a user is being kicked out and should be notified
@userLogout = (meeting, user, selfSignout=true) ->
  Meteor.call("userLogout", meeting, user)
  if selfSignout
    invalidateUser(meeting, user)

# Clear the local user session and redirect them away
@invalidateUser = (meeting, user) ->
  # wipe important session data
  setInSession("userId", null)
  setInSession("meetingId", null)
  setInSession("currentChatId", null)
  setInSession("meetingName", null)
  setInSession("bbbServerVersion", null)
  setInSession("userName", null) 
  setInSession "display_navbar", false # needed to hide navbar when the layout template renders
  # navigate to logout
  Router.go('logout')
