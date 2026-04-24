package com.biprangshu.guardiansathi.Global.Elder.data

interface ElderForegroundServiceRepository {
    fun startForegroundService()
    fun stopForegroundService()
    fun isServiceRunning(): Boolean
}
