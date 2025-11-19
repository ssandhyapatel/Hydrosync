packagpackage com.hydrosync.mobile.ui.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
import com.hydrosync.mobile.data.PredictionDao
import com.hydrosync.mobile.data.PredictionEntity
import com.hydrosync.mobile.data.SensorDao
import com.hydrosync.mobile.data.SensorReading
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class TrendsViewModel @Inject constructor(
    private val sensorDao: SensorDao,
    private val predictionDao: PredictionDao // New injection
) : ViewModel() {

    enum class Mode { DAY, WEEK }

    private val _mode = MutableStateFlow(Mode.WEEK)
    val mode: StateFlow<Mode> = _mode.asStateFlow()

    private val _hydrationEntries = MutableStateFlow<List<Entry>>(emptyList())
    val hydrationEntries: StateFlow<List<Entry>> = _hydrationEntries.asStateFlow()

    private val _fatigueEntries = MutableStateFlow<List<Entry>>(emptyList())
    val fatigueEntries: StateFlow<List<Entry>> = _fatigueEntries.asStateFlow()

    private val _logs = MutableStateFlow<List<SensorReading>>(emptyList())
    val logs: StateFlow<List<SensorReading>> = _logs.asStateFlow()

    init {
        viewModelScope.launch {
            // Combine mode, raw readings, and AI predictions
            combine(
                _mode,
                sensorDao.allReadingsFlow(),
                predictionDao.allPredictionsFlow()
            ) { m, readings, predictions ->
                Triple(m, readings, predictions)
            }.collect { (m, readings, predictions) ->
                if (m == Mode.DAY) processDay(readings, predictions) else processWeek(readings, predictions)
            }
        }
    }

    fun setMode(m: Mode) { _mode.value = m }

    private fun processDay(readings: List<SensorReading>, predictions: List<PredictionEntity>) {
        val now = System.currentTimeMillis()

        // 1. Process Charts using AI Predictions (Hydration/Fatigue)
        val buckets = MutableList(24) { mutableListOf<PredictionEntity>() }
        for (p in predictions) {
            val hoursAgo = ((now - p.timestamp) / TimeUnit.HOURS.toMillis(1)).toInt()
            if (hoursAgo in 0..23) {
                buckets[23 - hoursAgo].add(p)
            }
        }

        val hydEntries = mutableListOf<Entry>()
        val fatEntries = mutableListOf<Entry>()

        for (i in 0..23) {
            val b = buckets[i]
            if (b.isNotEmpty()) {
                val avgH = b.map { it.hydrationPct }.average().toFloat()
                val avgF = b.map { it.fatiguePct }.average().toFloat()
                hydEntries.add(Entry(i.toFloat(), avgH))
                fatEntries.add(Entry(i.toFloat(), avgF))
            } else {
                // Use previous value to maintain line continuity, or 0 if start
                val prevH = if (hydEntries.isNotEmpty()) hydEntries.last().y else 0f
                val prevF = if (fatEntries.isNotEmpty()) fatEntries.last().y else 0f
                hydEntries.add(Entry(i.toFloat(), prevH))
                fatEntries.add(Entry(i.toFloat(), prevF))
            }
        }
        _hydrationEntries.value = hydEntries
        _fatigueEntries.value = fatEntries

        // 2. Process Logs (Keep using raw SensorReadings for detailed list)
        _logs.value = readings
            .filter { now - it.timestamp <= TimeUnit.HOURS.toMillis(24) }
            .sortedByDescending { it.timestamp }
    }

    private fun processWeek(readings: List<SensorReading>, predictions: List<PredictionEntity>) {
        val now = System.currentTimeMillis()
        val millisPerDay = TimeUnit.DAYS.toMillis(1)

        val buckets = MutableList(7) { mutableListOf<PredictionEntity>() }
        for (p in predictions) {
            val daysAgo = ((now - p.timestamp) / millisPerDay).toInt()
            if (daysAgo in 0..6) {
                buckets[6 - daysAgo].add(p)
            }
        }

        val hydEntries = mutableListOf<Entry>()
        val fatEntries = mutableListOf<Entry>()

        for (i in 0..6) {
            val b = buckets[i]
            if (b.isNotEmpty()) {
                val avgH = b.map { it.hydrationPct }.average().toFloat()
                val avgF = b.map { it.fatiguePct }.average().toFloat()
                hydEntries.add(Entry(i.toFloat(), avgH))
                fatEntries.add(Entry(i.toFloat(), avgF))
            } else {
                val prevH = if (hydEntries.isNotEmpty()) hydEntries.last().y else 0f
                val prevF = if (fatEntries.isNotEmpty()) fatEntries.last().y else 0f
                hydEntries.add(Entry(i.toFloat(), prevH))
                fatEntries.add(Entry(i.toFloat(), prevF))
            }
        }
        _hydrationEntries.value = hydEntries
        _fatigueEntries.value = fatEntries

        _logs.value = readings
            .filter { now - it.timestamp <= 7 * millisPerDay }
            .sortedByDescending { it.timestamp }
    }
}