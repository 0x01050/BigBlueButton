config = require("../config")

moduleDeps = ["App"]

# The main router that registers the routes that can be accessed by the client.
module.exports = class MainRouter

  constructor: () ->
    config.modules.wait moduleDeps, =>
      @app = config.modules.get("App")
      @_registerRoutes()

  _registerRoutes: () ->
    @app.get "/", @_index

    @app.get "/html5.client", (req, res) ->
      meetingId = req.query.meeting_id
      userId = req.query.user_id
      authToken = req.query.auth_token

      console.log "\n\nLANDING PAGE IS PASSING:\n" + 
        "meeting_id=#{meetingId}\n" +
        "user_id=#{userId}\n" +
        "auth_token=#{authToken}\n\n"

  # Base route to render the HTML5 client.
  #
  # This method is registered as a route on express.
  #
  # @internal
  _index: (req, res) =>
    res.render "index",
      title: config.appName
