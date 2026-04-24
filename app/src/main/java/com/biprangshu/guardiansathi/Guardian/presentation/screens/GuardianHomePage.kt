package com.biprangshu.guardiansathi.Guardian.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryFull
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.biprangshu.guardiansathi.Guardian.presentation.viewmodel.GuardianHomeAction
import com.biprangshu.guardiansathi.Guardian.presentation.viewmodel.GuardianHomeState
import com.biprangshu.guardiansathi.Guardian.presentation.viewmodel.GuardianHomeViewModel
import com.biprangshu.guardiansathi.Guardian.presentation.viewmodel.toLastActiveText
import com.biprangshu.guardiansathi.R
import com.biprangshu.guardiansathi.Global.presentation.ui.theme.GuardianSathiTheme

private val SafeGreen = Color(0xFF4CAF50)

@Composable
fun GuardianHomeRoot(
    viewModel: GuardianHomeViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.getFCMTokenAndSave()
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    GuardianHomeScreen(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GuardianHomeScreen(
    state: GuardianHomeState,
    onAction: (GuardianHomeAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.guardian_home_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            if (state.guardianPhotoUrl != null) {
                AsyncImage(
                    model = state.guardianPhotoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.ic_profile_placeholder),
                    placeholder = painterResource(R.drawable.ic_profile_placeholder)
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Elder profile card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 28.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Elder photo with online dot
                Box(contentAlignment = Alignment.BottomEnd) {
                    AsyncImage(
                        model = state.elderPhotoUrl,
                        contentDescription = stringResource(R.string.guardian_home_monitoring_label),
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.Crop,
                        error = painterResource(R.drawable.ic_profile_placeholder),
                        placeholder = painterResource(R.drawable.ic_profile_placeholder)
                    )
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(SafeGreen)
                            .border(2.dp, MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.guardian_home_monitoring_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = state.elderName.ifBlank { "—" },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(10.dp))

                // Safe at Home pill
                Surface(
                    shape = RoundedCornerShape(50),
                    color = SafeGreen.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = SafeGreen
                        )
                        Text(
                            text = stringResource(R.string.guardian_home_status_safe),
                            style = MaterialTheme.typography.labelMedium,
                            color = SafeGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        //Stats grid
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            maxItemsInEachRow = 2,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                icon = Icons.Outlined.BatteryFull,
                value = "${state.batteryLevel}%",
                label = stringResource(R.string.guardian_home_battery_label),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Outlined.LocationOn,
                value = stringResource(R.string.guardian_home_location_safe_zone),
                label = stringResource(R.string.guardian_home_location_label),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Outlined.History,
                value = state.lastActiveTimestamp.toLastActiveText(),
                label = stringResource(R.string.guardian_home_last_active_label),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        //Upcoming reminder card(to be replaced)
        // TODO: Replace with real reminder data when reminders feature is built
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MedicalServices,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                        Text(
                            text = stringResource(R.string.guardian_home_upcoming_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Atenolol at\n8:00 PM",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Heart health & BP management",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                }
                Spacer(Modifier.width(12.dp))
                OutlinedButton(
                    onClick = { onAction(GuardianHomeAction.OnConfirmReminder) },
                    shape = RoundedCornerShape(50),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        MaterialTheme.colorScheme.onPrimary
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = stringResource(R.string.guardian_home_confirm),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Recent Activity ──────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.guardian_home_recent_activity),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = { onAction(GuardianHomeAction.OnSeeAllHistory) }) {
                Text(
                    text = stringResource(R.string.guardian_home_see_all),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        if (state.activityLogs.isEmpty()) {
            Text(
                text = "No recent activity",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            state.activityLogs.take(5).forEach { log ->
                val icon = if (log.type == "GEOFENCE_ENTER") Icons.Outlined.LocationOn else Icons.Outlined.LocationOn
                val tint = if (log.type == "GEOFENCE_ENTER") SafeGreen else MaterialTheme.colorScheme.error
                val title = if (log.type == "GEOFENCE_ENTER") "Safe Zone Entered" else "Safe Zone Exited"
                val subtitle = if (log.type == "GEOFENCE_ENTER") "Elder has entered the safe zone" else "Elder has left the safe zone"

                ActivityItem(
                    icon = icon,
                    iconTint = tint,
                    title = title,
                    subtitle = subtitle,
                    time = log.formattedTime
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActivityItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    time: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = iconTint.copy(alpha = 0.12f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = iconTint
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//private fun GuardianHomeScreenLightPreview() {
//    GuardianSathiTheme(darkTheme = false) {
//        GuardianHomeScreen(
//            state = GuardianHomeState(
//                elderName = "Ramesh (Father)",
//                batteryLevel = 85,
//                isCharging = false,
//                lastBatterySeen = System.currentTimeMillis() - 2 * 60 * 1000L
//            ),
//            onAction = {}
//        )
//    }
//}

//@Preview(showBackground = true)
//@Composable
//private fun GuardianHomeScreenDarkPreview() {
//    GuardianSathiTheme(darkTheme = true) {
//        GuardianHomeScreen(
//            state = GuardianHomeState(
//                elderName = "Ramesh (Father)",
//                batteryLevel = 85,
//                isCharging = false,
//                lastBatterySeen = System.currentTimeMillis() - 2 * 60 * 1000L
//            ),
//            onAction = {}
//        )
//    }
//}
