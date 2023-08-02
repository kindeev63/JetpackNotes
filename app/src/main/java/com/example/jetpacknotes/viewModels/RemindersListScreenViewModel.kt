package com.example.jetpacknotes.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jetpacknotes.db.Category
import com.example.jetpacknotes.db.Note
import com.example.jetpacknotes.db.Reminder
import com.example.jetpacknotes.db.ReminderAction
import java.util.Calendar

class RemindersListScreenViewModel(private val mainAppViewModel: MainAppViewModel): ViewModel() {
    private val _selectedReminders = MutableLiveData<List<Reminder>>(emptyList())
    val selectedReminders: LiveData<List<Reminder>> = _selectedReminders

    fun deleteSelectedReminders() {
        selectedReminders.value?.let { mainAppViewModel.deleteReminders(it) }
        _selectedReminders.value = emptyList()
    }

    fun filterReminders(reminders: List<Reminder>, searchText: String?): List<Reminder> {
        return reminders.filter { reminder -> reminder.title.lowercase().contains(searchText?.lowercase() ?: "")}
    }

    fun changeSelectionStateOf(reminder: Reminder) {
        _selectedReminders.value?.let { selReminders ->
            if (reminder in selReminders) {
                _selectedReminders.value = ArrayList(selReminders).apply {
                    remove(reminder)
                }
            } else {
                _selectedReminders.value = ArrayList(selReminders).apply {
                    add(reminder)
                }
            }
        }
    }
}