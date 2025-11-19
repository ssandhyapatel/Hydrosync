package com.hydrosync.mobile.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.*
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log

class BleScanner(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var scanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
    private var scanning = false
    private var callback: ((BluetoothDevice) -> Unit)? = null
    private val handler = Handler(Looper.getMainLooper())

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            // return discovered device
            callback?.invoke(device)
        }
        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            results.forEach { callback?.invoke(it.device) }
        }
        override fun onScanFailed(errorCode: Int) {
            Log.w("BleScanner", "scan failed: $errorCode")
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan(timeoutMs: Long = 10000L, onDeviceFound: (BluetoothDevice) -> Unit) {
        if (scanning) return
        callback = onDeviceFound
        scanner = bluetoothAdapter?.bluetoothLeScanner
        scanner ?: return
        val filters = listOf<ScanFilter>() // no filter: list all
        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        scanner?.startScan(filters, settings, scanCallback)
        scanning = true
        handler.postDelayed({ stopScan() }, timeoutMs)
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (!scanning) return
        scanner?.stopScan(scanCallback)
        scanning = false
        callback = null
    }
}
