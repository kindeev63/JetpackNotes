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

    fun createReminder(packageName: String, function: (Reminder) -> Unit) {
        val idsList = (mainAppViewModel.allReminders.value ?: emptyList()).map { it.id }
        var reminderId = 0
        while (true) {
            if (reminderId !in idsList) break
            reminderId++
        }
        val reminder = Reminder(reminderId, "", "", Calendar.getInstance().timeInMillis, null, packageName, true, ReminderAction.OpenApp)
        mainAppViewModel.insertReminder(reminder, function)
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