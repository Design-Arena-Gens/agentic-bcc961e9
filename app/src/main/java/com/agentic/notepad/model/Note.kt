package com.agentic.notepad.model

import kotlinx.serialization.Serializable

@Serializable
data class Note(
    val id: String,
    val title: String,
    val body: String,
    val pinned: Boolean,
    val lastModified: Long
)
