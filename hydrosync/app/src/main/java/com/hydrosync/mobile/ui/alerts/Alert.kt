package com.hydrosync.mobile.ui.alerts

data class Alert(
    val id: Long,
    val severity: Severity,
    val title: String,
    val message: String,
    val timestamp: Long,
    var read: Boolean = false
)

enum class Severity {
    URGENT, MILD, INFO
}
