package io.herow.sdk.detection.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationHelper {

    const val CHANNEL_ID: String = "Campaign channel ID"

    fun setUpNotificationChannel(context: Context): NotificationManager {
        val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Notifications for campaign",
                NotificationManager.IMPORTANCE_HIGH
            )

            channel.setShowBadge(true)
            notificationManager.createNotificationChannel(channel)
        }

        return notificationManager
    }

    fun hashCode(string: String?): Int {
        val prime = 31
        return if (string != null) string.hashCode() * prime else 0 // PRIME = 31 or another prime number.
    }
}