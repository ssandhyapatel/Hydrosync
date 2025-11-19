package com.hydrosync.mobile.repo

import android.util.Log
import com.hydrosync.mobile.ble.BleManager
import com.hydrosync.mobile.ble.BlePreferences
import com.hydrosync.mobile.data.SensorDao
import com.hydrosync.mobile.data.SensorReading
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensorRepository @Inject constructor(
    private val bleManager: BleManager,
    private val prefs: BlePreferences,
    private val sensorDao: SensorDao
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        // Listen for incoming data
        bleManager.onDataReceived = { bytes ->
            parseAndSave(bytes)
        }
    }

    fun connect(address: String) {
        prefs.setLastDeviceAddress(address)
        bleManager.connect(address)
    }

    fun autoConnect() {
        if (prefs.isAutoConnectEnabled()) {
            val lastAddress = prefs.getLastDeviceAddress()
            if (!lastAddress.isNullOrEmpty()) {
                bleManager.connect(lastAddress)
            }
        }
    }

    fun disconnect() = bleManager.disconnect()

    val connectionState = bleManager.connectionState

    private fun parseAndSave(bytes: ByteArray) {
        try {
            // Expected Payload: [Header(4), HR(1), GSR_L(1), GSR_H(1), Temp_L(1), Temp_H(1), ...]
            // Total size must be at least 9 bytes to contain all data.
            if (bytes.size >= 9) {
                // 1. Heart Rate (Byte 4) - Unsigned 8-bit
                val hr = bytes[4].toInt() and 0xFF

                // 2. GSR (Bytes 5,6) - Unsigned 16-bit Little Endian
                val gsrLow = bytes[5].toInt() and 0xFF
                val gsrHigh = bytes[6].toInt() and 0xFF
                val gsrRaw = (gsrHigh shl 8) or gsrLow

                // 3. Temperature (Bytes 7,8) - 16-bit scaled by 100 (e.g. 3650 -> 36.50 C)
                val tempLow = bytes[7].toInt() and 0xFF
                val tempHigh = bytes[8].toInt() and 0xFF
                val tempRaw = (tempHigh shl 8) or tempLow
                val temp = tempRaw / 100f

                // Data Sanity Check (Filter noise/zeros)
                if (hr > 0 && temp > 10f && temp < 50f) {
                    val reading = SensorReading(
                        timestamp = System.currentTimeMillis(),
                        hr = hr,
                        gsr = gsrRaw,
                        temp = temp
                    )
                    scope.launch { sensorDao.insert(reading) }
                    Log.d("SensorRepo", "Saved: HR=$hr, GSR=$gsrRaw, Temp=$temp")
                }
            }
        } catch (e: Exception) {
            Log.e("SensorRepo", "Parse Error", e)
        }
    }
}
