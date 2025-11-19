package com.hydrosync.mobile.ml

import com.hydrosync.mobile.data.SensorReading
import kotlin.math.sqrt

object FeatureBuilder {

    /**
     * Converts raw sensor readings into an array of 8 features for the ML model.
     */
    fun buildFeatures(readings: List<SensorReading>): FloatArray {
        if (readings.isEmpty()) return FloatArray(8) { 0f }

        // Extract raw lists
        val hrs = readings.map { it.hr.toDouble() }
        val gsrs = readings.map { it.gsr.toDouble() }
        val temps = readings.map { it.temp.toDouble() }

        // Calculate Stats
        val avgHr = hrs.average()
        val stdHr = stdDev(hrs, avgHr)
        val avgGsr = gsrs.average()
        val stdGsr = stdDev(gsrs, avgGsr)
        val avgTemp = temps.average()
        val stdTemp = stdDev(temps, avgTemp)

        // Derived Features
        val deltaHr = (readings.last().hr - readings.first().hr).toDouble()
        val countNorm = (readings.size.coerceAtMost(100) / 100.0)

        // Return 8-element vector
        return floatArrayOf(
            avgHr.toFloat(),
            stdHr.toFloat(),
            avgGsr.toFloat(),
            stdGsr.toFloat(),
            avgTemp.toFloat(),
            stdTemp.toFloat(),
            deltaHr.toFloat(),
            countNorm.toFloat()
        )
    }

    private fun stdDev(list: List<Double>, mean: Double): Double {
        if (list.isEmpty()) return 0.0
        val variance = list.map { (it - mean) * (it - mean) }.average()
        return sqrt(variance)
    }
}