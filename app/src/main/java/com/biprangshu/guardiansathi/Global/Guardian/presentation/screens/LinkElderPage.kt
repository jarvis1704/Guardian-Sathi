package com.biprangshu.guardiansathi.Global.Guardian.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biprangshu.guardiansathi.Global.Guardian.presentation.viewmodel.LinkElderAction
import com.biprangshu.guardiansathi.Global.Guardian.presentation.viewmodel.LinkElderEvent
import com.biprangshu.guardiansathi.Global.Guardian.presentation.viewmodel.LinkElderState
import com.biprangshu.guardiansathi.Global.Guardian.presentation.viewmodel.LinkElderViewModel
import com.biprangshu.guardiansathi.Global.presentation.ui.components.ConnectionSuccessDialog
import com.biprangshu.guardiansathi.R
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@Composable
fun LinkElderRoot(
    onNavigateToGuardianHome: () -> Unit,
    viewModel: LinkElderViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var successDialog by remember { mutableStateOf<LinkElderEvent.ShowConnectionSuccess?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LinkElderEvent.NavigateToGuardianHome -> onNavigateToGuardianHome()
                is LinkElderEvent.ShowConnectionSuccess -> successDialog = event
                is LinkElderEvent.ShowError -> { /* error shown via state */ }
            }
        }
    }

    LinkElderPage(
        state = state,
        onCodeInputChange = { viewModel.onAction(LinkElderAction.OnCodeInputChange(it)) },
        onSubmitCode = { viewModel.onAction(LinkElderAction.OnSubmitCode) },
        onQrScanned = { viewModel.onAction(LinkElderAction.OnQrScanned(it)) },
        onToggleScanner = { viewModel.onAction(LinkElderAction.OnToggleScanner) }
    )

    successDialog?.let { data ->
        ConnectionSuccessDialog(
            name = data.connectedName,
            photourl_1 = data.myPhotoUrl,
            photourl_2 = data.connectedPhotoUrl,
            onContinue = {
                successDialog = null
                onNavigateToGuardianHome()
            },
            onRetry = { successDialog = null },
            onDismiss = { successDialog = null }
        )
    }
}

@Composable
fun LinkElderPage(
    state: LinkElderState,
    onCodeInputChange: (String) -> Unit,
    onSubmitCode: () -> Unit,
    onQrScanned: (String) -> Unit,
    onToggleScanner: () -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val cameraGranted = remember(context) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
    }
    var cameraPermGranted by remember { mutableStateOf(cameraGranted) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        cameraPermGranted = granted
        if (granted) onToggleScanner()
    }

    if (state.showScanner && cameraPermGranted) {
        QrScannerView(
            onQrDetected = onQrScanned,
            onDismiss = onToggleScanner
        )
    } else {
        LinkElderMainContent(
            state = state,
            onCodeInputChange = onCodeInputChange,
            onSubmitCode = {
                keyboardController?.hide()
                onSubmitCode()
            },
            onScanClick = {
                if (cameraPermGranted) {
                    onToggleScanner()
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        )
    }
}

@Composable
private fun LinkElderMainContent(
    state: LinkElderState,
    onCodeInputChange: (String) -> Unit,
    onSubmitCode: () -> Unit,
    onScanClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .padding(top = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = stringResource(R.string.LinkElder_T),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.LinkElder_Share_S),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedButton(
            onClick = onScanClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(text = stringResource(R.string.LinkElder_S), fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.LinkElder_S2),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = state.codeInput,
            onValueChange = onCodeInputChange,
            label = { Text("Connection Code") },
            placeholder = { Text("SAATHI-XXXXXX") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = state.error != null,
            supportingText = {
                if (state.error != null) {
                    Text(state.error, color = MaterialTheme.colorScheme.error)
                }
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onSubmitCode() }),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSubmitCode,
            enabled = state.codeInput.isNotBlank() && !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(text = stringResource(R.string.LinkElder_button), fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
private fun QrScannerView(
    onQrDetected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var hasScanned by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                    val barcodeScanner = BarcodeScanning.getClient()
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                val mediaImage = imageProxy.image
                                if (mediaImage != null && !hasScanned) {
                                    val image = InputImage.fromMediaImage(
                                        mediaImage,
                                        imageProxy.imageInfo.rotationDegrees
                                    )
                                    barcodeScanner.process(image)
                                        .addOnSuccessListener { barcodes ->
                                            val qr = barcodes.firstOrNull {
                                                it.format == Barcode.FORMAT_QR_CODE
                                            }
                                            qr?.rawValue?.let { value ->
                                                if (!hasScanned) {
                                                    hasScanned = true
                                                    onQrDetected(value)
                                                }
                                            }
                                        }
                                        .addOnCompleteListener { imageProxy.close() }
                                } else {
                                    imageProxy.close()
                                }
                            }
                        }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel", color = Color.White)
            }
        }
    }
}