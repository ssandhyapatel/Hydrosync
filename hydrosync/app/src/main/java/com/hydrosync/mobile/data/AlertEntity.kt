package com.hydrosync.mobile.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val severity: String,      // "URGENT", "MILD", "INFO"
    val title: String,
    val message: String,
    val timestamp: Long,
    val read: Boolean = false
)
