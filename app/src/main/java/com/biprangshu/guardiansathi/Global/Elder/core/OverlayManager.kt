package com.biprangshu.guardiansathi.Global.Elder.core

import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.GppBad
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.biprangshu.guardiansathi.R
import com.biprangshu.guardiansathi.Global.presentation.ui.theme.GuardianSathiTheme
import com.biprangshu.guardiansathi.Global.Elder.data.ElderFirebaseRepository
import kotlinx.coroutines.delay

// OverlayManager.kt
class OverlayManager(
    private val context: Context,
    private val firebaseRepository: ElderFirebaseRepository? = null
) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: ComposeView? = null
    private val lifecycleOwner = OverlayLifecycleOwner()

    fun showFallOverlay(
        onDismiss: () -> Unit = {},
        onImOkay: () -> Unit = {},
        onCallEmergency: () -> Unit = {}
    ) {
        if (overlayView != null) return
        if (!Settings.canDrawOverlays(context)) return

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        )

        val composeView = ComposeView(context).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                GuardianSathiTheme {
                    ScamDetectionOverlay(
                        title = "Fall Detected",
                        description = "We detected a possible fall. Are you okay?",
                        severity = OverlaySeverity.CRITICAL,
                        onCallGuardian = {
                            removeOverlay()
                            firebaseRepository?.sendNotificaitonToGuardian(
                                notificationData = NotificationData(
                                    packageName = "Guardian Saathi",
                                    appName = "Guardian Saathi",
                                    title = "🚨 Elder Needs Help!",
                                    desc = "Elder pressed \"Get Immediate Help\" after a fall was detected. Respond immediately.",
                                    body = "Elder triggered emergency help request from fall alert overlay.",
                                    timestamp = System.currentTimeMillis()
                                ),
                                isOtp = false,
                                isTransaction = false,
                                customImportance = "HIGH"
                            )
                            onCallEmergency()
                        },
                        onDismiss = {
                            removeOverlay()
                            onImOkay()
                            onDismiss()
                        }
                    )
                }
            }
        }

        // Attach lifecycle BEFORE adding to WindowManager
        lifecycleOwner.attachToDecorView(composeView)
        lifecycleOwner.onCreate()
        lifecycleOwner.onStart()
        lifecycleOwner.onResume()

        overlayView = composeView

        // Must run on main thread
        Handler(Looper.getMainLooper()).post {
            try {
                windowManager.addView(composeView, params)
            } catch (e: Exception) {
                Log.e("OverlayManager", "Failed to add overlay view: ${e.message}")
                overlayView = null
            }
        }
    }

    fun removeOverlay() {
        Handler(Looper.getMainLooper()).post {
            overlayView?.let {
                try {
                    lifecycleOwner.onPause()
                    lifecycleOwner.onStop()
                    lifecycleOwner.onDestroy()
                    windowManager.removeView(it)
                } catch (e: Exception) {
                    Log.e("OverlayManager", "Failed to remove overlay: ${e.message}")
                } finally {
                    overlayView = null
                }
            }
        }
    }

    fun showSuspiciousLinkOverlay(
        message: String,
        title: String = context.getString(R.string.suspicious_link_overlay_title),
        severity: OverlaySeverity = OverlaySeverity.WARNING,
        onCallGuardian: () -> Unit = {},
        onDismiss: () -> Unit = {}
    ) {
        if (overlayView != null) return
        if (!Settings.canDrawOverlays(context)) return

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        )

        val composeView = ComposeView(context).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                GuardianSathiTheme {
                    ScamDetectionOverlay(
                        title = title,
                        description = message,
                        severity = severity,
                        onCallGuardian = {
                            removeOverlay()
                            firebaseRepository?.sendNotificaitonToGuardian(
                                notificationData = NotificationData(
                                    packageName = "Guardian Saathi",
                                    appName = "Guardian Saathi",
                                    title = "🚨 Elder Called Guardian!",
                                    desc = "Elder pressed \"Call Guardian\" from a scam/suspicious activity alert. Check in immediately.",
                                    body = "Elder triggered guardian call from scam detection overlay.",
                                    timestamp = System.currentTimeMillis()
                                ),
                                isOtp = false,
                                isTransaction = false,
                                customImportance = "HIGH"
                            )
                            onCallGuardian()
                        },
                        onDismiss = {
                            removeOverlay()
                            onDismiss()
                        }
                    )
                }
            }
        }

        // Attach lifecycle BEFORE adding to WindowManager
        lifecycleOwner.attachToDecorView(composeView)
        lifecycleOwner.onCreate()
        lifecycleOwner.onStart()
        lifecycleOwner.onResume()

        overlayView = composeView

        // Must run on main thread
        Handler(Looper.getMainLooper()).post {
            try {
                windowManager.addView(composeView, params)
            } catch (e: Exception) {
                Log.e("OverlayManager", "Failed to add overlay view: ${e.message}")
                overlayView = null
            }
        }
    }
}

// OverlayLifecycleOwner.kt
class OverlayLifecycleOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore = ViewModelStore()

    fun onCreate() {
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    fun onStart() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    fun onResume() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun onPause() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    fun onStop() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        viewModelStore.clear()
    }

    fun attachToDecorView(decorView: View?) {
        decorView ?: return
        decorView.setViewTreeLifecycleOwner(this)
        decorView.setViewTreeViewModelStoreOwner(this)
        decorView.setViewTreeSavedStateRegistryOwner(this)
    }
}


//based on the severity, the colours wil be orange or red
enum class OverlaySeverity {
    WARNING,
    CRITICAL
}

@Composable
fun ScamDetectionOverlay(
    description: String,
    title: String = "Possible Scam Detected",
    severity: OverlaySeverity = OverlaySeverity.WARNING,
    onCallGuardian: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val primaryColor = if (severity == OverlaySeverity.CRITICAL) Color(0xFFD32F2F) else Color(0xFFFF8C00)
    val glowColor = if (severity == OverlaySeverity.CRITICAL) Color(0xFFFF5252) else Color(0xFFFFB300)
    val cardBgColor = if (severity == OverlaySeverity.CRITICAL) Color(0xFF1F0D0D) else Color(0xFF1F150D)
    val iconVector = if (severity == OverlaySeverity.CRITICAL) Icons.Rounded.GppBad else Icons.Rounded.Warning

    // Animation transition setup
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = EaseInOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale1"
    )
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = EaseInOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha1"
    )

    val scale2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, delayMillis = 1000, easing = EaseInOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale2"
    )
    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, delayMillis = 1000, easing = EaseInOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha2"
    )

    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconScale"
    )

    var progress by remember { mutableStateOf(1f) }
    LaunchedEffect(Unit) {
        val duration = 30000L
        val stepTime = 100L
        val totalSteps = duration / stepTime
        for (i in 0..totalSteps) {
            progress = 1f - (i.toFloat() / totalSteps)
            delay(stepTime)
        }
        onDismiss()
    }

    //todo: change the text to have the strings which are language translatable

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Overlay Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.82f))
        )

        // Radial Glow effect behind the card
        Box(
            modifier = Modifier
                .size(450.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(glowColor.copy(alpha = 0.15f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = cardBgColor.copy(alpha = 0.95f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 24.dp),
            border = BorderStroke(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.4f),
                        primaryColor.copy(alpha = 0.1f)
                    )
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Pulsing Icon
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(120.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .graphicsLayer {
                                scaleX = scale1
                                scaleY = scale1
                                alpha = alpha1
                            }
                            .background(primaryColor.copy(alpha = 0.25f), shape = CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .graphicsLayer {
                                scaleX = scale2
                                scaleY = scale2
                                alpha = alpha2
                            }
                            .background(primaryColor.copy(alpha = 0.25f), shape = CircleShape)
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(72.dp)
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            }
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = 0.2f),
                                        primaryColor.copy(alpha = 0.05f)
                                    )
                                ),
                                shape = CircleShape
                            )
                            .border(2.dp, primaryColor.copy(alpha = 0.8f), CircleShape)
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = "Alert Icon",
                            tint = primaryColor,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                // Severity badge
                val badgeText = if (severity == OverlaySeverity.CRITICAL) "CRITICAL ACTION REQUIRED" else "SUSPICIOUS ACTIVITY DETECTED"
                Box(
                    modifier = Modifier
                        .background(primaryColor.copy(alpha = 0.12f), shape = RoundedCornerShape(50.dp))
                        .border(1.dp, primaryColor.copy(alpha = 0.4f), RoundedCornerShape(50.dp))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = primaryColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center
                )

                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.1f),
                    thickness = 1.dp
                )

                Text(
                    text = description,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                // Countdown Timer bar
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = primaryColor,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                    Text(
                        text = "Auto-dismissing in ${kotlin.math.ceil(progress * 30).toInt()}s",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Actions Layout
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onCallGuardian,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = if (severity == OverlaySeverity.CRITICAL) "Get Immediate Help" else "Call Guardian",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "I understand, close",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}