package com.hydrosync.mobile.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.hydrosync.mobile.DashboardActivity
import com.hydrosync.mobile.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ALERTS_ID = "hydrosync_alerts"
        const val CHANNEL_SERVICE_ID = "hydrosync_service"
        const val SERVICE_NOTIF_ID = 101
        const val ACTION_STOP = "STOP_SERVICE"
    }

    init {
        createChannels()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = context.getSystemService(NotificationManager::class.java)

            // 1. Alerts Channel (High Importance)
            val alertsChannel = NotificationChannel(
                CHANNEL_ALERTS_ID,
                "Health Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent hydration and fatigue warnings"
                enableVibration(true)
            }

            // 2. Service Channel (Low Importance - Silent)
            val serviceChannel = NotificationChannel(
                CHANNEL_SERVICE_ID,
                "Background Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps HydroSync running in the background"
                setShowBadge(false)
            }

            mgr.createNotificationChannels(listOf(alertsChannel, serviceChannel))
        }
    }

    // --- For BleForegroundService ---
    fun buildServiceNotification(): Notification {
        val stopIntent = Intent(context, com.hydrosync.mobile.ble.BleForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val pendingStop = PendingIntent.getService(context, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        val openIntent = Intent(context, DashboardActivity::class.java)
        val pendingOpen = PendingIntent.getActivity(context, 0, openIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(context, CHANNEL_SERVICE_ID)
            .setContentTitle("HydroSync Active")
            .setContentText("Monitoring your sensors in background...")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Ensure this icon exists, or use R.mipmap.ic_launcher
            .setContentIntent(pendingOpen)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", pendingStop)
            .setOngoing(true)
            .build()
    }

    // --- For Alerts (Worker/ViewModel) ---
    fun postUrgentAlertNotification(id: Long, title: String, message: String) {
        val intent = Intent(context, DashboardActivity::class.java)
        val pending = PendingIntent.getActivity(context, id.toInt(), intent, PendingIntent.FLAG_IMMUTABLE)

        val notif = NotificationCompat.Builder(context, CHANNEL_ALERTS_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        val mgr = context.getSystemService(NotificationManager::class.java)
        mgr.notify(id.toInt(), notif)
    }
}