package org.bigbluebutton.api2.endpoint.redis

class MessageSender(publisher: RedisPublisher) {

  def send(channel: String, data: String) {
    publisher.publish(channel, data)
  }
}
