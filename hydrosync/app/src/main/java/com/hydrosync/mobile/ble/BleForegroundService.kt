package com.hydrosync.mobile.ble

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ServiceCompat
import com.hydrosync.mobile.notifications.NotificationHelper
import com.hydrosync.mobile.repo.SensorRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class BleForegroundService : Service() {

    @Inject lateinit var repository: SensorRepository
    @Inject lateinit var notifHelper: NotificationHelper

    private val TAG = "BleForegroundService"
    private var serviceScope: CoroutineScope? = null

    override fun onCreate() {
        super.onCreate()

        // 1. Start Foreground immediately
        val notif = notifHelper.buildServiceNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NotificationHelper.SERVICE_NOTIF_ID,
                notif,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
            )
        } else {
            startForeground(NotificationHelper.SERVICE_NOTIF_ID, notif)
        }

        // 2. Start Scope
        serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        // 3. Auto-Connect
        serviceScope?.launch {
            try {
                Log.i(TAG, "Service started. Attempting auto-connect.")
                repository.autoConnect()
            } catch (e: Exception) {
                Log.e(TAG, "Error in auto-connect", e)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == NotificationHelper.ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        serviceScope?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}