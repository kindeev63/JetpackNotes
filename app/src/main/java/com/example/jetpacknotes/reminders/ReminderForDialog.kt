package com.example.jetpacknotes.reminders

import com.example.jetpacknotes.db.Reminder
import java.io.Serializable

data class ReminderForDialog(val reminder: Reminder?): Serializable