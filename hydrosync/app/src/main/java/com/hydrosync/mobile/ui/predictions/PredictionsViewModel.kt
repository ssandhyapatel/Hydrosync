package com.hydrosync.mobile.ui.predictions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hydrosync.mobile.data.PredictionEntity
import com.hydrosync.mobile.repo.PredictionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PredictionsViewModel @Inject constructor(
    repository: PredictionRepository
) : ViewModel() {

    val predictions: StateFlow<List<PredictionEntity>> = repository.allFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}