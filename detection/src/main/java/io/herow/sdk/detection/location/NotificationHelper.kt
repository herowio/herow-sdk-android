package io.herow.sdk.detection.location

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import io.herow.sdk.detection.R
import java.util.*

object NotificationHelper {
    private const val FOREGROUND_NOTIFICATION_ID = 15_951

    private const val CHANNEL_ID = "com.connecthings.connectplace.geodetection"
    private const val CHANNEL_NAME = "Herow"
    private const val CHANNEL_DESCRIPTION = "Here and now!"

    @SuppressLint("NewApi")
    fun createNotificationChannel(context: Context) {
        val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance: Int = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
            channel.setSound(null, null)
            channel.description = CHANNEL_DESCRIPTION
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            channel.enableVibration(false)
            channel.setShowBadge(true)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun foregroundNotification(context: Context, id: UUID): ForegroundInfo {
        val pendingIntent = WorkManager.getInstance(context).createCancelPendingIntent(id)
        val notificationTitle = context.resources.getString(R.string.foreground_notification_title)
        val notificationDescription = context.resources.getString(R.string.foreground_notification_description)
        val deleteActionTitle = context.resources.getString(R.string.foreground_notification_delete_action)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_notification)
            .setContentTitle(notificationTitle)
            .setContentText(notificationDescription)
            .addAction(R.drawable.ic_close, deleteActionTitle, pendingIntent)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                FOREGROUND_NOTIFICATION_ID,
                notification,
                FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            ForegroundInfo(FOREGROUND_NOTIFICATION_ID, notification)
        }
    }
}