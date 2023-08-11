package com.example.jetpacknotes.viewModels

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jetpacknotes.db.Category
import com.example.jetpacknotes.db.Note
import com.example.jetpacknotes.db.Reminder
import com.example.jetpacknotes.db.ReminderAction
import com.example.jetpacknotes.receivers.AlarmReceiver
import java.util.Calendar

class RemindersListScreenViewModel(private val mainAppViewModel: MainAppViewModel): ViewModel() {
    private val _selectedReminders = MutableLiveData<List<Reminder>>(emptyList())
    val selectedReminders: LiveData<List<Reminder>> = _selectedReminders

    fun deleteSelectedReminders(context: Context) {
        selectedReminders.value?.let {reminders ->
            reminders.map { it.id }.forEach { reminderId ->
                cancelAlarm(reminderId, context)
            }
            mainAppViewModel.deleteReminders(reminders)
        }
        _selectedReminders.value = emptyList()

    }

    private fun cancelAlarm(reminderId: Int, context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val i = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            i,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        )
        alarmManager.cancel(pendingIntent)
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