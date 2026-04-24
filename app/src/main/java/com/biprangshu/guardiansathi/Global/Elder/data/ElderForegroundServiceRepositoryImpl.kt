package com.biprangshu.guardiansathi.Global.Elder.data

import android.app.ActivityManager
import android.content.Context
import com.biprangshu.guardiansathi.Global.Elder.core.GuardianService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import androidx.core.content.edit

class ElderForegroundServiceRepositoryImpl @Inject constructor(
    @ApplicationContext private val applicationcontext: Context
) : ElderForegroundServiceRepository {

    override fun startForegroundService() {
        GuardianService.Companion.start(applicationcontext)
        applicationcontext.getSharedPreferences("guardian_saathi_prefs", Context.MODE_PRIVATE)
            .edit {
                putString("user_role", "ELDER")
            }
    }

    override fun stopForegroundService() {
        GuardianService.Companion.stop(applicationcontext)
        applicationcontext.getSharedPreferences("guardian_saathi_prefs", Context.MODE_PRIVATE)
            .edit { remove("user_role") }
    }

    override fun isServiceRunning(): Boolean {
        val manager = applicationcontext.getSystemService(ActivityManager::class.java)
        return manager.getRunningServices(Int.MAX_VALUE).any {
            it.service.className == GuardianService::class.java.name
        }
    }
}
