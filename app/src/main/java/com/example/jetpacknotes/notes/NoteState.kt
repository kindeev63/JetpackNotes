package com.example.jetpacknotes.notes

import androidx.compose.ui.text.input.TextFieldValue
import java.io.Serializable

data class NoteState(val title: TextFieldValue, val text: TextFieldValue, val colorIndex: Int, val categories: String): Serializable
