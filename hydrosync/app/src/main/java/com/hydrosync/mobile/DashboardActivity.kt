package com.hydrosync.mobile

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hydrosync.mobile.databinding.ActivityDashboardBinding
import com.hydrosync.mobile.ui.dashboard.DashboardViewModel
import com.hydrosync.mobile.ui.dashboard.RecentLogAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val vm: DashboardViewModel by viewModels()
    private lateinit var adapter: RecentLogAdapter

    @Inject
    lateinit var repository: com.hydrosync.mobile.repo.SensorRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupUI()
        observeData()

        repository.autoConnect()
    }

    private fun setupNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.selectedItemId = R.id.navigation_dashboard

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> true
                R.id.navigation_trends -> {
                    startActivity(Intent(this, TrendsActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.navigation_alerts -> {
                    startActivity(Intent(this, AlertsActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.navigation_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }

        binding.btnOpenPersonal.setOnClickListener {
            startActivity(Intent(this, PersonalSummaryActivity::class.java))
        }
        binding.btnOpenWellness.setOnClickListener {
            startActivity(Intent(this, WellnessActivity::class.java))
        }
        binding.btnViewHistory.setOnClickListener {
            startActivity(Intent(this, PredictionHistoryActivity::class.java))
        }
    }

    private fun setupUI() {
        adapter = RecentLogAdapter(mutableListOf())
        binding.rvRecent.layoutManager = LinearLayoutManager(this)
        binding.rvRecent.adapter = adapter

        binding.lineMini.apply {
            description = Description().apply { text = "" }
            axisLeft.isEnabled = false
            axisRight.isEnabled = false
            xAxis.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(false)
        }
    }

    private fun observeData() {
        // Hydration Progress
        lifecycleScope.launch {
            vm.hydrationPct.collectLatest { pct ->
                binding.cpbHydration.setProgressWithAnimation(pct, 1000)
            }
        }

        lifecycleScope.launch {
            vm.fatiguePct.collectLatest { pct ->
                binding.cpbFatigue.setProgressWithAnimation(pct, 1000)
            }
        }

        lifecycleScope.launch {
            vm.lastSync.collectLatest { time ->
                binding.tvLastSync.text = "Last synced: $time"
            }
        }

        lifecycleScope.launch {
            repository.connectionState.collectLatest { state ->
                val (text, colorRes) = when (state) {
                    com.hydrosync.mobile.ble.ConnectionState.CONNECTED ->
                        "Connected" to R.color.brand_primary
                    com.hydrosync.mobile.ble.ConnectionState.CONNECTING ->
                        "Connecting..." to R.color.accent_info
                    else ->
                        "Not Connected" to R.color.accent_warn
                }
                binding.tvConnectionState.text = text
                binding.tvConnectionState.setTextColor(ContextCompat.getColor(this@DashboardActivity, colorRes))
            }
        }


        lifecycleScope.launch {
            vm.miniTrend.collectLatest { entries ->
                val set = LineDataSet(entries, "Trend")
                set.color = ContextCompat.getColor(this@DashboardActivity, R.color.brand_primary)
                set.setDrawCircles(false)
                set.lineWidth = 2f
                val data = LineData(set)
                binding.lineMini.data = data
                binding.lineMini.invalidate()
            }
        }


        lifecycleScope.launch {
            vm.recentLogs.collectLatest { logs ->
                adapter.setAll(logs)
            }
        }
    }
}
