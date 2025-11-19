package com.hydrosync.mobile.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "predictions")
data class PredictionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val timestamp: Long,
    val hydrationPct: Float,
    val fatiguePct: Float,
    val stressPct: Float,
    val confidencePct: Int,
    val source: String // "model" or "heuristic"
)