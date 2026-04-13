package com.biprangshu.guardiansathi.Elder.ui

import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Bundle
import android.os.CountDownTimer
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.biprangshu.guardiansathi.Elder.presentation.screens.FallAlarmScreen

class FallAlarmActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null
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

    private fun playAlarm() {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, alarmUri)
                setAudioStreamType(AudioManager.STREAM_ALARM)
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            // fallback: use ringtone if alarm stream fails
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, ringtoneUri)
                isLooping = true
                prepare()
                start()
            }
        }
    }

    private fun dismissAlarm() {
        countDownTimer?.cancel()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        finish()
    }

    private fun sendAlertAndDismiss() {
        // TODO: trigger FCM/SMS alert to guardian here
        // GuardianAlertManager.sendFallAlert(this)
        dismissAlarm()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        countDownTimer?.cancel()
    }
}