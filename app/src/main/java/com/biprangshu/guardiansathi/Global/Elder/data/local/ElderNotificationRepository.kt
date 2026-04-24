package com.biprangshu.guardiansathi.Global.Elder.data.local

import android.util.Log
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


interface ElderNotificationRepository {
    suspend fun insertNotification(notification: ElderNotification)
    suspend fun deleteNotification(notification: ElderNotification)
    fun getAllNotifications(): Flow<List<ElderNotification>>
    suspend fun deleteAllNotifications(notifications: List<ElderNotification>)
}

class ElderNotificationRepositoryImpl @Inject constructor(
    private val dao: ElderNotificationsDao
) : ElderNotificationRepository {
    override suspend fun insertNotification(notification: ElderNotification) {
        dao.insertNotification(notification)
        Log.d("ElderNotifications","stored notification: $notification")
    }

    override suspend fun deleteNotification(notification: ElderNotification) {
        dao.deleteNotification(notification)
        Log.d("ElderNotifications","deleted notification: $notification")
    }

    override fun getAllNotifications(): Flow<List<ElderNotification>> = dao.getAllNotifications()

    override suspend fun deleteAllNotifications(notifications: List<ElderNotification>) {
        dao.deleteAllNotifications(notifications)
        Log.d("ElderNotifications","all notifications deleted")
    }
}