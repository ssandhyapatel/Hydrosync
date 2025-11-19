package com.hydrosync.mobile.wellness

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class WellnessViewModel @Inject constructor(
    application: Application,
    private val store: WellnessStore
) : AndroidViewModel(application) {

    private val _logs = MutableStateFlow<List<DrinkLog>>(emptyList())
    val logs: StateFlow<List<DrinkLog>> = _logs

    private val workManager = WorkManager.getInstance(application)

    init {
        _logs.value = store.getDrinkLogs()
    }

    fun logDrink(ml: Int = 500) {
        val ts = System.currentTimeMillis()
        store.addDrinkLog(ts, ml)
        _logs.value = store.getDrinkLogs()
    }

    fun setTimer(minutes: Long = 15) {
        // schedule one-time worker after 'minutes'
        val data = Data.Builder()
            .putString(ReminderWorker.KEY_TITLE, "Break time")
            .putString(ReminderWorker.KEY_MSG, "Take a 15-minute break now.")
            .build()

        val req = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(minutes, TimeUnit.MINUTES)
            .setInputData(data)
            .build()

        workManager.enqueue(req)
    }

    fun enableDailyNudge(enabled: Boolean) {
        store.setDailyNudge(enabled)
        if (enabled) scheduleDailyNudge() else cancelDailyNudge()
    }

    private fun scheduleDailyNudge() {
        // simple periodic work every 24 hours (you can refine with flex/window or exact alarm)
        val data = Data.Builder()
            .putString(ReminderWorker.KEY_TITLE, "Stand & Stretch")
            .putString(ReminderWorker.KEY_MSG, "Reminder: stand up and stretch.")
            .build()

        val req = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInputData(data)
            .build()

        workManager.enqueueUniquePeriodicWork("daily_nudge", ExistingPeriodicWorkPolicy.REPLACE, req)
    }

    private fun cancelDailyNudge() {
        workManager.cancelUniqueWork("daily_nudge")
    }

    fun setSmartMorning(enabled: Boolean) {
        store.setSmartMorning(enabled)
        // as example schedule 1 sync at 8am next day - for demo keep periodic daily at 8:00 using a more advanced scheduler
        if (enabled) scheduleMorning() else cancelMorning()
    }

    private fun scheduleMorning() {
        // for demo: schedule periodic 24-hour work with offset not implemented here.
        val data = Data.Builder()
            .putString(ReminderWorker.KEY_TITLE, "Morning sunlight")
            .putString(ReminderWorker.KEY_MSG, "Get some sunlight this morning.")
            .build()
        val req = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInputData(data)
            .build()
        workManager.enqueueUniquePeriodicWork("smart_morning", ExistingPeriodicWorkPolicy.REPLACE, req)
    }

    private fun cancelMorning() {
        workManager.cancelUniqueWork("smart_morning")
    }

    // helper to refresh in-memory logs
    fun refreshLogs() {
        viewModelScope.launch {
            _logs.value = store.getDrinkLogs()
        }
    }
}
