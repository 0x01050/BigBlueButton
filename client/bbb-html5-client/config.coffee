# # Global configurations file

config = {}

# Default global variables
config.appName = 'BigBlueButton HTML5 Client'
config.maxUsernameLength = 30
config.maxChatLength = 140

# the path in which an image of a presentation is stored
config.presentationImagePath = (meetingID, presentationID, filename) ->
  "bigbluebutton/presentation/#{meetingID}/#{meetingID}/#{presentationID}/png/#{filename}"

## Application configurations
config.app = {}

# Generate a new secret with:
# $ npm install crypto
# $ coffee
# coffee> crypto = require 'crypto'
# coffee> crypto.randomBytes(32).toString('base64')
config.app.sessionSecret = "J7XSu96KC/B/UPyeGub3J6w6QFXWoUNABVgi9Q1LskE="

# Configs for redis
config.redis = {}
config.redis.host = "127.0.0.1"
config.redis.post = "6379"

# Logging
config.log = {}
config.log.path = "/var/log/bigbluebutton/bbbnode.log"

# Global instance of Modules, created by `app.coffee`
config.modules = null

module.exports = config
