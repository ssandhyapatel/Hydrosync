package com.hydrosync.mobile.ble

import android.content.Context

class BlePreferences(context: Context) {
    private val prefs = context.getSharedPreferences("hydrosync_ble_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LAST_ADDR = "last_device_address"
        private const val KEY_AUTO_CONNECT = "auto_connect_enabled"
    }

    fun setLastDeviceAddress(address: String?) {
        prefs.edit().putString(KEY_LAST_ADDR, address).apply()
    }

    fun getLastDeviceAddress(): String? = prefs.getString(KEY_LAST_ADDR, null)

    fun setAutoConnectEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_CONNECT, enabled).apply()
    }

    fun isAutoConnectEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_CONNECT, true)
}