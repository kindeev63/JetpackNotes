package com.example.jetpacknotes.viewModels

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jetpacknotes.db.Category
import com.example.jetpacknotes.db.CategoryType
import com.example.jetpacknotes.db.Note
import java.util.Calendar
import java.util.Date

class NotesListScreenViewModel(private val mainAppViewModel: MainAppViewModel) : ViewModel() {
    private val _selectedNotes = MutableLiveData<List<Note>>(emptyList())
    val selectedNotes: LiveData<List<Note>> = _selectedNotes

    fun changeSelectionStateOf(note: Note) {
        _selectedNotes.value?.let { selNotes ->
            if (note in selNotes) {
                _selectedNotes.value = ArrayList(selNotes).apply {
                    remove(note)
                }
            } else {
                _selectedNotes.value = ArrayList(selNotes).apply {
                    add(note)
                }
            }
        }
    }

    fun deleteSelectedNotes() {
        selectedNotes.value?.let { mainAppViewModel.deleteNotes(it) }
        _selectedNotes.value = emptyList()
    }

    fun createCategory(): Category {
        val idsList = (mainAppViewModel.categoryOfNotes.value ?: emptyList()).map { it.id }
        var categoryId = 0
        while (true) {
            if (categoryId !in idsList) break
            categoryId++
        }
        return Category(categoryId, "", CategoryType.Note)
    }

    fun clickOnCategory(category: Category, long: Boolean, state: MutableState<Category?>, openDialog: MutableState<Category?>) {
        if (long) {
            openDialog.value = category.copy()
        } else {
            state.value = category.copy()
        }
    }

    fun filterNotes(notes: List<Note>, searchText: String?, category: Category?): List<Note> {
        return notes.filter { note -> (if (category != null) category.id.toString() in note.categories.split(" | ") else true) && note.title.lowercase().contains(searchText?.lowercase() ?: "")}
    }

    fun deleteCategory(category: Category) {
        mainAppViewModel.allNotes.value?.forEach { note ->
            if (note.categories.contains(category.id.toString())) {
                mainAppViewModel.insertNote(note.copy(
                    categories = ArrayList(note.categories.split(" | ")).apply {
                        remove(category.id.toString())
                    }.joinToString(" | ")
                ))
            }
        }
        mainAppViewModel.deleteCategory(category)
    }

    fun insertCategory(category: Category) {
        mainAppViewModel.insertCategory(category)
    }
}