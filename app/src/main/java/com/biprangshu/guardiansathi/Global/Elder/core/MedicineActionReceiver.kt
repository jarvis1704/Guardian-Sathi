package com.biprangshu.guardiansathi.Global.Elder.core

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.biprangshu.guardiansathi.Global.core.data.MedicineRepositoryImpl
import com.biprangshu.guardiansathi.Global.core.domain.MedicineReminder
import com.biprangshu.guardiansathi.Global.core.domain.MedicineStatus
import com.biprangshu.guardiansathi.R
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MedicineActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var medicineRepository: MedicineRepositoryImpl

    @Inject
    lateinit var auth: FirebaseAuth

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val reminderId = intent.getStringExtra("reminder_id") ?: return
        val reminderName = intent.getStringExtra("medicine_name") ?: ""
        val reminderDosage = intent.getStringExtra("medicine_dosage") ?: ""
        val notificationId = intent.getIntExtra("notification_id", 0)

        val elderUid = auth.uid ?: return

        val status = when (action) {
            "ACTION_MEDICINE_TAKEN" -> MedicineStatus.TAKEN
            "ACTION_MEDICINE_SKIP" -> MedicineStatus.SKIPPED
            else -> return
        }

        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch {
            medicineRepository.markAsTaken(
                elderUid = elderUid,
                reminder = MedicineReminder(id = reminderId, name = reminderName, dosage = reminderDosage),
                status = status
            )
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)
        }
    }
}
