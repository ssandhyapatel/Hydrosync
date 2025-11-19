package com.hydrosync.mobile

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.hydrosync.mobile.ml.ModelInferenceScheduler
import com.hydrosync.mobile.settings.SettingsStore
import com.hydrosync.mobile.ui.settings.ThemeHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class HydroSyncApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()


        val settings = SettingsStore(this)
        ThemeHelper.applyTheme(settings.isDarkMode())


        ModelInferenceScheduler.schedule(this)
    }

    override fun getWorkManagerConfiguration() = Configuration.Builder()
        .setWorkerFactory(workerFactory)
        .build()
}