package com.blank.commutetrack.feature.tracking.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.blank.commutetrack.feature.tracking.service.TrackingService

/**
 * Helper class for managing tracking notifications.
 */
object TrackingNotificationHelper {
    
    const val CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_ID = 1001
    
    // Actions
    const val ACTION_PAUSE = "com.blank.commutetrack.action.PAUSE"
    const val ACTION_RESUME = "com.blank.commutetrack.action.RESUME"
    const val ACTION_STOP = "com.blank.commutetrack.action.STOP"
    
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Trip Tracking"
            val descriptionText = "Ongoing trip tracking notifications"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setSound(null, null)
                enableVibration(false)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun buildNotification(
        context: Context,
        elapsedTime: String,
        startLocation: String,
        isPaused: Boolean,
        pauseCount: Int
    ): Notification {
        // Create intents for actions
        val pauseIntent = PendingIntent.getService(
            context,
            0,
            Intent(context, TrackingService::class.java).apply {
                action = if (isPaused) ACTION_RESUME else ACTION_PAUSE
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val stopIntent = PendingIntent.getService(
            context,
            1,
            Intent(context, TrackingService::class.java).apply {
                action = ACTION_STOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Content intent - opens the app when notification is tapped
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            context.packageManager.getLaunchIntentForPackage(context.packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val statusText = if (isPaused) "⏸ PAUSED" else "▶ ACTIVE"
        val pauseButtonText = if (isPaused) "▶ RESUME" else "⏸ PAUSE"
        
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Trip in Progress - $statusText")
            .setContentText("⏱ $elapsedTime  •  From: $startLocation")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation) // Default location icon
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true) // Cannot be dismissed by user
            .setContentIntent(contentIntent)
            .setOnlyAlertOnce(true)
            .addAction(android.R.drawable.ic_media_pause, pauseButtonText, pauseIntent)
            .addAction(android.R.drawable.ic_delete, "⏹ STOP", stopIntent)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("⏱ $elapsedTime  •  From: $startLocation${if (pauseCount > 0) "  •  Pauses: $pauseCount" else ""}")
            )
            .build()
    }
}
