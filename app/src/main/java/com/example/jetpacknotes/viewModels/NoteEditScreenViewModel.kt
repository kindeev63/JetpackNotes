package com.example.jetpacknotes.viewModels

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jetpacknotes.db.Note
import com.example.jetpacknotes.notes.NoteState
import java.util.Calendar

class NoteEditScreenViewModel(private val mainAppViewModel: MainAppViewModel): ViewModel() {
    private var noteStates = arrayListOf<NoteState>()
    private var stateIndex = -1
    private val _currentNoteState = MutableLiveData<NoteState>()
    val currentNoteState: LiveData<NoteState> = _currentNoteState

    fun addNoteState(noteState: NoteState) {
        if (stateIndex != -1) {
            val statesToDelete =
                noteStates.filter { state -> noteStates.indexOf(state) > (noteStates.size + stateIndex) }
            noteStates.removeAll(statesToDelete.toSet())
            stateIndex = -1
        }
        noteStates.add(noteState)
        _currentNoteState.value = noteState
    }

    fun addNoteState(
        title: TextFieldValue? = null,
        text: TextFieldValue? = null,
        colorIndex: Int? = null,
        categories: String? = null
    ) {
        val oldState = getState()
        addNoteState(
            NoteState(
                title = title ?: oldState.title,
                text = text ?: oldState.text,
                colorIndex = colorIndex ?: oldState.colorIndex,
                categories = categories ?: oldState.categories
            )
        )
    }

    fun redoState() {
        if (stateIndex < -1) stateIndex++
        _currentNoteState.value = getState()
    }

    fun undoState() {
        if (noteStates.size + stateIndex > 0) stateIndex--
        _currentNoteState.value = getState()
    }

    private fun getState() = noteStates[noteStates.size + stateIndex]

    fun createNote(function: (Note) -> Unit) {
        val idsList = (mainAppViewModel.allNotes.value ?: emptyList()).map { it.id }
        var noteId = 0
        while (true) {
            if (noteId !in idsList) break
            noteId++
        }
        val note = Note(noteId, "", "", Calendar.getInstance().timeInMillis, "", 0)
        function(note)
    }
}