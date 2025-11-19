package com.hydrosync.mobile.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@SuppressLint("MissingPermission")
class BleManager @Inject constructor(private val context: Context) {

    companion object {
        private const val TAG = "BleManager"

        // Device UUID Configuration
        val SERVICE_UUID: UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")
        val CHAR_DATA_UUID: UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")
        val CLIENT_CONFIG_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }

    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val btAdapter: BluetoothAdapter? by lazy {
        val mgr = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mgr.adapter
    }

    private var bluetoothGatt: BluetoothGatt? = null
    private var deviceAddress: String? = null

    // State Flow for UI
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    // Reconnection Logic
    private val retryCounter = AtomicInteger(0)
    private var reconnectJob: Job? = null

    // Data Callback
    var onDataReceived: ((ByteArray) -> Unit)? = null

    fun connect(address: String) {
        if (deviceAddress == address && _connectionState.value == ConnectionState.CONNECTED) {
            return
        }

        reconnectJob?.cancel()
        deviceAddress = address
        _connectionState.value = ConnectionState.CONNECTING

        val device = btAdapter?.getRemoteDevice(address)
        if (device == null) {
            _connectionState.value = ConnectionState.FAILED
            scheduleReconnect()
            return
        }

        bluetoothGatt?.close()
        bluetoothGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            device.connectGatt(context, false, gattCallback)
        }
        retryCounter.set(0)
    }

    fun disconnect() {
        reconnectJob?.cancel()
        _connectionState.value = ConnectionState.DISCONNECTING
        try {
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Disconnect failed", e)
        } finally {
            bluetoothGatt = null
            deviceAddress = null
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }

    private fun scheduleReconnect() {
        if (deviceAddress.isNullOrBlank()) return

        reconnectJob?.cancel()
        reconnectJob = mainScope.launch {
            val attempt = retryCounter.incrementAndGet()
            val delayMs = (2000L * attempt).coerceAtMost(60000L) // Max 1 min delay
            Log.d(TAG, "Reconnecting in ${delayMs}ms (Attempt $attempt)")
            delay(delayMs)
            deviceAddress?.let { connect(it) }
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                bluetoothGatt = gatt
                _connectionState.value = ConnectionState.CONNECTED
                retryCounter.set(0)
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                _connectionState.value = ConnectionState.DISCONNECTED
                bluetoothGatt?.close()
                scheduleReconnect()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt.getService(SERVICE_UUID)
                val characteristic = service?.getCharacteristic(CHAR_DATA_UUID)

                if (characteristic != null) {
                    gatt.setCharacteristicNotification(characteristic, true)
                    val descriptor = characteristic.getDescriptor(CLIENT_CONFIG_UUID)
                    descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(descriptor)
                }
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            val data = characteristic.value
            onDataReceived?.invoke(data)
        }
    }
}
