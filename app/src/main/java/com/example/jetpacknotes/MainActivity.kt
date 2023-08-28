package com.example.jetpacknotes

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.jetpacknotes.db.Task
import com.example.jetpacknotes.notes.NoteEditScreen
import com.example.jetpacknotes.tasks.TaskEditDialog
import com.example.jetpacknotes.viewModels.MainAppViewModel
import kotlinx.coroutines.launch

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
                if (intent.hasExtra("taskId")) {
                    OpenTask(taskId = intent.getIntExtra("taskId", 0), mainAppViewModel = mainAppViewModel)
                }
                MainScreen(mainAppViewModel = mainAppViewModel)
            }
        }
    }

    @Composable
    private fun OpenTask(taskId: Int, mainAppViewModel: MainAppViewModel) {
        var task by rememberSaveable {
            mutableStateOf<Task?>(null)
        }
        val scope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            scope.launch {
                mainAppViewModel.getTaskById(taskId) {
                    task = it
                }
            }
        }
        task?.let {
            TaskEditDialog(task = task, mainAppViewModel = mainAppViewModel, onDismissRequest = {
                task = null
            })
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

