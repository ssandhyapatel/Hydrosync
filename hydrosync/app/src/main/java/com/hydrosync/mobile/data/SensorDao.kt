package com.hydrosync.mobile.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SensorDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(reading: SensorReading)

    // last N readings (descending)
    @Query("SELECT * FROM sensor_reading ORDER BY timestamp DESC LIMIT :n")
    suspend fun lastN(n: Int): List<SensorReading>

    // Flow of last X readings (ordered ascending)
    @Query("SELECT * FROM sensor_reading ORDER BY timestamp ASC")
    fun allReadingsFlow(): Flow<List<SensorReading>>

    // readings between
    @Query("SELECT * FROM sensor_reading WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp ASC")
    suspend fun range(from: Long, to: Long): List<SensorReading>

    @Query("SELECT * FROM sensor_reading ORDER BY timestamp DESC")
    suspend fun allReadingsOnce(): List<SensorReading>
}
