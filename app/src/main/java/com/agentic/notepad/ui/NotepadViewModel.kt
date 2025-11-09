package com.agentic.notepad.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agentic.notepad.data.NoteRepository
import com.agentic.notepad.model.Note
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class NotepadUiState(
    val notes: List<Note> = emptyList(),
    val searchQuery: String = ""
)

class NotepadViewModel(
    private val repository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotepadUiState())
    val uiState: StateFlow<NotepadUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.notes.collectLatest { saved ->
                _uiState.value = _uiState.value.copy(notes = saved.sortedWith(noteComparator()))
            }
        }
    }

    fun updateSearch(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query
        )
    }

    fun filteredNotes(): List<Note> {
        val current = _uiState.value
        if (current.searchQuery.isBlank()) return current.notes
        val normalized = current.searchQuery.trim().lowercase()
        return current.notes.filter {
            it.title.lowercase().contains(normalized) ||
                it.body.lowercase().contains(normalized)
        }
    }

    fun createNote(title: String, body: String) {
        val newNote = Note(
            id = UUID.randomUUID().toString(),
            title = title.ifBlank { "Untitled" },
            body = body,
            pinned = false,
            lastModified = System.currentTimeMillis()
        )
        persist(_uiState.value.notes + newNote)
    }

    fun updateNote(noteId: String, title: String, body: String) {
        val current = _uiState.value.notes.toMutableList()
        val index = current.indexOfFirst { it.id == noteId }
        if (index == -1) return
        current[index] = current[index].copy(
            title = title.ifBlank { "Untitled" },
            body = body,
            lastModified = System.currentTimeMillis()
        )
        persist(current)
    }

    fun deleteNote(noteId: String) {
        val updated = _uiState.value.notes.filterNot { it.id == noteId }
        persist(updated)
    }

    fun togglePin(noteId: String) {
        val current = _uiState.value.notes.toMutableList()
        val index = current.indexOfFirst { it.id == noteId }
        if (index == -1) return
        val note = current[index]
        current[index] = note.copy(
            pinned = !note.pinned,
            lastModified = System.currentTimeMillis()
        )
        persist(current)
    }

    private fun persist(notes: List<Note>) {
        viewModelScope.launch {
            repository.saveNotes(notes.sortedWith(noteComparator()))
        }
    }

    private fun noteComparator(): Comparator<Note> = compareByDescending<Note> { it.pinned }
        .thenByDescending { it.lastModified }
}
