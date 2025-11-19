package com.hydrosync.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hydrosync.mobile.databinding.ActivityTrendsBinding
import com.hydrosync.mobile.ui.trends.TrendsLogAdapter
import com.hydrosync.mobile.ui.trends.TrendsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class TrendsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTrendsBinding
    private val vm: TrendsViewModel by viewModels()
    private lateinit var adapter: TrendsLogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrendsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupUI()
        observeViewModel()
    }

    private fun setupNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.selectedItemId = R.id.navigation_trends

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_trends -> true
                R.id.navigation_dashboard -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
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
    }

    private fun setupUI() {
        // Charts styling
        styleChart(binding.chartHydration)
        styleChart(binding.chartFatigue)

        // Recycler View
        adapter = TrendsLogAdapter(mutableListOf())
        binding.rvTrendsLogs.layoutManager = LinearLayoutManager(this)
        binding.rvTrendsLogs.adapter = adapter

        // Toggle Buttons
        binding.tvDay.setOnClickListener { vm.setMode(TrendsViewModel.Mode.DAY) }
        binding.tvWeek.setOnClickListener { vm.setMode(TrendsViewModel.Mode.WEEK) }

        // Export Button
        binding.btnExport.setOnClickListener { exportDataToCSV() }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            vm.mode.collectLatest { mode ->
                updateTabs(mode == TrendsViewModel.Mode.DAY)
            }
        }

        lifecycleScope.launch {
            vm.hydrationEntries.collectLatest { entries ->
                val set = LineDataSet(entries, "Hydration")
                set.color = ContextCompat.getColor(this@TrendsActivity, R.color.brand_primary)
                set.setDrawCircles(false)
                set.lineWidth = 2f
                set.mode = LineDataSet.Mode.CUBIC_BEZIER
                set.setDrawValues(false)
                binding.chartHydration.data = LineData(set)
                binding.chartHydration.invalidate()
            }
        }

        lifecycleScope.launch {
            vm.fatigueEntries.collectLatest { entries ->
                val set = LineDataSet(entries, "Fatigue")
                set.color = ContextCompat.getColor(this@TrendsActivity, R.color.accent_warn)
                set.setDrawCircles(false)
                set.lineWidth = 2f
                set.mode = LineDataSet.Mode.CUBIC_BEZIER
                set.setDrawValues(false)
                binding.chartFatigue.data = LineData(set)
                binding.chartFatigue.invalidate()
            }
        }

        lifecycleScope.launch {
            vm.logs.collectLatest { list ->
                adapter.setAll(list)
            }
        }
    }

    private fun updateTabs(isDay: Boolean) {
        val activeColor = ContextCompat.getColor(this, R.color.brand_primary)
        val inactiveColor = ContextCompat.getColor(this, R.color.text_tertiary)

        if (isDay) {
            binding.tvDay.setTextColor(activeColor)
            binding.tvWeek.setTextColor(inactiveColor)
        } else {
            binding.tvDay.setTextColor(inactiveColor)
            binding.tvWeek.setTextColor(activeColor)
        }
    }

    private fun styleChart(chart: com.github.mikephil.charting.charts.LineChart) {
        val desc = Description()
        desc.text = ""
        chart.description = desc
        chart.axisRight.isEnabled = false
        chart.xAxis.isEnabled = false
        chart.legend.isEnabled = false
        chart.setTouchEnabled(true)
        chart.setDrawGridBackground(false)
    }

    private fun exportDataToCSV() {
        val data = adapter.getItems()
        if (data.isEmpty()) {
            Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show()
            return
        }

        val sb = StringBuilder()
        sb.append("Timestamp,DateTime,HR,GSR,Temp\n")
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        for (r in data) {
            sb.append("${r.timestamp},${df.format(Date(r.timestamp))},${r.hr},${r.gsr},${r.temp}\n")
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "HydroSync Data Export")
            putExtra(Intent.EXTRA_TEXT, sb.toString())
        }
        startActivity(Intent.createChooser(intent, "Export CSV using..."))
    }
}
