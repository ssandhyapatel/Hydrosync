package com.hydrosync.mobile.ui.personal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
import com.hydrosync.mobile.data.SensorDao
import com.hydrosync.mobile.data.SensorReading
import com.hydrosync.mobile.ml.FeatureBuilder
import com.hydrosync.mobile.ml.TFLiteModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class PersonalSummaryViewModel @Inject constructor(
    application: Application,
    private val sensorDao: SensorDao
) : AndroidViewModel(application) {

    private val context = application.applicationContext

    // StateFlows for UI
    private val _hydrationPct = MutableStateFlow(0f)
    val hydrationPct: StateFlow<Float> = _hydrationPct

    private val _fatiguePct = MutableStateFlow(0f)
    val fatiguePct: StateFlow<Float> = _fatiguePct

    private val _stressPct = MutableStateFlow(0f)
    val stressPct: StateFlow<Float> = _stressPct

    private val _confidence = MutableStateFlow(0)
    val confidence: StateFlow<Int> = _confidence

    private val _hydrationTrend = MutableStateFlow<List<Entry>>(emptyList())
    val hydrationTrend: StateFlow<List<Entry>> = _hydrationTrend

    private val _fatigueTrend = MutableStateFlow<List<Entry>>(emptyList())
    val fatigueTrend: StateFlow<List<Entry>> = _fatigueTrend

    // Attempt to load ML model
    private val model: TFLiteModel? = TFLiteModel.load(context)

    init {
        viewModelScope.launch {
            sensorDao.allReadingsFlow().collectLatest { list ->
                processReadings(list)
            }
        }
    }

    private fun processReadings(readings: List<SensorReading>) {
        if (readings.isEmpty()) {
            setDefaults()
            return
        }

        // 1. Try ML Prediction
        var mlSuccess = false
        if (model != null) {
            try {
                val inputs = FeatureBuilder.buildFeatures(readings)
                val outputs = model.run(inputs)

                if (outputs != null && outputs.size == 4) {
                    // Map ML outputs (assuming 0..100 or 0..1)
                    _hydrationPct.value = outputs[0].coerceIn(0f, 100f)
                    _fatiguePct.value = outputs[1].coerceIn(0f, 100f)
                    _stressPct.value = outputs[2].coerceIn(0f, 100f)
                    _confidence.value = outputs[3].coerceIn(0f, 100f).toInt()
                    mlSuccess = true
                }
            } catch (e: Exception) {
                // Ignore ML error, fallback to heuristic
            }
        }

        // 2. Fallback Heuristic (if ML failed or no model)
        if (!mlSuccess) {
            computeHeuristic(readings)
        }

        // 3. Always compute Chart Trends
        computeTrends(readings)
    }

    private fun computeHeuristic(readings: List<SensorReading>) {
        val avgGsr = readings.map { it.gsr }.average().toFloat()
        val avgHr = readings.map { it.hr }.average().toFloat()
        val varGsr = readings.map { (it.gsr - avgGsr) * (it.gsr - avgGsr) }.average().toFloat()

        val hydration = (avgGsr / 10f).coerceIn(0f, 100f)
        val fatigue = ((100f - avgHr)).coerceIn(0f, 100f)
        val stress = (varGsr / 50f * 100f).coerceIn(0f, 100f)

        _hydrationPct.value = hydration.roundToInt().toFloat()
        _fatiguePct.value = fatigue.roundToInt().toFloat()
        _stressPct.value = stress.roundToInt().toFloat()

        val recentCount = readings.count { System.currentTimeMillis() - it.timestamp <= TimeUnit.HOURS.toMillis(1) }
        _confidence.value = (50 + (recentCount * 2)).coerceIn(50, 90)
    }

    private fun computeTrends(readings: List<SensorReading>) {
        // Calculate trends (Last 7 days)
        val now = System.currentTimeMillis()
        val millisPerDay = TimeUnit.DAYS.toMillis(1)
        val buckets = MutableList(7) { mutableListOf<SensorReading>() }

        for (r in readings) {
            val daysAgo = ((now - r.timestamp) / millisPerDay).toInt()
            if (daysAgo in 0..6) {
                buckets[6 - daysAgo].add(r)
            }
        }

        val hEntries = mutableListOf<Entry>()
        val fEntries = mutableListOf<Entry>()

        for (i in 0..6) {
            val b = buckets[i]
            if (b.isNotEmpty()) {
                val avgG = b.map { it.gsr }.average().toFloat()
                val avgH = b.map { it.hr }.average().toFloat()
                hEntries.add(Entry(i.toFloat(), (avgG / 10f).coerceIn(0f, 100f)))
                fEntries.add(Entry(i.toFloat(), (100f - avgH).coerceIn(0f, 100f)))
            } else {
                hEntries.add(Entry(i.toFloat(), 0f))
                fEntries.add(Entry(i.toFloat(), 0f))
            }
        }
        _hydrationTrend.value = hEntries
        _fatigueTrend.value = fEntries
    }

    private fun setDefaults() {
        _hydrationPct.value = 0f
        _fatiguePct.value = 0f
        _stressPct.value = 0f
        _confidence.value = 0
        _hydrationTrend.value = emptyList()
        _fatigueTrend.value = emptyList()
    }
}