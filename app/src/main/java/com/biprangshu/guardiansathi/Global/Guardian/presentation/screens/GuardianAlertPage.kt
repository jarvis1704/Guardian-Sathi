package com.biprangshu.guardiansathi.Global.Guardian.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Battery2Bar
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FmdBad
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biprangshu.guardiansathi.Global.Guardian.data.GuardianAlert
import com.biprangshu.guardiansathi.Global.Guardian.presentation.viewmodel.GuardianAlertViewModel
import com.biprangshu.guardiansathi.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val CriticalRed = Color(0xFFE53935)
private val WarningOrange = Color(0xFFFF9800)
private val InfoBlue = Color(0xFF2196F3)
private val SuccessGreen = Color(0xFF4CAF50)

private enum class AlertSeverity { CRITICAL, WARNING, INFO, SUCCESS }

private data class AlertItem(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val time: String,
    val severity: AlertSeverity
)

private fun GuardianAlert.toAlertItem(): AlertItem {
    val severity = when {
        title.contains("Fall", ignoreCase = true) -> AlertSeverity.CRITICAL
        imp.equals("HIGH", ignoreCase = true) -> AlertSeverity.WARNING
        title.contains("Check-In", ignoreCase = true) -> AlertSeverity.SUCCESS
        else -> AlertSeverity.INFO
    }

    val icon = when {
        title.contains("Fall", ignoreCase = true) -> Icons.Outlined.FmdBad
        title.contains("Battery", ignoreCase = true) -> Icons.Outlined.Battery2Bar
        title.contains("Zone", ignoreCase = true) -> Icons.Outlined.LocationOff
        title.contains("Check", ignoreCase = true) -> Icons.Outlined.CheckCircle
        imp.equals("HIGH", ignoreCase = true) -> Icons.Outlined.WarningAmber
        else -> Icons.Outlined.Notifications
    }

    val timeString = if (timestamp > 0) {
        val formatter = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
        formatter.format(Date(timestamp))
    } else {
        "Just now"
    }

    return AlertItem(
        icon = icon,
        title = title,
        description = desc.ifEmpty { body },
        time = timeString,
        severity = severity
    )
}

@Composable
fun GuardianAlertPage(
    viewModel: GuardianAlertViewModel = hiltViewModel()
) {
    var selectedFilter by remember { mutableIntStateOf(0) }
    val alerts by viewModel.alerts.collectAsStateWithLifecycle()
    val mappedAlerts = remember(alerts) {
        alerts.map { it.toAlertItem() }
    }
    val filters = listOf(
        stringResource(R.string.guardian_alerts_filter_all),
        stringResource(R.string.guardian_alerts_filter_critical),
        stringResource(R.string.guardian_alerts_filter_warning),
        stringResource(R.string.guardian_alerts_filter_info)
    )

    val displayedAlerts = when (selectedFilter) {
        1 -> mappedAlerts.filter { it.severity == AlertSeverity.CRITICAL }
        2 -> mappedAlerts.filter { it.severity == AlertSeverity.WARNING }
        3 -> mappedAlerts.filter { it.severity == AlertSeverity.INFO || it.severity == AlertSeverity.SUCCESS }
        else -> mappedAlerts
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Spacer(Modifier.height(10.dp)) }

        // ── Title ────────────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.guardian_alerts_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = CircleShape,
                    color = CriticalRed.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Outlined.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = CriticalRed
                        )
                    }
                }
            }
        }

        // ── Filter chips ─────────────────────────────────────────────────────
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filters.indices.toList()) { idx ->
                    FilterChip(
                        selected = selectedFilter == idx,
                        onClick = { selectedFilter = idx },
                        label = { Text(filters[idx], style = MaterialTheme.typography.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        if (displayedAlerts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = SuccessGreen
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.guardian_alerts_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(displayedAlerts) { alert ->
                AlertCard(alert)
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun AlertCard(alert: AlertItem) {
    val (accentColor, bgColor) = when (alert.severity) {
        AlertSeverity.CRITICAL -> CriticalRed to CriticalRed.copy(alpha = 0.1f)
        AlertSeverity.WARNING -> WarningOrange to WarningOrange.copy(alpha = 0.1f)
        AlertSeverity.INFO -> InfoBlue to InfoBlue.copy(alpha = 0.1f)
        AlertSeverity.SUCCESS -> SuccessGreen to SuccessGreen.copy(alpha = 0.1f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = bgColor,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = alert.icon,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = accentColor
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = alert.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = alert.time,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = bgColor
            ) {
                Text(
                    text = alert.severity.name.lowercase()
                        .replaceFirstChar { it.uppercase() },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
