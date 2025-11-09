package com.agentic.notepad.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agentic.notepad.data.NoteRepository

class NotepadViewModelFactory(
    private val repository: NoteRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotepadViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotepadViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class $modelClass")
    }
}
