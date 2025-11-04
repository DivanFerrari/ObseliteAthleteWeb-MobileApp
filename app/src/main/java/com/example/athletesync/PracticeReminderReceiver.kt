package com.example.athletesync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class PracticeReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("practice_title") ?: "Practice"
        val location = intent.getStringExtra("practice_location") ?: "Unknown location"
        val time = intent.getStringExtra("practice_time") ?: "Unknown time"

        showNotification(context, title, location, time)
    }

    private fun showNotification(context: Context, title: String, location: String, time: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "practice_reminders",
                "Practice Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for scheduled practices"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, "practice_reminders")
            .setContentTitle("🏈 Practice Reminder")
            .setContentText("$title at $time")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("🔔 Practice Reminder\n\nPractice: $title\n⏰ Time: $time\n📍 Location: $location\n\nDon't forget your practice session!"))
            .setSmallIcon(R.drawable.ic_soccer_ball)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}