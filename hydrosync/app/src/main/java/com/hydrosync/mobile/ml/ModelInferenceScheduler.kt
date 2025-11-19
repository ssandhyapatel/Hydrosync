package com.hydrosync.mobile.ml

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ModelInferenceScheduler {

    private const val WORK_NAME = "hydrosync_ml_worker"

    fun schedule(context: Context) {
        // Run every 1 hour
        val req = PeriodicWorkRequestBuilder<ModelInferenceWorker>(1, TimeUnit.HOURS)
            .setInitialDelay(1, TimeUnit.MINUTES) // Delay slightly on first launch
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // KEEP ensures we don't restart it every app launch
            req
        )
    }
}