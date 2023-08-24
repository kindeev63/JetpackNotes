package com.example.jetpacknotes.receivers

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.jetpacknotes.MainActivity
import com.example.jetpacknotes.MainApp
import com.example.jetpacknotes.Notifications
import com.example.jetpacknotes.R
import com.example.jetpacknotes.db.Reminder
import com.example.jetpacknotes.db.ReminderAction

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminder = intent.getSerializableExtra("reminder") as Reminder
        val notificationIntent = when (reminder.action) {
            ReminderAction.OpenApp -> {
                context.packageManager.getLaunchIntentForPackage(getCorrectPackageName(reminder.packageName, context))
            }

            ReminderAction.OpenNote -> {
                Intent(context, MainActivity::class.java).apply {
                    reminder.itemId?.let { putExtra("noteId", reminder.itemId) }
                }
            }

            ReminderAction.OpenTask -> {
                Intent(context, MainActivity::class.java).apply {
                    reminder.itemId?.let { putExtra("taskId", reminder.itemId) }
                }
            }
        }
        notificationIntent?.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        createNotification(
            context,
            reminder.id,
            reminder.title,
            reminder.description,
            notificationIntent
        )
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "MyApp:MyWakeLock"
        )
        if (reminder.sound) playNotificationSound(context)
        wakeLock.acquire(5000)
        val mainAppViewModel = (context.applicationContext as MainApp).mainAppViewModel
        mainAppViewModel.deleteReminders(listOf(reminder))
    }

    private fun playNotificationSound(context: Context) {
        val notificationSound = RingtoneManager.getRingtone(
            context, RingtoneManager.getDefaultUri(
                RingtoneManager.TYPE_NOTIFICATION
            )
        )
        notificationSound.play()
    }

    @SuppressLint("MissingPermission")
    private fun createNotification(
        context: Context,
        id: Int,
        title: String,
        description: String,
        notificationIntent: Intent?
    ) {
        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = NotificationCompat.Builder(context, Notifications.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(description)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        val manager = NotificationManagerCompat.from(context)
        manager.notify(id, builder.build())
    }

    private fun getCorrectPackageName(packageName: String, context: Context): String {
        val packageManager = context.packageManager
        return try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            packageName
        } catch (e: PackageManager.NameNotFoundException) {
            context.packageName
        }
    }
}
