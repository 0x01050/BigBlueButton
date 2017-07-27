package org.bigbluebutton.core.domain

case class BreakoutRoom2x(
    id:            String,
    externalId:    String,
    name:          String,
    parentId:      String,
    sequence:      Int,
    voiceConf:     String,
    assignedUsers: Vector[String],
    users:         Vector[BreakoutUser],
    startedOn:     Option[Long],
    started:       Boolean
) {

}

case class BreakoutUser(id: String, name: String)
