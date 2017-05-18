package org.bigbluebutton.api2.domain

case class User2(intId: String, extId: String, name: String, role: String, avatarURL: String,
                 guest: Boolean, waitingForAcceptance: Boolean, status: Vector[String],
                 streams: Set[String])

object Users {
  def findWithId(users: Users, id: String): Option[User2] = {
    users.toVector.find(u => u.intId == id)
  }

  def add(users: Users, user: User2): User2 = {
    users.save(user)
  }

  def remove(users: Users, id: String): Option[User2] = {
    users.remove(id)
  }
}

class Users {
  private var users = new collection.immutable.HashMap[String, User2]

  private def toVector: Vector[User2] = users.values.toVector

  private def save(user: User2): User2 = {
    users += user.intId -> user
    user
  }

  private def remove(id: String): Option[User2] = {
    val user = users.get(id)
    user foreach (u => users -= id)
    user
  }
}

case class RegisteredUser2(meetingId: String, intId: String, name: String, role: String,
                           extId: String, authToken: String, avatarURL: String,
                           guest: Boolean, authed: Boolean)

object RegisteredUsers {
  def findWithId(users: RegisteredUsers, id: String): Option[RegisteredUser2] = {
    users.toVector.find(u => u.intId == id)
  }

  def add(users: RegisteredUsers, user: RegisteredUser2): RegisteredUser2 = {
    users.save(user)
  }

  def remove(users: RegisteredUsers, id: String): Option[RegisteredUser2] = {
    users.remove(id)
  }
}

class RegisteredUsers {
  private var users = new collection.immutable.HashMap[String, RegisteredUser2]

  private def toVector: Vector[RegisteredUser2] = users.values.toVector

  private def save(user: RegisteredUser2): RegisteredUser2 = {
    users += user.intId -> user
    user
  }

  private def remove(id: String): Option[RegisteredUser2] = {
    val user = users.get(id)
    user foreach (u => users -= id)
    user
  }
}

case class Config2(token: String, timestamp: Long, config: String)

object Configs {

}

class Configs {
  private var configs = new collection.immutable.HashMap[String, Config2]

  private def toVector: Vector[Config2] = configs.values.toVector

  private def save(config: Config2): Config2 = {
    configs += config.token -> config
    config
  }

  private def remove(id: String): Option[Config2] = {
    val config = configs.get(id)
    config foreach (u => configs -= id)
    config
  }
}