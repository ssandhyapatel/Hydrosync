package com.hydrosync.mobile.wellness

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hydrosync.mobile.notifications.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val notifier: NotificationHelper
) : CoroutineWorker(appContext, params) {

    companion object {
        const val KEY_TITLE = "title"
        const val KEY_MSG = "message"
    }

    override suspend fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE) ?: "HydroSync reminder"
        val msg = inputData.getString(KEY_MSG) ?: "Time for a quick break."
        // post notification (NotificationHelper will create channel)
        notifier.postUrgentAlertNotification((System.currentTimeMillis() % Int.MAX_VALUE).toInt().toLong(), title, msg)
        return Result.success()
    }
}
