package com.biprangshu.guardiansathi.Global

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.biprangshu.guardiansathi.Global.presentation.navigation.AuthNavGraph
import com.biprangshu.guardiansathi.Global.ui.theme.GuardianSathiTheme
import com.psydrite.bugsnap.BugSnap
import com.psydrite.bugsnap.BugSnapOverlay

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val savedLanguage = LanguageUtils.getSavedLanguage(newBase)
        val context = LanguageUtils.setLocale(newBase, savedLanguage)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        BugSnap.init(
            activity = this,
            projectKey = "guardian-sathi",      // Firebase Project ID
            FBstorageUrl = "gs://guardian-sathi.firebasestorage.app",  // Storage bucket
            ApiKey = "",
            collectionName = "BugSnap"
        )
        setContent {
            val NavController = rememberNavController()
            GuardianSathiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BugSnapOverlay()
                    AuthNavGraph()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        BugSnap.stop()  // Clean up resources
    }
}