package com.example.data.repository

import com.example.data.local.NoteDao
import com.example.data.model.Note
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {

    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()

    fun searchNotes(query: String): Flow<List<Note>> {
        return if (query.isBlank()) {
            noteDao.getAllNotes()
        } else {
            noteDao.searchNotes(query.trim())
        }
    }

    fun getNotesByCategory(category: String): Flow<List<Note>> {
        return if (category == "Tümü" || category.isBlank()) {
            noteDao.getAllNotes()
        } else {
            noteDao.getNotesByCategory(category)
        }
    }

    suspend fun insert(note: Note): Long {
        return noteDao.insertNote(note)
    }

    suspend fun update(note: Note) {
        noteDao.updateNote(note)
    }

    suspend fun delete(note: Note) {
        noteDao.deleteNote(note)
    }

    suspend fun togglePin(note: Note) {
        noteDao.updateNote(note.copy(isPinned = !note.isPinned))
    }
}
