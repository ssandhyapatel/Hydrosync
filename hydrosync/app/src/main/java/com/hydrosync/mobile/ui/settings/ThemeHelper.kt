package com.hydrosync.mobile.ui.settings

import androidx.appcompat.app.AppCompatDelegate

object ThemeHelper {
    fun applyTheme(isDarkMode: Boolean) {
        val mode = if (isDarkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}