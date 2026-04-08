package com.biprangshu.guardiansathi.Global

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.biprangshu.guardiansathi.Global.ui.theme.GuardianSathiTheme
import com.psydrite.bugsnap.BugSnap
import com.psydrite.bugsnap.BugSnapOverlay

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
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
            GuardianSathiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BugSnapOverlay()
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        BugSnap.stop()  // Clean up resources
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GuardianSathiTheme {
        Greeting("Android")
    }
}