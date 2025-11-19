package com.hydrosync.mobile.onboarding

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.hydrosync.mobile.DashboardActivity
import com.hydrosync.mobile.R
import com.hydrosync.mobile.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private val prefsName = "hydrosync_prefs"
    private val onboardKey = "shown_onboarding"

    // Launcher for multiple permissions (Bluetooth, Location)
    private val requestMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Proceed to notifications check regardless of result (for flow continuity)
        proceedAfterPermissionRequest()
    }

    // Launcher for Notification permission (Android 13+)
    private val requestNotification = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        finishOnboardingAndLaunchMain()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Check if onboarding was already shown
        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)
        if (prefs.getBoolean(onboardKey, false)) {
            goToMain()
            return
        }

        // 2. Setup Buttons
        binding.btnRequestPerms.setOnClickListener { requestPermissionsFlow() }
        binding.btnSkip.setOnClickListener { finishOnboardingAndLaunchMain() }
    }

    private fun requestPermissionsFlow() {
        val perms = mutableListOf<String>()

        // Bluetooth Permissions for Android 12+ (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            perms.add(Manifest.permission.BLUETOOTH_SCAN)
            perms.add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            // Older Android needs Location for BLE
            perms.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // Always add location if not present (some BLE scans require it explicitly)
        if (!perms.contains(Manifest.permission.ACCESS_FINE_LOCATION)) {
            perms.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // Filter only what is NOT granted
        val toRequest = perms.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (toRequest.isEmpty()) {
            proceedAfterPermissionRequest()
        } else {
            // Request the permissions
            requestMultiplePermissions.launch(toRequest)
        }
    }

    private fun proceedAfterPermissionRequest() {
        // Handle Android 13+ Notification Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                AlertDialog.Builder(this)
                    .setTitle(R.string.onboard_perm_title)
                    .setMessage(R.string.onboard_perm_msg)
                    .setPositiveButton(R.string.allow) { _, _ ->
                        requestNotification.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    .setNegativeButton(R.string.skip) { _, _ -> finishOnboardingAndLaunchMain() }
                    .show()
                return
            }
        }
        finishOnboardingAndLaunchMain()
    }

    private fun finishOnboardingAndLaunchMain() {
        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)
        prefs.edit().putBoolean(onboardKey, true).apply()
        goToMain()
    }

    private fun goToMain() {
        // Navigate to DashboardActivity (Your main entry point)
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}