package com.hydrosync.mobile.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SensorReading::class, AlertEntity::class, PredictionEntity::class],
    version = 2, // Incremented version
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sensorDao(): SensorDao
    abstract fun alertDao(): AlertDao
    abstract fun predictionDao(): PredictionDao
}