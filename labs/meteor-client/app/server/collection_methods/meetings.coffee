# --------------------------------------------------------------------------------------------
# Private methods on server
# --------------------------------------------------------------------------------------------
@addMeetingToCollection = (meetingId, name, intendedForRecording, voiceConf, duration) ->
	#check if the meeting is already in the collection
	unless Meteor.Meetings.findOne({meetingId: meetingId})?
		currentlyBeingRecorded = false # defaut value
		id = Meteor.Meetings.insert(
			meetingId: meetingId,
			meetingName: name,
			intendedForRecording: intendedForRecording,
			currentlyBeingRecorded: currentlyBeingRecorded,
			voiceConf: voiceConf,
			duration: duration)
		Meteor.log.info "added meeting _id=[#{id}]:meetingId=[#{meetingId}]:name=[#{name}]:duration=[#{duration}]:voiceConf=[#{voiceConf}].Meetings.size is now #{Meteor.Meetings.find().count()}"

@removeMeetingFromCollection = (meetingId) ->
	if Meteor.Meetings.findOne({meetingId: meetingId})?
		if Meteor.Users.find({meetingId: meetingId}).count() isnt 0
			Meteor.log.info "\n!!!!!removing a meeting which has active users in it!!!!\n"
		id = Meteor.Meetings.findOne({meetingId: meetingId})
		if id?
			Meteor.Meetings.remove(id._id)
			Meteor.log.info "removed from Meetings:#{meetingId} now there are only #{Meteor.Meetings.find().count()} meetings running"
# --------------------------------------------------------------------------------------------
# end Private methods on server
# --------------------------------------------------------------------------------------------
