package com.hydrosync.mobile.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
import com.hydrosync.mobile.data.SensorDao
import com.hydrosync.mobile.data.SensorReading
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val sensorDao: SensorDao
) : ViewModel() {

    private val _hydrationPct = MutableStateFlow(0f)
    val hydrationPct: StateFlow<Float> = _hydrationPct

    private val _fatiguePct = MutableStateFlow(0f)
    val fatiguePct: StateFlow<Float> = _fatiguePct

    private val _lastSync = MutableStateFlow<String>("-")
    val lastSync: StateFlow<String> = _lastSync

    private val _miniTrend = MutableStateFlow<List<Entry>>(emptyList())
    val miniTrend: StateFlow<List<Entry>> = _miniTrend

    private val _recentLogs = MutableStateFlow<List<SensorReading>>(emptyList())
    val recentLogs: StateFlow<List<SensorReading>> = _recentLogs

    init {
        viewModelScope.launch {
            sensorDao.allReadingsFlow().collectLatest { readings ->
                computeSummary(readings)
            }
        }
    }

    private fun computeSummary(readings: List<SensorReading>) {
        if (readings.isEmpty()) {
            _hydrationPct.value = 75f
            _fatiguePct.value = 25f
            _lastSync.value = "-"
            _miniTrend.value = (0..6).map { Entry(it.toFloat(), (30 + it * 5).toFloat()) }
            _recentLogs.value = emptyList()
            return
        }

        val latest = readings.maxByOrNull { it.timestamp }!!
        _lastSync.value = readableTime(latest.timestamp)

        val avgGsr = readings.map { it.gsr }.average().toFloat()
        val avgHr = readings.map { it.hr }.average().toFloat()

        // Simple logic: High GSR = High Hydration, Low HR = Low Fatigue
        val hydration = (avgGsr / 10f).coerceIn(0f, 100f)
        val fatigue = ((100f - avgHr)).coerceIn(0f, 100f)

        _hydrationPct.value = hydration.roundToInt().toFloat()
        _fatiguePct.value = fatigue.roundToInt().toFloat()

        val last8 = readings.sortedBy { it.timestamp }.takeLast(8)
        val entries = last8.mapIndexed { idx, r -> Entry(idx.toFloat(), (r.gsr / 10f).coerceAtMost(100f)) }
        _miniTrend.value = if (entries.isNotEmpty()) entries else (0..6).map { Entry(it.toFloat(), 0f) }

        _recentLogs.value = readings.sortedByDescending { it.timestamp }.take(10)
    }

    private fun readableTime(ts: Long): String {
        return try {
            val df = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            df.format(Date(ts))
        } catch (e: Exception) {
            "-"
        }
    }
}