package com.hydrosync.mobile

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.hydrosync.mobile.databinding.ActivityPersonalSummaryBinding
import com.hydrosync.mobile.ui.personal.PersonalSummaryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PersonalSummaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersonalSummaryBinding
    private val vm: PersonalSummaryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeViewModel()
        styleCharts()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            vm.hydrationPct.collectLatest { pct ->
                binding.cpbHydration.setProgressWithAnimation(pct, 800)
            }
        }
        lifecycleScope.launch {
            vm.fatiguePct.collectLatest { pct ->
                binding.cpbFatigue.setProgressWithAnimation(pct, 800)
            }
        }
        lifecycleScope.launch {
            vm.stressPct.collectLatest { pct ->
                binding.cpbStress.setProgressWithAnimation(pct, 800)
            }
        }
        lifecycleScope.launch {
            vm.confidence.collectLatest { conf ->
                binding.tvConfidence.text = "$conf% Confidence"
                binding.seekPrediction.progress = conf
            }
        }

        lifecycleScope.launch {
            vm.hydrationTrend.collectLatest { entries ->
                // Fixed: Pass brand color resource
                updateLineChart(binding.lineRecentHydration, entries, R.color.brand_primary)
            }
        }

        lifecycleScope.launch {
            vm.fatigueTrend.collectLatest { entries ->
                // Fixed: Pass warning color resource for fatigue
                updateLineChart(binding.lineRecentFatigue, entries, R.color.accent_warn)
            }
        }

        lifecycleScope.launch {
            vm.hydrationPct.collectLatest { h ->
                val status = if (h >= 75f) "Excellent" else if (h >= 50f) "Fair" else "Low"
                binding.tvOverallStatus.text = "Overall Status: $status"
            }
        }
    }

    private fun styleCharts() {
        val charts = listOf(binding.lineRecentHydration, binding.lineRecentFatigue)
        for (c in charts) {
            val desc = Description()
            desc.text = ""
            c.description = desc
            c.axisLeft.isEnabled = false
            c.axisRight.isEnabled = false
            c.xAxis.position = XAxis.XAxisPosition.BOTTOM
            c.xAxis.setDrawGridLines(false)
            c.legend.isEnabled = false
            c.setTouchEnabled(false)
        }
    }


    private fun updateLineChart(
        chart: com.github.mikephil.charting.charts.LineChart,
        entries: List<Entry>,
        colorResId: Int
    ) {
        val set = LineDataSet(entries, "trend")
        set.color = ContextCompat.getColor(this, colorResId)
        set.lineWidth = 2f
        set.setDrawCircles(false)
        set.setDrawValues(false)
        set.mode = LineDataSet.Mode.CUBIC_BEZIER
        chart.data = LineData(set)
        chart.invalidate()
    }
}