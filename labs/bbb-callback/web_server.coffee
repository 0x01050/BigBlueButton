express = require("express")
url = require("url")

config = require("./config")
Hook = require("./hook")
Utils = require("./utils")

# Web server that listens for API calls and process them.
module.exports = class WebServer

  constructor: ->
    @app = express()
    @_registerRoutes()

  start: (port) ->
    @server = @app.listen(port)
    unless @server.address()?
      console.log "Could not bind to port", port
      console.log "Aborting."
      process.exit(1)
    console.log "== Server listening on port", port, "in", @app.settings.env.toUpperCase(), "mode"

  _registerRoutes: ->
    # Request logger
    @app.all "*", (req, res, next) ->
      console.log "<==", req.method, "request to", req.url, "from:", clientDataSimple(req)
      next()

    @app.get "/bigbluebutton/api/hooks/create", @_validateChecksum, @_create
    @app.get "/bigbluebutton/api/hooks/destroy", @_validateChecksum, @_destroy
    @app.get "/bigbluebutton/api/hooks/list", @_validateChecksum, @_list

  _create: (req, res, next) ->
    urlObj = url.parse(req.url, true)
    callbackURL = urlObj.query["callbackURL"]
    meetingID = urlObj.query["meetingID"]

    # TODO: if meetingID is set in the url, check if the meeting exists, otherwise
    #   invalid("invalidMeetingIdentifier", "The meeting ID that you supplied did not match any existing meetings");

    unless callbackURL?
      respondWithXML(res, config.api.responses.missingParamCallbackURL)
    else
      Hook.addSubscription callbackURL, meetingID, (error, hook) ->
        if error? # the only error for now is for duplicated callbackURL
          msg = config.api.responses.hookDuplicated(hook.id)
        else if hook?
          msg = config.api.responses.hookSuccess(hook.id)
        else
          msg = config.api.responses.hookFailure
        respondWithXML(res, msg)

  _destroy: (req, res, next) ->
    urlObj = url.parse(req.url, true)
    hookID = urlObj.query["hookID"]

    unless hookID?
      respondWithXML(res, config.api.responses.missingParamHookID)
    else
      Hook.removeSubscription hookID, (error, result) ->
        if error?
          msg = config.api.responses.destroyFailure
        else if !result
          msg = config.api.responses.destroyNoHook
        else
          msg = config.api.responses.destroySuccess
        respondWithXML(res, msg)

  _list: (req, res, next) ->
    # TODO: implement
    res.send "Listing subscriptions!"

  # Validates the checksum in the request `req`.
  # If it doesn't match BigBlueButton's shared secret, will send an XML response
  # with an error code just like BBB does.
  _validateChecksum: (req, res, next) =>
    urlObj = url.parse(req.url, true)
    checksum = urlObj.query["checksum"]

    if checksum is Utils.checksumAPI(req.url, config.bbb.sharedSecret)
      next()
    else
      console.log "checksum check failed, sending a checksumError response"
      res.setHeader("Content-Type", "text/xml")
      res.send cleanupXML(config.api.responses.checksumError)

respondWithXML = (res, msg) ->
  res.setHeader("Content-Type", "text/xml")
  res.send cleanupXML(msg)

# Returns a simple string with a description of the client that made
# the request. It includes the IP address and the user agent.
clientDataSimple = (req) ->
  "ip " + Utils.ipFromRequest(req) + ", using " + req.headers["user-agent"]

# Cleans up a string with an XML in it removing spaces and new lines from between the tags.
cleanupXML = (string) ->
  string.trim().replace(/>\s*/g, '>')
