package com.hydrosync.mobile.repo

import com.hydrosync.mobile.data.PredictionDao
import com.hydrosync.mobile.data.PredictionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PredictionRepository @Inject constructor(
    private val dao: PredictionDao
) {
    suspend fun insert(pred: PredictionEntity) = withContext(Dispatchers.IO) {
        dao.insert(pred)
    }

    fun allFlow(): Flow<List<PredictionEntity>> = dao.allPredictionsFlow()

    suspend fun latest(limit: Int = 10): List<PredictionEntity> = withContext(Dispatchers.IO) {
        dao.latestPredictions(limit)
    }
}