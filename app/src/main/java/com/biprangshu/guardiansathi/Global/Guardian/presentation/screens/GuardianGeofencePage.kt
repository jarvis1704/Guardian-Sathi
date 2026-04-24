package com.biprangshu.guardiansathi.Global.Guardian.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biprangshu.guardiansathi.Global.Guardian.presentation.viewmodel.GuardianGeofenceAction
import com.biprangshu.guardiansathi.Global.Guardian.presentation.viewmodel.GuardianGeofenceEvent
import com.biprangshu.guardiansathi.Global.Guardian.presentation.viewmodel.GuardianGeofenceState
import com.biprangshu.guardiansathi.Global.Guardian.presentation.viewmodel.GuardianGeofenceViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun GuardianGeofenceRoot(
    viewModel: GuardianGeofenceViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is GuardianGeofenceEvent.ShowSnackbar -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    GuardianGeofenceScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun GuardianGeofenceScreen(
    state: GuardianGeofenceState,
    onAction: (GuardianGeofenceAction) -> Unit
) {
    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.isLoading && state.centerLat == null && state.elderLocationLat == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                return@Box
            }

            Column(modifier = Modifier.fillMaxSize()) {
                val initialLat = state.centerLat ?: state.elderLocationLat ?: 0.0
                val initialLng = state.centerLong ?: state.elderLocationLong ?: 0.0
                val initialPos = LatLng(initialLat, initialLng)

                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(initialPos, 15f)
                }

                // Map occupies upper part
                Box(modifier = Modifier.weight(1f)) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        onMapClick = { latLng ->
                            onAction(GuardianGeofenceAction.OnMapClick(latLng))
                        }
                    ) {
                        // Elder Location Marker
                        if (state.elderLocationLat != null && state.elderLocationLong != null) {
                            Marker(
                                state = MarkerState(position = LatLng(state.elderLocationLat, state.elderLocationLong)),
                                title = "Elder Location"
                            )
                        }

                        // Geofence Circle
                        if (state.centerLat != null && state.centerLong != null) {
                            Circle(
                                center = LatLng(state.centerLat, state.centerLong),
                                radius = state.radiusMeters,
                                fillColor = Color(0x220000FF),
                                strokeColor = Color.Blue,
                                strokeWidth = 2f
                            )
                            Marker(
                                state = MarkerState(position = LatLng(state.centerLat, state.centerLong)),
                                title = "Geofence Center"
                            )
                        }
                    }
                }

                // Controls at bottom
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Geofence Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(text = "Tap on the map to set the safe zone center.")

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Enable Geofence", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = state.isActive,
                            onCheckedChange = { onAction(GuardianGeofenceAction.OnToggleActive(it)) }
                        )
                    }

                    Column {
                        Text(text = "Radius: ${state.radiusMeters.toInt()} meters")
                        Slider(
                            value = state.radiusMeters.toFloat(),
                            onValueChange = { onAction(GuardianGeofenceAction.OnRadiusChange(it)) },
                            valueRange = 100f..5000f,
                            steps = 49
                        )
                    }

                    Button(
                        onClick = { onAction(GuardianGeofenceAction.OnSaveClick) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Save Configuration")
                        }
                    }
                }
            }
        }
    }
}