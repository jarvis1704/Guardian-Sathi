package com.biprangshu.guardiansathi.Elder.ui

import android.app.NotificationManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.biprangshu.guardiansathi.Elder.core.GuardianService
import com.biprangshu.guardiansathi.Elder.presentation.screens.FallAlarmScreen
import kotlin.jvm.java

class FallAlarmActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var isMediaPlayerReady = false
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // THE KEY: show over lock screen and wake the display
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        playAlarm()

        setContent {
            FallAlarmScreen(
                onImOkay = { dismissAlarm() },
                onTimerFinished = { sendAlertAndDismiss() }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        // If notification was already cancelled (user tapped action button), close activity
        val nm = getSystemService(NotificationManager::class.java)
        val isNotificationActive = nm?.activeNotifications
            ?.any { it.id == GuardianService.FALL_NOTIFICATION_ID } ?: false
        if (!isNotificationActive) {
            dismissAlarm()
        }
    }

    private fun playAlarm() {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, alarmUri)
                setAudioStreamType(AudioManager.STREAM_ALARM)
                isLooping = true
                setOnPreparedListener {
                    isMediaPlayerReady = true  // ← only mark ready after prepare
                    start()
                }
                prepareAsync()  // ← use prepareAsync instead of prepare()
            }
        } catch (e: Exception) {
            Log.e("GuardianService", "MediaPlayer error: ${e.message}")
        }
    }

    private fun dismissAlarm() {
        countDownTimer?.cancel()
        countDownTimer = null
        // ← only stop if actually ready
        if (isMediaPlayerReady) {
            try {
                mediaPlayer?.stop()
            } catch (e: Exception) {
                Log.e("GuardianService", "Stop error: ${e.message}")
            }
        }
        mediaPlayer?.release()
        mediaPlayer = null
        isMediaPlayerReady = false
        GuardianService.cancelFallNotification(this)
        finish()
    }

    private fun sendAlertAndDismiss() {
        // TODO: trigger FCM/SMS alert to guardian here
        GuardianService.cancelFallNotification(this)
        dismissAlarm()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        countDownTimer?.cancel()
    }
}

