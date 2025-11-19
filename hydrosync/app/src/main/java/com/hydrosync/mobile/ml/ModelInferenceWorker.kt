package com.hydrosync.mobile.ml

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hydrosync.mobile.data.AlertEntity
import com.hydrosync.mobile.data.PredictionEntity
import com.hydrosync.mobile.data.SensorDao
import com.hydrosync.mobile.repo.AlertsRepository
import com.hydrosync.mobile.repo.PredictionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@HiltWorker
class ModelInferenceWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val sensorDao: SensorDao,
    private val alertsRepo: AlertsRepository,
    private val predictionRepo: PredictionRepository // <--- Injected
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val TAG = "ModelInferenceWorker"
        private const val FATIGUE_THRESHOLD = 80f
        private const val STRESS_THRESHOLD = 75f
        private const val CONFIDENCE_MIN = 40
        private const val DUP_WINDOW_MS = 4L * 60L * 60L * 1000L
        private const val HISTORY_MS = 60L * 60L * 1000L
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val now = System.currentTimeMillis()
            val all = sensorDao.allReadingsOnce()
            val recent = all.filter { now - it.timestamp <= HISTORY_MS }

            // 1. Run Inference
            val model = TFLiteModel.load(applicationContext)
            var isModel = false

            val resultArray: FloatArray? = if (model != null) {
                try {
                    val features = FeatureBuilder.buildFeatures(recent)
                    val res = model.run(features)
                    isModel = true
                    res
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }

            val (hydration, fatigue, stress, confidence) = if (resultArray != null && resultArray.size >= 4) {
                Quad(
                    resultArray[0].coerceIn(0f, 100f),
                    resultArray[1].coerceIn(0f, 100f),
                    resultArray[2].coerceIn(0f, 100f),
                    resultArray[3].coerceIn(0f, 100f).toInt()
                )
            } else {
                isModel = false
                computeHeuristic(recent)
            }

            // 2. SAVE PREDICTION HISTORY (New Step)
            val prediction = PredictionEntity(
                timestamp = now,
                hydrationPct = hydration,
                fatiguePct = fatigue,
                stressPct = stress,
                confidencePct = confidence,
                source = if (isModel) "model" else "heuristic"
            )
            predictionRepo.insert(prediction)
            Log.d(TAG, "Saved prediction: $prediction")

            // 3. Alert Logic (Existing)
            val severityLabel = when {
                fatigue >= FATIGUE_THRESHOLD || stress >= STRESS_THRESHOLD -> "URGENT"
                fatigue >= 60f || stress >= 60f -> "MILD"
                else -> "INFO"
            }

            val lastTs = alertsRepo.getLatestTimestampBySeverity(severityLabel) ?: 0L
            val shouldInsert = (now - lastTs) >= DUP_WINDOW_MS

            if (shouldInsert && confidence >= CONFIDENCE_MIN && severityLabel != "INFO") {
                val title = when (severityLabel) {
                    "URGENT" -> if (fatigue >= FATIGUE_THRESHOLD) "High Fatigue Warning" else "High Stress Warning"
                    else -> "Elevated Levels"
                }
                val message = "Analysis: Fatigue ${fatigue.roundToInt()}%, Stress ${stress.roundToInt()}%"

                val alert = AlertEntity(
                    severity = severityLabel,
                    title = title,
                    message = message,
                    timestamp = now,
                    isRead = false
                )
                alertsRepo.insert(alert)
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Worker failed", e)
            Result.failure()
        }
    }

    private fun computeHeuristic(readings: List<com.hydrosync.mobile.data.SensorReading>): Quad {
        if (readings.isEmpty()) return Quad(75f, 25f, 15f, 50)
        val avgGsr = readings.map { it.gsr }.average().toFloat()
        val avgHr = readings.map { it.hr }.average().toFloat()
        val varGsr = readings.map { (it.gsr - avgGsr) * (it.gsr - avgGsr) }.average().toFloat()

        val hydration = (avgGsr / 10f).coerceIn(0f, 100f)
        val fatigue = (100f - avgHr).coerceIn(0f, 100f)
        val stress = (varGsr / 50f * 100f).coerceIn(0f, 100f)
        return Quad(hydration, fatigue, stress, 65)
    }

    private data class Quad(val h: Float, val f: Float, val s: Float, val c: Int)
}