Template.vertoDeskshareMenu.events
	"click .vertoButton": (event) ->
		$("#settingsModal").foundation('reveal', 'close')

	"click .screenshareShow": (event) ->
		$("#deskshareModal").foundation('reveal', 'open');

	"click .screenshareHide": (event) ->
		if(!!window["deskshareStream"])
			$("#webcam").src = null;
			window["deskshareStream"].stop();
		else
			screenStart(false, (->))
			console.log("ending simulation");
			simulatePresenterDeskshareHasEnded();

	"click #installChromeExtension": (event) ->
		# do a check for Chrome desksharing extension
		successCallback = ->
			setInSession("gotChromeExtension", true)
			location.reload()

		failureCallback = (error) ->
			setInSession("gotChromeExtension", false)
			console.error error

		installExtension = ->
			!!navigator.webkitGetUserMedia && !!window.chrome && !!chrome.webstore && !!chrome.webstore.install &&
			chrome.webstore.install(
				'https://chrome.google.com/webstore/detail/---',
				successCallback,
				failureCallback
			);
		installExtension()

Template.deskshareModal.events
	"click .screenshareStart": (event) ->
		$("#deskshareModal").foundation('reveal', 'close')
		screenStart(true, ((m)-> console.log(m)), "webcam")

	"click .screenshareStop": (event) ->
		$("#deskshareModal").foundation('reveal', 'close')
		screenStart(false, (->))

	"click #desksharePreview": (event) ->
		success = ->
			toggleWhiteboardVideo("video")
			setInSession("isPreviewingDeskshare", true)
		doDesksharePreview((-> success()), (->), "webcam");

	"click #stopDesksharePreview": (event) ->
		toggleWhiteboardVideo("whiteboard");
		setInSession("isPreviewingDeskshare", false);
		if(!!window["deskshareStream"])
			$("#webcam").src = null;
			window["deskshareStream"].stop();

Template.vertoWebcamMenu.events
	"click .vertoButton": (event) ->
		$("#settingsModal").foundation('reveal', 'close')

	"click .webcamShow": (event) ->
		$("#webcamModal").foundation('reveal', 'open');
		$("#webcamShow").hide()
		$("#webcamHide").show()

	"click .webcamHide": (event) ->
		if(!!window["webcamStream"])
			$("#webcam").src = null;
			window["webcamStream"].stop();

		$("#webcamShow").show()
		$("#webcamHide").hide()

Template.webcamModal.events
	"click .webcamStart": (event) ->
		$("#webcamModal").foundation('reveal', 'open');
		$("#webcamStart").hide()
		$("#webcamStop").show()

	"click .webcamStop": (event) ->
		$("#webcamStart").show()
		$("#webcamStop").hide()

	"click #webcamPreview": (event) ->
		doWebcamPreview((->), (->), "webcam");

	"click #getAdjustedResolutions": (event) ->
		getAdjustedResolutions (result) ->
			for i of result
				$("#adjustedResolutions").append(i + ": " + result[i].width + "x" + result[i].height + "<br/>")

@toggleWhiteboardVideo = (display) ->
	if display is "whiteboard"
		$("#webcam").css("display", "none")
		$("#whiteboard-container").css("display", "block")
		$("#whiteboard-paper").addClass("vertically-centered")
		scaleWhiteboard()
	else if display is "video"
		$("#whiteboard-container").css("display", "none")
		$("#webcam").css("display", "block")
		$("#webcam").css("width", "100%")
		$("#webcam").css("height", "100%")

# if remote deskshare has been ended disconnect and hide the video stream
@presenterDeskshareHasEnded = ->
	toggleWhiteboardVideo("whiteboard")
	exitVoiceCall()
	console.log("deskshare Ended")

# if remote deskshare has been started connect and display the video stream
@presenterDeskshareHasStarted = ->
	toggleWhiteboardVideo("video")
	console.log("deskshare started")
	joinVoiceCall @, {
		watchOnly: true
	}

# instead of a redis message notify the server to simulate a desksharing
# notification
@simulatePresenterDeskshareHasStarted = ->
	console.log("Calling pres desk started on the server");
	Meteor.call("simulatePresenterDeskshareHasStarted", getInSession("meetingId"), "12345", getInSession("userId"))

@simulatePresenterDeskshareHasEnded = ->
	console.log("calling server to end deskshare")
	Meteor.call("simulatePresenterDeskshareHasEnded", getInSession("meetingId"), getInSession("userId"))

Handlebars.registerHelper "canIPresentDeskshare", ->
	Meteor.Users.findOne({userId: getInSession("userId")})?.user.presenter and not Meteor.config.useSIPAudio
