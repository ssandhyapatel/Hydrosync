package com.hydrosync.mobile.repo

import com.hydrosync.mobile.data.AlertDao
import com.hydrosync.mobile.data.AlertEntity
import com.hydrosync.mobile.notifications.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertsRepository @Inject constructor(
    private val dao: AlertDao,
    private val notifier: NotificationHelper
) {

    // Expose the Flow required by AlertsViewModel
    val allAlerts: Flow<List<AlertEntity>> = dao.getAllFlow()

    suspend fun insert(entity: AlertEntity) = withContext(Dispatchers.IO) {
        // We insert and capture the new row ID
        dao.insert(entity)

        // Existing Logic: Trigger notification if Urgent
        // Note: We use the entity properties. If 'id' is auto-generated,
        // ensure your AlertEntity object has the ID or use the return value of insert.
        if (entity.severity.equals("URGENT", ignoreCase = true)) {
            notifier.postUrgentAlertNotification(entity.id, entity.title, entity.message)
        }
    }

    suspend fun markRead(id: Long) = withContext(Dispatchers.IO) {
        dao.markRead(id)
    }

    // NEW: Required for Swipe-to-Delete feature
    suspend fun deleteById(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteById(id)
    }
}