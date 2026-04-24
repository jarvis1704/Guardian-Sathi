package com.biprangshu.guardiansathi.Global.Elder.data

import com.biprangshu.guardiansathi.Global.Elder.core.NotificationData

interface ElderFirebaseRepository {
    fun sendDataToFirebaseDatabase(label: String, data: String)
    fun updateFirebaseTimestamp(label: String)
    fun sendNotificaitonToGuardian(notificationData: NotificationData, isOtp: Boolean, isTransaction: Boolean = false, customImportance: String = "LOW")
    fun pushActivityLog(type: String)
}