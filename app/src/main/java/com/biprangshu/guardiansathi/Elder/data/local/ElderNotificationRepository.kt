package com.biprangshu.guardiansathi.Elder.data.local

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


interface ElderNotificationRepository {
    suspend fun insertNotification(notification: ElderNotification)
    suspend fun deleteNotification(notification: ElderNotification)
    fun getAllNotifications(): Flow<List<ElderNotification>>
}

class ElderNotificationRepositoryImpl @Inject constructor(
    private val dao: ElderNotificationsDao
) : ElderNotificationRepository {
    override suspend fun insertNotification(notification: ElderNotification) {
        dao.insertNotification(notification)
    }

    override suspend fun deleteNotification(notification: ElderNotification) {
        dao.deleteNotification(notification)
    }

    override fun getAllNotifications(): Flow<List<ElderNotification>> = dao.getAllNotifications()

}