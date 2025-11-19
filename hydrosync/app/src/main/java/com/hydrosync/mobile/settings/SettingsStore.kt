package com.hydrosync.mobile.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

class SettingsStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("hydrosync_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PUSH = "push_notifications"
        private const val KEY_DARK = "dark_mode"
        private const val KEY_HAPTIC = "haptic_feedback"
        private const val KEY_UNITS = "units" // "metric" or "imperial"
        private const val KEY_URGENT = "urgent_alerts"
    }

    fun setPushNotifications(enabled: Boolean) = prefs.edit().putBoolean(KEY_PUSH, enabled).apply()
    fun getPushNotifications() = prefs.getBoolean(KEY_PUSH, true)

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK, enabled).apply()
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
    fun isDarkMode() = prefs.getBoolean(KEY_DARK, false)

    fun setHaptic(enabled: Boolean) = prefs.edit().putBoolean(KEY_HAPTIC, enabled).apply()
    fun isHaptic() = prefs.getBoolean(KEY_HAPTIC, true)

    fun setUnits(units: String) = prefs.edit().putString(KEY_UNITS, units).apply()
    fun getUnits() = prefs.getString(KEY_UNITS, "metric") ?: "metric"

    fun setUrgentAlerts(enabled: Boolean) = prefs.edit().putBoolean(KEY_URGENT, enabled).apply()
    fun getUrgentAlerts() = prefs.getBoolean(KEY_URGENT, true)
}
