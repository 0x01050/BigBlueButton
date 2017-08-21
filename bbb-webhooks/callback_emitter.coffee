_ = require('lodash')
request = require("request")
url = require('url')
EventEmitter = require('events').EventEmitter

config = require("./config")
Logger = require("./logger")
Utils = require("./utils")

# Use to perform a callback. Will try several times until the callback is
# properly emitted and stop when successful (or after a given number of tries).
# Used to emit a single callback. Destroy it and create a new class for a new callback.
# Emits "success" on success, "failure" on error and "stopped" when gave up trying
# to perform the callback.
module.exports = class CallbackEmitter extends EventEmitter

  constructor: (@callbackURL, @message, @backupURL) ->
    @nextInterval = 0
    @timestap = 0
    @permanent = false

  start: (permanent) ->
    @timestamp = new Date().getTime()
    @nextInterval = 0
    @permanent = permanent
    @_scheduleNext 0

  _scheduleNext: (timeout) ->
    setTimeout( =>
      @_emitMessage (error, result) =>
        if not error? and result
          @emit "success"
        else
          @emit "failure", error

          # get the next interval we have to wait and schedule a new try
          interval = config.hooks.retryIntervals[@nextInterval]
          if interval?
            Logger.warn "[Emitter] trying the callback again in #{interval/1000.0} secs"
            @nextInterval++
            @_scheduleNext(interval)

          # no intervals anymore, time to give up
          else
            @nextInterval = if not @permanent then 0 else 8 # Reset interval to permanent hooks
            # If a hook has backup URLs for the POSTS, use them after a few failed attempts
            if @backupURL? and @permanent then @backupURL.push(@backupURL[0]); @backupURL.shift(); @callbackURL = @backupURL[0]
            @_scheduleNext(interval) if @permanent
            @emit "stopped" if not @permanent

    , timeout)

  _emitMessage: (callback) ->
    # data to be sent
    # note: keep keys in alphabetical order
    data =
      event: @message
      timestamp: @timestamp

    # calculate the checksum
    checksum = Utils.checksum("#{@callbackURL}#{JSON.stringify(data)}#{config.bbb.sharedSecret}")

    # get the final callback URL, including the checksum
    urlObj = url.parse(@callbackURL, true)
    callbackURL = @callbackURL
    callbackURL += if _.isEmpty(urlObj.search) then "?" else "&"
    callbackURL += "checksum=#{checksum}"

    requestOptions =
      followRedirect: true
      maxRedirects: 10
      uri: callbackURL
      method: "POST"
      form: data

    request requestOptions, (error, response, body) ->
      if error? or not (response?.statusCode >= 200 and response?.statusCode < 300)
        Logger.warn "[Emitter] error in the callback call to: [#{requestOptions.uri}] for #{simplifiedEvent(data.event)}", "error:", error, "status:", response?.statusCode
        callback error, false
      else
        Logger.info "[Emitter] successful callback call to: [#{requestOptions.uri}] for #{simplifiedEvent(data.event)}"
        callback null, true

# A simple string that identifies the event
simplifiedEvent = (event) ->
  try
    eventJs = JSON.parse(event)
    "event: { name: #{eventJs.data?.id}, timestamp: #{eventJs.data.event?.ts} }"
  catch e
    "event: #{event}"
