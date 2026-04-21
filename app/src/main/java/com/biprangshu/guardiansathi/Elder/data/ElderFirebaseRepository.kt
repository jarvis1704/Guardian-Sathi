package com.biprangshu.guardiansathi.Elder.data

import com.biprangshu.guardiansathi.Elder.core.NotificationData

interface ElderFirebaseRepository {
    fun sendDataToFirebaseDatabase(label: String, data: String)
    fun updateFirebaseTimestamp(label: String)
    fun sendNotificaitonToGuardian(notificationData: NotificationData, isOtp: Boolean, isTransaction: Boolean = false)
    fun pushActivityLog(type: String)
}