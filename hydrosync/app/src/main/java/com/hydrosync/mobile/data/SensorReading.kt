package com.hydrosync.mobile.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sensor_reading")
data class SensorReading(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,   // epoch millis
    val hr: Float,         // heart rate (bpm)
    val gsr: Float,        // GSR (uS or scaled)
    val temp: Float       // skin temp (°C)
)