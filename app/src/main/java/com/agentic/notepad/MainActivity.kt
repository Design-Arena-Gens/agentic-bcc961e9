package com.agentic.notepad

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agentic.notepad.data.NoteRepository
import com.agentic.notepad.ui.NotepadApp
import com.agentic.notepad.ui.NotepadViewModel
import com.agentic.notepad.ui.NotepadViewModelFactory
import com.agentic.notepad.ui.theme.AgenticNotepadTheme

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "notes_datastore")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val factory = NotepadViewModelFactory(NoteRepository(dataStore))
            val viewModel: NotepadViewModel = viewModel(factory = factory)
            AgenticNotepadTheme(darkTheme = isSystemInDarkTheme()) {
                Surface {
                    NotepadApp(viewModel = viewModel)
                }
            }
        }
    }
}
