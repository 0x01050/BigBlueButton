package org.bigbluebutton.core.models

import org.bigbluebutton.common2.messages.breakoutrooms.{ BreakoutRoomVO, BreakoutUserVO }

object BreakoutRooms {

  def newBreakoutRoom(parentRoomId: String, id: String, externalMeetingId: String, name: String, sequence: Integer, voiceConfId: String,
    assignedUsers: Vector[String], breakoutRooms: BreakoutRooms): Option[BreakoutRoomVO] = {
    val brvo = new BreakoutRoomVO(id, externalMeetingId, name, parentRoomId, sequence, voiceConfId, assignedUsers, Vector())
    breakoutRooms.add(brvo)
    Some(brvo)
  }

  def getBreakoutRoom(breakoutRooms: BreakoutRooms, id: String): Option[BreakoutRoomVO] = {
    breakoutRooms.rooms.get(id)
  }

  def getRoomWithExternalId(breakoutRooms: BreakoutRooms, externalId: String): Option[BreakoutRoomVO] = {
    breakoutRooms.rooms.values find (r => r.externalMeetingId == externalId)
  }

  def getRooms(breakoutRooms: BreakoutRooms): Array[BreakoutRoomVO] = {
    breakoutRooms.rooms.values.toArray
  }

  def getNumberOfRooms(breakoutRooms: BreakoutRooms): Int = {
    breakoutRooms.rooms.size
  }

  def getAssignedUsers(breakoutRooms: BreakoutRooms, breakoutMeetingId: String): Option[Vector[String]] = {
    for {
      room <- breakoutRooms.rooms.get(breakoutMeetingId)
    } yield room.assignedUsers
  }

  def updateBreakoutUsers(breakoutRooms: BreakoutRooms, breakoutMeetingId: String, users: Vector[BreakoutUserVO]): Option[BreakoutRoomVO] = {
    for {
      room <- breakoutRooms.rooms.get(breakoutMeetingId)
      newroom = room.copy(users = users)
      room2 = breakoutRooms.add(newroom)
    } yield room2
  }

  def removeRoom(breakoutRooms: BreakoutRooms, id: String) {
    breakoutRooms.remove(id);
  }
}

class BreakoutRooms {

  private var rooms = new collection.immutable.HashMap[String, BreakoutRoomVO]

  var pendingRoomsNumber: Integer = 0

  private def add(room: BreakoutRoomVO): BreakoutRoomVO = {
    rooms += room.id -> room
    room
  }

  private def remove(id: String) {
    rooms -= id
  }
}
