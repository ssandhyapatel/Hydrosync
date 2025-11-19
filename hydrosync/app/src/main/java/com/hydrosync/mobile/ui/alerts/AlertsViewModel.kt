package com.hydrosync.mobile.ui.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hydrosync.mobile.data.AlertEntity
import com.hydrosync.mobile.repo.AlertsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val repository: AlertsRepository
) : ViewModel() {

    // Transform Entity -> UI Model
    val alerts: StateFlow<List<Alert>> = repository.allAlerts
        .map { entities -> entities.map { it.toUi() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun markRead(id: Long) {
        viewModelScope.launch {
            repository.markRead(id)
        }
    }

    fun deleteAlert(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    // Helper to convert DB Entity to UI Model
    private fun AlertEntity.toUi(): Alert {
        val sev = when(this.severity) {
            "URGENT" -> Severity.URGENT
            "MILD" -> Severity.MILD
            else -> Severity.INFO
        }
        return Alert(
            id = this.id,
            title = this.title,
            message = this.message,
            timestamp = this.timestamp,
            severity = sev,
            read = this.isRead
        )
    }

    // Helper methods for filtering (used in Activity)
    fun filterUrgent(list: List<Alert>): List<Alert> {
        return list.filter { it.severity == Severity.URGENT }
    }
}

