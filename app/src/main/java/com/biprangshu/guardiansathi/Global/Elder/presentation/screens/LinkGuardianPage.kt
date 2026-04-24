package com.biprangshu.guardiansathi.Global.Elder.presentation.screens

import android.graphics.Bitmap
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biprangshu.guardiansathi.Global.Elder.core.generateQrCodeAsBitmap
import com.biprangshu.guardiansathi.Global.Elder.core.shareQrCodeToWhatsApp
import com.biprangshu.guardiansathi.Global.Elder.presentation.viewmodel.LinkGuardianAction
import com.biprangshu.guardiansathi.Global.Elder.presentation.viewmodel.LinkGuardianEvent
import com.biprangshu.guardiansathi.Global.Elder.presentation.viewmodel.LinkGuardianState
import com.biprangshu.guardiansathi.Global.Elder.presentation.viewmodel.LinkGuardianViewModel
import com.biprangshu.guardiansathi.Global.core.isGestureNav
import com.biprangshu.guardiansathi.Global.presentation.ui.components.ConnectionSuccessDialog
import com.biprangshu.guardiansathi.R

@Composable
fun LinkGuardianRoot(
    onNavigateToElderHome: () -> Unit,
    viewModel: LinkGuardianViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val shareMessagePrefix = stringResource(R.string.LinkGuardian_Share_Message)
    var successDialog by remember { mutableStateOf<LinkGuardianEvent.ShowConnectionSuccess?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LinkGuardianEvent.NavigateToElderHome -> onNavigateToElderHome()
                is LinkGuardianEvent.ShowConnectionSuccess -> successDialog = event
            }
        }
    }

    LinkGuardianPage(
        state = state,
        onShareClick = {
            if (state.qrContent != null && state.linkCode != null) {
                val bitmap = generateQrCodeAsBitmap(state.qrContent!!)
                val message = "$shareMessagePrefix ${state.linkCode}"
                shareQrCodeToWhatsApp(context, bitmap, message)
            }
        },
        onRetryClick = { viewModel.onAction(LinkGuardianAction.OnRetryGenerateCode) }
    )

    successDialog?.let { data ->
        ConnectionSuccessDialog(
            name = data.connectedName,
            photourl_1 = data.myPhotoUrl,
            photourl_2 = data.connectedPhotoUrl,
            onContinue = {
                successDialog = null
                onNavigateToElderHome()
            },
            onRetry = { successDialog = null },
            onDismiss = { successDialog = null }
        )
    }
}

@Composable
fun LinkGuardianPage(
    state: LinkGuardianState,
    onShareClick: () -> Unit,
    onRetryClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waiting_pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )

    val qrBitmap: Bitmap? = remember(state.qrContent) {
        state.qrContent?.let { generateQrCodeAsBitmap(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .padding(top = 50.dp)
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.LinkGuardian_T),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = stringResource(R.string.LinkGuardian_S),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // QR Card
        Box(
            modifier = Modifier
                .size(260.dp)
                .border(
                    width = 4.dp,
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.isLoading -> CircularProgressIndicator()
                state.error != null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.error,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                        TextButton(onClick = onRetryClick) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Retry")
                        }
                    }
                }
                qrBitmap != null -> {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.size(200.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.LinkGuardian_S2),
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Code box
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = state.linkCode ?: "Generating...",
                modifier = Modifier
                    .padding(20.dp)
                    .padding(horizontal = 6.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Waiting status
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .alpha(if (state.isWaiting) dotAlpha else 1f)
                    .background(
                        color = if (state.isWaiting) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.tertiary,
                        shape = CircleShape
                    )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = if (state.isWaiting) stringResource(R.string.LinkGuardian_S3) else "Connected!",
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Share button
        Button(
            onClick = onShareClick,
            enabled = !state.isLoading && state.qrContent != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.LinkGuardian_Share), fontSize = 16.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.LinkGuardian_Share_S),
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = if (isGestureNav) 20.dp else 70.dp)
        )
    }
}
