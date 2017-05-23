package org.bigbluebutton.core.apps

import org.bigbluebutton.core.api._
import org.bigbluebutton.core.OutMessageGateway
import org.bigbluebutton.core.running.{ LiveMeeting, MeetingActor }

trait SharedNotesApp {
  this: MeetingActor =>

  val outGW: OutMessageGateway

  def handlePatchDocumentRequest(msg: PatchDocumentRequest) {
    val requesterID = msg.operation match {
      case SharedNotesOperation.PATCH => msg.requesterID
      case SharedNotesOperation.UNDO => "SYSTEM"
      case SharedNotesOperation.REDO => "SYSTEM"
      case _ => return
    }

    val (patchID, patch, undo, redo) = liveMeeting.notesModel.patchDocument(msg.noteID, msg.patch, msg.operation)

    if (patch != "") outGW.send(new PatchDocumentReply(mProps.meetingID, mProps.recorded, requesterID, msg.noteID, patch, patchID, undo, redo))
  }

  def handleGetCurrentDocumentRequest(msg: GetCurrentDocumentRequest) {
    val notesReport = liveMeeting.notesModel.notesReport.toMap

    outGW.send(new GetCurrentDocumentReply(mProps.meetingID, mProps.recorded, msg.requesterID, notesReport))
  }

  private def createAdditionalNotes(requesterID: String, noteName: String = "") {
    liveMeeting.notesModel.synchronized {
      val noteID = liveMeeting.notesModel.createNote(noteName)

      outGW.send(new CreateAdditionalNotesReply(mProps.meetingID, mProps.recorded, requesterID, noteID, noteName))
    }
  }

  def handleCreateAdditionalNotesRequest(msg: CreateAdditionalNotesRequest) {
    createAdditionalNotes(msg.requesterID, msg.noteName)
  }

  def handleDestroyAdditionalNotesRequest(msg: DestroyAdditionalNotesRequest) {
    liveMeeting.notesModel.synchronized {
      liveMeeting.notesModel.destroyNote(msg.noteID)

      outGW.send(new DestroyAdditionalNotesReply(mProps.meetingID, mProps.recorded, msg.requesterID, msg.noteID))
    }
  }

  def handleRequestAdditionalNotesSetRequest(msg: RequestAdditionalNotesSetRequest) {
    while (liveMeeting.notesModel.notesSize < msg.additionalNotesSetSize) {
      createAdditionalNotes(msg.requesterID)
    }
  }

  def handleSharedNotesSyncNoteRequest(msg: SharedNotesSyncNoteRequest) {
    liveMeeting.notesModel.getNoteReport(msg.noteID) match {
      case Some(note) => outGW.send(new SharedNotesSyncNoteReply(mProps.meetingID, mProps.recorded, msg.requesterID, msg.noteID, note))
      case None =>
    }
  }
}