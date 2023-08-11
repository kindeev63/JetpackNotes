package com.example.jetpacknotes.services

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.*
import com.example.jetpacknotes.Notifications
import com.example.jetpacknotes.R
import com.example.jetpacknotes.db.Reminder
import com.example.jetpacknotes.receivers.AlarmReceiver
import com.example.jetpacknotes.viewModels.MainAppViewModel
import kotlinx.coroutines.*
import java.util.*

class UpdateService : Service() {
    private lateinit var mainAppViewModel: MainAppViewModel
    private lateinit var alarmManager: AlarmManager
    private val scope = CoroutineScope(Dispatchers.Main)

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()
        mainAppViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
            .create(MainAppViewModel::class.java)
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, Notifications.CHANNEL_ID)
            .setContentTitle("Update Service")
            .setContentText("Updating reminders...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
        startForeground(111, notification)
        GlobalScope.launch {
            val allReminders = withContext(Dispatchers.IO) {
                mainAppViewModel.getAllReminders()
            }
            scope.launch {
                Log.e("test", "All reminders = $allReminders")
                val time = Calendar.getInstance().timeInMillis
                val showed = mutableListOf<Reminder>()
                val updated = mutableListOf<Reminder>()
                for (reminder in allReminders) {
                    if (reminder.time <= time) {
                        showed.add(reminder)
                    } else {
                        updated.add(reminder)
                    }
                }
                Log.e("test", "Updated = $updated")
                Log.e("test", "Deleted = $showed")
                for (reminder in updated) {
                    val i = Intent(this@UpdateService, AlarmReceiver::class.java).apply {
                        putExtra("reminder", reminder)
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        this@UpdateService,
                        reminder.id,
                        i,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, reminder.time, pendingIntent
                    )
                }
                Log.e("test", "Update Complete")
                for (reminder in showed){
                    setAlarm(reminder, time)
                }
                Log.e("test", "Show Complete")
                stopForeground(true)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun setAlarm(reminder: Reminder, time: Long){
            val alarmManager =
                getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val i = Intent(this, AlarmReceiver::class.java).apply {
                putExtra("reminder", reminder)
            }

            val pendingIntent =
                PendingIntent.getBroadcast(this, reminder.id, i, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                time,
                pendingIntent
            )
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}
