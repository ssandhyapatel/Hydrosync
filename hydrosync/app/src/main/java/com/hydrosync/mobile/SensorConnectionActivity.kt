package com.hydrosync.mobile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.hydrosync.mobile.ble.BleDeviceAdapter
import com.hydrosync.mobile.ble.BleScanner
import com.hydrosync.mobile.databinding.ActivitySensorConnectionBinding
import com.hydrosync.mobile.repo.SensorRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SensorConnectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySensorConnectionBinding

    @Inject lateinit var bleScanner: BleScanner
    @Inject lateinit var repository: SensorRepository

    private lateinit var deviceAdapter: BleDeviceAdapter
    private var isScanning = false

    // Permission Launcher
    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            startScan()
        } else {
            Toast.makeText(this, "Permissions needed to scan", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySensorConnectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        checkPermissionsAndInit()
    }

    private fun setupUI() {
        // RecyclerView
        deviceAdapter = BleDeviceAdapter { device ->
            connectToDevice(device)
        }
        binding.rvDevices.layoutManager = LinearLayoutManager(this)
        binding.rvDevices.adapter = deviceAdapter

        // Scan Button
        binding.btnScan.setOnClickListener {
            if (isScanning) stopScan() else checkPermissionsAndInit()
        }

        // Continue Button (Skip or Proceed)
        binding.btnContinue.setOnClickListener {
            navigateToDashboard()
        }
    }

    private fun checkPermissionsAndInit() {
        val perms = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            perms.add(Manifest.permission.BLUETOOTH_SCAN)
            perms.add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            perms.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        val missing = perms.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isEmpty()) {
            startScan()
        } else {
            requestPermissions.launch(missing.toTypedArray())
        }
    }

    private fun startScan() {
        isScanning = true
        binding.btnScan.text = "Stop Scan"
        binding.tvStatus.text = "Status: Scanning..."

        // Clear old list
        deviceAdapter.submitList(emptyList())

        // Fixed: Correct method call matching BleScanner.kt definition
        bleScanner.startScan(timeoutMs = 10000L) { device ->
            runOnUiThread {
                // Prevent duplicates in the UI list
                val currentList = deviceAdapter.currentList.toMutableList()
                if (currentList.none { it.address == device.address }) {
                    currentList.add(device)
                    deviceAdapter.submitList(currentList)
                }
            }
        }
    }

    private fun stopScan() {
        bleScanner.stopScan()
        isScanning = false
        binding.btnScan.text = "Start Scan"
        binding.tvStatus.text = "Status: Stopped"
    }

    private fun connectToDevice(device: android.bluetooth.BluetoothDevice) {
        binding.tvStatus.text = "Connecting to ${device.name ?: "Unknown"}..."

        lifecycleScope.launch {
            repository.connect(device.address)
            Toast.makeText(this@SensorConnectionActivity, "Connected!", Toast.LENGTH_SHORT).show()
            navigateToDashboard()
        }
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}