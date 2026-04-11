package com.biprangshu.guardiansathi.Elder.core

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

data class BatteryInfo(
    val percentage: Int,
    val isCharging: Boolean,
    val chargingType: String,
    val health: String,
    val temperature: Float
)

fun getDetailedBatteryInfo(context: Context): BatteryInfo {
    val batteryStatus: Intent? = context.registerReceiver(
        null,
        IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    )

    val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
    val percentage = if (level != -1 && scale != -1) {
        (level * 100 / scale.toFloat()).toInt()
    } else {
        -1
    }

    val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

    val chargePlug = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
    val chargingType = when (chargePlug) {
        BatteryManager.BATTERY_PLUGGED_USB -> "USB"
        BatteryManager.BATTERY_PLUGGED_AC -> "AC Charger"
        BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
        else -> "Not Charging"
    }

    val health = batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
    val healthStatus = when (health) {
        BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheating"
        BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
        else -> "Unknown"
    }

    val temp = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
    val temperature = temp / 10.0f // Temperature in Celsius

    return BatteryInfo(
        percentage = percentage,
        isCharging = isCharging,
        chargingType = chargingType,
        health = healthStatus,
        temperature = temperature
    )
}