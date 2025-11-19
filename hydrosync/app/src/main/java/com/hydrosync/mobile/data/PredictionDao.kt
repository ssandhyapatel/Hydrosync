package com.hydrosync.mobile.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PredictionDao {
    @Insert
    suspend fun insert(prediction: PredictionEntity): Long

    @Query("SELECT * FROM predictions ORDER BY timestamp DESC")
    fun allPredictionsFlow(): Flow<List<PredictionEntity>>

    @Query("SELECT * FROM predictions ORDER BY timestamp DESC LIMIT :limit")
    suspend fun latestPredictions(limit: Int): List<PredictionEntity>
}