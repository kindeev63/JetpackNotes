package com.example.jetpacknotes.viewModels

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
import com.example.jetpacknotes.receivers.AlarmReceiver
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

    fun deleteSelectedNotes(context: Context) {
        selectedNotes.value?.let { notes ->
            val remindersForDelete =
                mainAppViewModel.allReminders.value?.filter { reminder ->
                    notes.any { note ->
                        reminder.itemId == note.id
                    }
                }
            remindersForDelete?.let { remindersList ->
                remindersList.map { it.id }.forEach { reminderId ->
                    cancelAlarm(reminderId, context)
                }
                mainAppViewModel.deleteReminders(remindersList)
            }
            mainAppViewModel.deleteNotes(notes)
        }
        _selectedNotes.value = emptyList()
    }

    private fun cancelAlarm(reminderId: Int, context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val i = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, reminderId, i, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        )
        alarmManager.cancel(pendingIntent)
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
                mainAppViewModel.insertNote(
                    note.copy(
                        categories = ArrayList(note.categories.split(" | ")).apply {
                            remove(category.id.toString())
                        }.joinToString(" | ")
                    )
                )
            }
        }
        mainAppViewModel.deleteCategory(category)
    }
}