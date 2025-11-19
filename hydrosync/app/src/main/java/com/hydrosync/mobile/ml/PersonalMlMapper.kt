package com.hydrosync.mobile.ml

import com.hydrosync.mobile.data.SensorReading
import kotlin.math.sqrt

/**
 * Converts a list of raw sensor readings into a "Feature Vector"
 * that the ML model understands.
 */
object PersonalMlMapper {

    fun toFeatureVector(readings: List<SensorReading>): FloatArray {
        if (readings.isEmpty()) {
            return FloatArray(8) { 0f }
        }

        val hrs = readings.map { it.hr.toDouble() }
        val gsrs = readings.map { it.gsr.toDouble() }
        val temps = readings.map { it.temp.toDouble() }

        fun mean(xs: List<Double>) = if (xs.isEmpty()) 0.0 else xs.average()

        fun std(xs: List<Double>, avg: Double): Double {
            if (xs.isEmpty()) return 0.0
            val variance = xs.map { (it - avg) * (it - avg) }.average()
            return sqrt(variance)
        }

        val avgHr = mean(hrs)
        val stdHr = std(hrs, avgHr)
        val avgGsr = mean(gsrs)
        val stdGsr = std(gsrs, avgGsr)
        val avgTemp = mean(temps)
        val stdTemp = std(temps, avgTemp)

        // Simple delta (Latest - Earliest)
        val deltaHr = (readings.last().hr - readings.first().hr).toDouble()

        // Count of readings (normalized, max 100)
        val sampleCountNorm = (readings.size.coerceAtMost(100) / 100.0)

        // Return 8 features
        return floatArrayOf(
            avgHr.toFloat(),
            stdHr.toFloat(),
            avgGsr.toFloat(),
            stdGsr.toFloat(),
            avgTemp.toFloat(),
            stdTemp.toFloat(),
            deltaHr.toFloat(),
            sampleCountNorm.toFloat()
        )
    }
}