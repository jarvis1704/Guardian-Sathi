package com.biprangshu.guardiansathi.Global

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.biprangshu.guardiansathi.Global.core.LanguageUtils
import com.biprangshu.guardiansathi.Global.core.NotificationHelper
import com.biprangshu.guardiansathi.Global.core.isGestureNav
import com.biprangshu.guardiansathi.Global.core.isGestureNavigationEnabled
import com.biprangshu.guardiansathi.Global.presentation.navigation.AppNav
import com.biprangshu.guardiansathi.Global.presentation.ui.components.errorAlert
import com.biprangshu.guardiansathi.Global.presentation.ui.theme.GuardianSathiTheme
import com.psydrite.bugsnap.BugSnap
import com.psydrite.bugsnap.BugSnapOverlay

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted")
        } else {
            Log.d("MainActivity", "Notification permission denied")
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val savedLanguage = LanguageUtils.getSavedLanguage(newBase)
        val context = LanguageUtils.setLocale(newBase, savedLanguage)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createChannels(this)
        requestNotificationPermission()
        BugSnap.init(
            activity = this,
            projectKey = "guardian-sathi",      // Firebase Project ID
            FBstorageUrl = "guardian-sathi.firebasestorage.app",  // Storage bucket
            ApiKey = "",
            collectionName = "BugSnap"
        )
        setContent {
            val NavController = rememberNavController()
            GuardianSathiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    isGestureNav = isGestureNavigationEnabled(this)
                    BugSnapOverlay()
                    errorAlert()
                    AppNav(NavController)
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        BugSnap.stop()  // Clean up resources
    }
}