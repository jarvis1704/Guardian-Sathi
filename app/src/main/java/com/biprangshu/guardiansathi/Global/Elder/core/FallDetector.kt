package com.biprangshu.guardiansathi.Global.Elder.core

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.sqrt

class FallDetector(
    private val context: Context,
    private val onFallDetected: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // Thresholds — tunable
    private val FREE_FALL_THRESHOLD = 3.0f   // m/s² — below this = free fall (~0.3g)
    private val IMPACT_THRESHOLD = 25.0f     // m/s² — above this = hard impact (~2.5g)
    private val STILL_THRESHOLD = 12.0f      // m/s² — near 1g after impact = lying still

    // State machine
    private var state = FallState.NORMAL
    private var freeFallTimestamp = 0L
    private val FALL_WINDOW_MS = 2000L       // impact must happen within 2s of free fall

    private enum class FallState { NORMAL, FREE_FALL_DETECTED, IMPACT_DETECTED }

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
            Log.d("FallDetector", "✅ Accelerometer registered")
        } ?: Log.e("FallDetector", "❌ No accelerometer found on device")
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        Log.d("FallDetector", "🛑 Accelerometer unregistered")
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Total acceleration magnitude
        val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

        when (state) {
            FallState.NORMAL -> {
                if (magnitude < FREE_FALL_THRESHOLD) {
                    // Phase 1: Free fall detected
                    state = FallState.FREE_FALL_DETECTED
                    freeFallTimestamp = System.currentTimeMillis()
                    Log.d("FallDetector", "⬇️ Free fall phase — magnitude: $magnitude")
                }
            }

            FallState.FREE_FALL_DETECTED -> {
                val elapsed = System.currentTimeMillis() - freeFallTimestamp

                if (elapsed > FALL_WINDOW_MS) {
                    // Took too long — not a fall, reset
                    Log.d("FallDetector", "⏱️ Window expired, resetting")
                    state = FallState.NORMAL
                    return
                }

                if (magnitude > IMPACT_THRESHOLD) {
                    // Phase 2: Impact spike detected
                    state = FallState.IMPACT_DETECTED
                    Log.d("FallDetector", "💥 Impact detected — magnitude: $magnitude")
                }
            }

            FallState.IMPACT_DETECTED -> {
                if (magnitude in FREE_FALL_THRESHOLD..STILL_THRESHOLD) {
                    // Phase 3: Stillness after impact = confirmed fall
                    Log.d("FallDetector", "🚨 FALL CONFIRMED — person is still")
                    state = FallState.NORMAL  // reset for next detection
                    onFallDetected()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}