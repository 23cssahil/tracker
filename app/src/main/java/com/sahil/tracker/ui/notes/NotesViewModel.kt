package com.sahil.tracker.ui.notes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sahil.tracker.data.database.TrackerDatabase
import com.sahil.tracker.data.models.Note
import com.sahil.tracker.data.repository.TrackerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotesViewModel(app: Application) : AndroidViewModel(app) {
    private val repository: TrackerRepository
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _editingNote = MutableStateFlow<Note?>(null)
    val editingNote: StateFlow<Note?> = _editingNote.asStateFlow()

    init {
        val db = TrackerDatabase.getDatabase(app)
        repository = TrackerRepository(db.typingEventDao(), db.noteDao())
        loadAllNotes()
    }

    fun loadAllNotes() {
        viewModelScope.launch {
            repository.getAllNotes().collect {
                _notes.value = it
            }
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            loadAllNotes()
        } else {
            viewModelScope.launch {
                repository.searchNotes(query).collect {
                    _notes.value = it
                }
            }
        }
    }

    fun saveNote(title: String, content: String) {
        val now = System.currentTimeMillis()
        val dateStr = dateFormat.format(Date(now))
        val existing = _editingNote.value
        viewModelScope.launch {
            if (existing == null) {
                repository.insertNote(Note(title = title, content = content, createdAt = now, updatedAt = now, dateString = dateStr))
            } else {
                repository.updateNote(existing.copy(title = title, content = content, updatedAt = now))
            }
            _editingNote.value = null
        }
    }

    fun editNote(note: Note) { _editingNote.value = note }
    fun clearEditing() { _editingNote.value = null }

    fun deleteNote(note: Note) {
        viewModelScope.launch { repository.deleteNote(note) }
    }
}
