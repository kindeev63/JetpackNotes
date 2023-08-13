package com.example.jetpacknotes

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.jetpacknotes.db.Note
import com.example.jetpacknotes.notes.NoteEditScreen
import com.example.jetpacknotes.notes.NotesListScreen
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mainAppViewModel = (application as MainApp).mainAppViewModel
        createNotificationChannel(this)
        setContent {
            if (intent.hasExtra("noteId")) {
                NoteEditScreen(
                    mainAppViewModel = mainAppViewModel,
                    noteId = intent.getIntExtra("noteId", 0)
                ) {
                    finish()
                }
            } else {
                MainScreen(mainAppViewModel = mainAppViewModel)
            }
        }
    }

    private fun createNotificationChannel(context: Context) {
        val channelName = "Reminders"
        val channelDescription = "Reminders for Jetpack Notes"
        val channelImportance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(
            Notifications.CHANNEL_ID, channelName, channelImportance
        ).apply {
            description = channelDescription
        }
        val notificationManager =
            context.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

