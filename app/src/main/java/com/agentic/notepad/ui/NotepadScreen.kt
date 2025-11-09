package com.agentic.notepad.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agentic.notepad.R
import com.agentic.notepad.model.Note

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotepadApp(
    viewModel: NotepadViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val notes = viewModel.filteredNotes()
    val editorState = remember { mutableStateOf<EditableNote?>(null) }
    val isEditing = editorState.value != null

    Scaffold(
        topBar = {
            NotepadTopBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::updateSearch
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editorState.value = EditableNote.New() }
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedVisibility(visible = notes.isEmpty()) {
                EmptyState()
            }

            AnimatedVisibility(visible = notes.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
                ) {
                    items(notes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onClick = { editorState.value = EditableNote.Existing(note) },
                            onDelete = { viewModel.deleteNote(note.id) },
                            onTogglePin = { viewModel.togglePin(note.id) }
                        )
                    }
                }
            }
        }
    }

    if (isEditing) {
        NoteEditorDialog(
            editorState = editorState,
            onSave = { editable ->
                when (editable) {
                    is EditableNote.New -> viewModel.createNote(editable.title, editable.body)
                    is EditableNote.Existing -> viewModel.updateNote(
                        editable.source.id,
                        editable.title,
                        editable.body
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotepadTopBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    TopAppBar(
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text(text = stringResource(id = R.string.search_notes_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.empty_notes_message),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = if (note.pinned) 6.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (note.pinned) FontWeight.Bold else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = note.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    IconButton(onClick = onTogglePin) {
                        Icon(
                            imageVector = if (note.pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = stringResource(id = R.string.pin_note_content_description)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(id = R.string.delete_note_content_description)
                        )
                    }
                }
            }
        }
    }
}

private sealed interface EditableNote {
    val title: String
    val body: String

    data class New(
        override val title: String = "",
        override val body: String = ""
    ) : EditableNote

    data class Existing(
        val source: Note,
        override val title: String = source.title,
        override val body: String = source.body
    ) : EditableNote
}

@Composable
private fun NoteEditorDialog(
    editorState: MutableState<EditableNote?>,
    onSave: (EditableNote) -> Unit
) {
    val current = editorState.value ?: return
    val titleState = remember(current) { mutableStateOf(current.title) }
    val bodyState = remember(current) { mutableStateOf(current.body) }

    LaunchedEffect(current) {
        titleState.value = current.title
        bodyState.value = current.body
    }

    AlertDialog(
        onDismissRequest = { editorState.value = null },
        title = {
            Text(
                text = if (current is EditableNote.New) "New note" else "Edit note",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = titleState.value,
                    onValueChange = { titleState.value = it },
                    label = { Text(text = "Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = bodyState.value,
                    onValueChange = { bodyState.value = it },
                    label = { Text(text = "Note") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val updated = when (current) {
                        is EditableNote.New -> current.copy(
                            title = titleState.value,
                            body = bodyState.value
                        )
                        is EditableNote.Existing -> current.copy(
                            title = titleState.value,
                            body = bodyState.value
                        )
                    }
                    onSave(updated)
                    editorState.value = null
                }
            ) {
                Text(text = "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = { editorState.value = null }) {
                Text(text = "Cancel")
            }
        }
    )
}
