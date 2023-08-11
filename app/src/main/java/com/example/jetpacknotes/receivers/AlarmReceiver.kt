package com.example.jetpacknotes.receivers

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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

class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminder = intent.getSerializableExtra("reminder") as Reminder
        createNotification(
            context,
            reminder.title,
            reminder.description,
            reminder.id,
            reminder.noteId,
            reminder.packageName,
            reminder.action
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

    private fun playNotificationSound(context: Context){
        val notificationSound = RingtoneManager.getRingtone(context, RingtoneManager.getDefaultUri(
            RingtoneManager.TYPE_NOTIFICATION))
        notificationSound.play()
    }
    @SuppressLint("MissingPermission")
    private fun createNotification(context:Context, title: String, description: String, reminderId:Int, noteId: Int?, packageName: String, action: ReminderAction){
        val notificationIntent = if (action == ReminderAction.OpenApp) context.packageManager.getLaunchIntentForPackage(packageName) else Intent(context, MainActivity::class.java).apply {
            putExtra("noteId", noteId!!)
        }
        notificationIntent?.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val pendingIntent = PendingIntent.getActivity(context, reminderId, notificationIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = NotificationCompat.Builder(context, Notifications.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(description)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        val manager = NotificationManagerCompat.from(context)
        manager.notify(reminderId, builder.build())
    }
}