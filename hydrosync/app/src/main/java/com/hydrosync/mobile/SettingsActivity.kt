package com.hydrosync.mobile

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hydrosync.mobile.databinding.ActivitySettingsBinding
import com.hydrosync.mobile.settings.SettingsStore

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var store: SettingsStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.selectedItemId = R.id.navigation_settings // Highlight Settings tab

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_settings -> return@setOnItemSelectedListener true // Already here

                R.id.navigation_dashboard -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    overridePendingTransition(0, 0)
                    return@setOnItemSelectedListener true
                }
                R.id.navigation_trends -> {
                    startActivity(Intent(this, TrendsActivity::class.java))
                    overridePendingTransition(0, 0)
                    return@setOnItemSelectedListener true
                }
                R.id.navigation_alerts -> {
                    startActivity(Intent(this, AlertsActivity::class.java))
                    overridePendingTransition(0, 0)
                    return@setOnItemSelectedListener true
                }
            }
            false
        }

        store = SettingsStore(this)

        loadSettings()
        setupListeners()
    }

    private fun loadSettings() {
        binding.swPush.isChecked = store.getPushNotifications()
        binding.swDarkMode.isChecked = store.isDarkMode()
        binding.swHaptic.isChecked = store.isHaptic()
        binding.swUrgentAlerts.isChecked = store.getUrgentAlerts()

        val units = store.getUnits()
        if (units == "metric") binding.rbMetric.isChecked = true
        else binding.rbImperial.isChecked = true

        // Connected device example
        binding.tvConnectedDevice.text = "HydroSync-123"
    }

    private fun setupListeners() {

        binding.swPush.setOnCheckedChangeListener { _, v ->
            store.setPushNotifications(v)
        }

        binding.swDarkMode.setOnCheckedChangeListener { _, v ->
            store.setDarkMode(v)
        }

        binding.swHaptic.setOnCheckedChangeListener { _, v ->
            store.setHaptic(v)
        }

        binding.swUrgentAlerts.setOnCheckedChangeListener { _, v ->
            store.setUrgentAlerts(v)
        }

        binding.rgUnits.setOnCheckedChangeListener { _, id ->
            if (id == binding.rbMetric.id) store.setUnits("metric")
            else store.setUnits("imperial")
        }

        binding.tvLogout.setOnClickListener {
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
        }

        binding.tvDeleteAccount.setOnClickListener {
            Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show()
        }

        binding.btnManageDevice.setOnClickListener {
            Toast.makeText(this, "Manage device clicked", Toast.LENGTH_SHORT).show()
        }
    }
}
