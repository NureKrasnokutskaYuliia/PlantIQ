package com.plantiq.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.plantiq.R

object NotificationHelper {
    const val CHANNEL_ID = "plantiq_alerts_v2"
    const val CHANNEL_NAME = "PlantIQ Сповіщення"
    const val CHANNEL_DESC = "Сповіщення про стан рослин та пристроїв"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                setShowBadge(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showNotification(context: Context, id: Int, title: String, message: String, priority: Int = NotificationCompat.PRIORITY_DEFAULT) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (e: SecurityException) { }
    }

    fun typeToTitle(type: Int): String = when (type) {
        0 -> "💧 Низька вологість"
        1 -> "💧 Висока вологість"
        2 -> "☀️ Мало світла"
        3 -> "☀️ Забагато світла"
        4 -> "🔋 Низький заряд батареї"
        else -> "🌿 PlantIQ"
    }

    fun toAndroidPriority(priority: Int): Int = when (priority) {
        0 -> NotificationCompat.PRIORITY_LOW
        1 -> NotificationCompat.PRIORITY_DEFAULT
        2 -> NotificationCompat.PRIORITY_HIGH
        3 -> NotificationCompat.PRIORITY_MAX
        else -> NotificationCompat.PRIORITY_DEFAULT
    }
}
