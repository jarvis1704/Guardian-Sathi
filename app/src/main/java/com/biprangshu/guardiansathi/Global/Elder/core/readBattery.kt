package com.biprangshu.guardiansathi.Global.Elder.core

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.google.firebase.Timestamp

data class BatteryData(
    val level: Int,
    val isCharging: Boolean,
    val timestamp: String
)

fun readBattery(context: Context): BatteryData {
    // ACTION_BATTERY_CHANGED is a sticky broadcast — registerReceiver with
    // null receiver returns the last sticky intent immediately (no wait).
    val intent = context.registerReceiver(
        null,
        IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    )

    val level  = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale  = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
    val pct    = if (scale > 0) (level * 100 / scale) else 0

    val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL


    return BatteryData(
        level       = pct,
        isCharging  = isCharging,
        timestamp   = Timestamp.now().seconds.toString()
    )
}