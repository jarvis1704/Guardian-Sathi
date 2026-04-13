package com.biprangshu.guardiansathi.Elder.data

interface ElderForegroundServiceRepository {
    fun startForegroundService()
    fun stopForegroundService()
    fun isServiceRunning(): Boolean
}
