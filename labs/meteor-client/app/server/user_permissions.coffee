
moderator = null
presenter = null
viewer =
  # raising/lowering hand
  raiseOwnHand : true
  lowerOwnHand : true

  # muting
  muteSelf : true
  unmuteSelf : true

  logoutSelf : true

  #subscribing
  subscribeUsers: true
  subscribeChat: true

  #chat
  chatPublic: true #should make this dynamically modifiable later on
  chatPrivate: true #should make this dynamically modifiable later on

@isAllowedTo = (action, meetingId, userId, authToken) ->
  Meteor.log.info "in isAllowedTo: action-#{action}, userId=#{userId}, authToken=#{authToken}"

  user = Meteor.Users.findOne({meetingId:meetingId, userId: userId})
  if user?
    # we check if the user is who he claims to be
    if authToken is user.authToken
      if user.user?.role is 'VIEWER'
        return viewer[action] or false
    Meteor.log.warn "in meetingId=#{meetingId} userId=#{userId} tried to perform #{action} without permission" #TODO make this a warning
    Meteor.log.error "..while the authToken for user #{userId}(#{Meteor.Users.findOne({meetingId:meetingId, userId: userId})?.user?.name}) was #{Meteor.Users.findOne({meetingId:meetingId, userId: userId}).authToken}"

    # the current version of the HTML5 client represents only VIEWER users
  return false
