package com.hydrosync.mobile

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.hydrosync.mobile.databinding.ActivityPredictionHistoryBinding
import com.hydrosync.mobile.ui.predictions.PredictionsAdapter
import com.hydrosync.mobile.ui.predictions.PredictionsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PredictionHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPredictionHistoryBinding
    private val vm: PredictionsViewModel by viewModels()
    private lateinit var adapter: PredictionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPredictionHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeData()
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = PredictionsAdapter(mutableListOf())
        binding.rvPredictions.layoutManager = LinearLayoutManager(this)
        binding.rvPredictions.adapter = adapter

        binding.chartSpark.apply {
            axisLeft.isEnabled = false
            axisRight.isEnabled = false
            xAxis.isEnabled = false
            legend.isEnabled = false
            description = Description().apply { text = "" }
            setTouchEnabled(false)
            setDrawGridBackground(false)
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            vm.predictions.collectLatest { list ->
                adapter.setAll(list)

                if (list.isNotEmpty()) {
                    val entries = list.take(20).reversed().mapIndexed { index, p ->
                        Entry(index.toFloat(), p.hydrationPct)
                    }

                    val set = LineDataSet(entries, "Hydration Trend")
                    // Fixed: Use Theme Color instead of hardcoded hex
                    set.color = ContextCompat.getColor(this@PredictionHistoryActivity, R.color.brand_primary)
                    set.lineWidth = 2f
                    set.setDrawCircles(false)
                    set.setDrawValues(false)
                    set.mode = LineDataSet.Mode.CUBIC_BEZIER

                    binding.chartSpark.data = LineData(set)
                    binding.chartSpark.invalidate()
                }
            }
        }
    }
}