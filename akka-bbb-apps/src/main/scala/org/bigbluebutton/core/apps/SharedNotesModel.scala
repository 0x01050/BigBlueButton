package org.bigbluebutton.core.apps

import name.fraser.neil.plaintext.diff_match_patch

import scala.collection._
import scala.collection.mutable.Stack
import scala.collection.mutable.HashMap

import org.bigbluebutton.common2.msgs.Note
import org.bigbluebutton.common2.msgs.NoteReport

class SharedNotesModel {
  val MAIN_NOTE_ID = "MAIN_NOTE"
  val SYSTEM_ID = "SYSTEM"
  val MAX_UNDO_STACK_SIZE = 30

  private val patcher = new diff_match_patch()

  private var notesCounter = 0;
  private var removedNotes: Set[Int] = Set()

  val notes = new HashMap[String, Note]()
  notes += (MAIN_NOTE_ID -> new Note("", "", 0, new Stack(), new Stack()))

  def patchNote(noteId: String, patch: String, operation: String): (Integer, String, Boolean, Boolean) = {
    notes.synchronized {
      val note = notes(noteId)
      val document = note.document
      var undoPatches = note.undoPatches
      var redoPatches = note.redoPatches

      var patchToApply = operation match {
        case "PATCH" => {
          patch
        }
        case "UNDO" => {
          if (undoPatches.isEmpty) {
            return (-1, "", false, false)
          } else {
            val (undo, redo) = undoPatches.pop()
            redoPatches.push((undo, redo))
            undo
          }
        }
        case "REDO" => {
          if (redoPatches.isEmpty) {
            return (-1, "", false, false)
          } else {
            val (undo, redo) = redoPatches.pop()
            undoPatches.push((undo, redo))
            redo
          }
        }
      }

      val patchObjects = patcher.patch_fromText(patchToApply)
      val result = patcher.patch_apply(patchObjects, document)

      // If it is a patch operation, save an undo patch and clear redo stack
      if (operation == "PATCH") {
        undoPatches.push((patcher.custom_patch_make(result(0).toString(), document), patchToApply))
        redoPatches.clear

        if (undoPatches.size > MAX_UNDO_STACK_SIZE) {
          undoPatches = undoPatches.dropRight(1)
        }
      }

      val patchCounter = note.patchCounter + 1
      notes(noteId) = new Note(note.name, result(0).toString(), patchCounter, undoPatches, redoPatches)
      (patchCounter, patchToApply, !undoPatches.isEmpty, !redoPatches.isEmpty)
    }
  }

  def createNote(noteName: String = ""): String = {
    var noteId = 0
    if (removedNotes.isEmpty) {
      notesCounter += 1
      noteId = notesCounter
    } else {
      noteId = removedNotes.min
      removedNotes -= noteId
    }
    notes += (noteId.toString -> new Note(noteName, "", 0, new Stack(), new Stack()))

    noteId.toString
  }

  def destroyNote(noteId: String) {
    removedNotes += noteId.toInt
    notes -= noteId
  }

  def notesReport: HashMap[String, NoteReport] = {
    notes.synchronized {
      var report = new HashMap[String, NoteReport]()
      notes foreach {
        case (id, note) =>
          report += (id -> noteToReport(note))
      }
      report
    }
  }

  def getNoteReport(noteId: String): Option[NoteReport] = {
    notes.synchronized {
      notes.get(noteId) match {
        case Some(note) => Some(noteToReport(note))
        case None => None
      }
    }
  }

  private def noteToReport(note: Note): NoteReport = {
    new NoteReport(note.name, note.document, note.patchCounter, !note.undoPatches.isEmpty, !note.redoPatches.isEmpty)
  }
}