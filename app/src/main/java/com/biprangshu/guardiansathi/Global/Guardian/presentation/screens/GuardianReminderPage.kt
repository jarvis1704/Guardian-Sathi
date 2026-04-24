package com.biprangshu.guardiansathi.Global.Guardian.presentation.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.biprangshu.guardiansathi.R

private val CompletedGreen = Color(0xFF4CAF50)
private val UpcomingBlue = Color(0xFF2196F3)
private val MissedRed = Color(0xFFE53935)

private enum class ReminderStatus { COMPLETED, UPCOMING, MISSED }

private data class ReminderItem(
    val time: String,
    val period: String,
    val medicineName: String,
    val dose: String,
    val status: ReminderStatus
)

// TODO: Replace with real reminder data from Firestore when reminders feature is built
private val todayReminders = listOf(
    ReminderItem("8:00 AM", "Morning", "Metformin", "500 mg", ReminderStatus.COMPLETED),
    ReminderItem("2:00 PM", "Afternoon", "Amlodipine", "5 mg", ReminderStatus.UPCOMING),
    ReminderItem("8:00 PM", "Evening", "Atenolol", "25 mg", ReminderStatus.UPCOMING),
    ReminderItem("10:00 PM", "Night", "Rosuvastatin", "10 mg", ReminderStatus.UPCOMING)
)

@Composable
fun GuardianReminderPage() {
    Scaffold(
        floatingActionButton = {
            // TODO: wire up actual add reminder flow when reminders feature is built
            FloatingActionButton(
                onClick = { /* TODO */ },
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.guardian_reminders_add),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            // ── Title ────────────────────────────────────────────────────────
            Text(
                text = stringResource(R.string.guardian_reminders_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.guardian_reminders_today),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            // ── Summary strip ─────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val completedCount = todayReminders.count { it.status == ReminderStatus.COMPLETED }
                val upcomingCount = todayReminders.count { it.status == ReminderStatus.UPCOMING }
                val missedCount = todayReminders.count { it.status == ReminderStatus.MISSED }

                SummaryChip(
                    count = completedCount,
                    label = stringResource(R.string.guardian_reminders_status_done),
                    color = CompletedGreen,
                    modifier = Modifier.weight(1f)
                )
                SummaryChip(
                    count = upcomingCount,
                    label = stringResource(R.string.guardian_reminders_status_upcoming),
                    color = UpcomingBlue,
                    modifier = Modifier.weight(1f)
                )
                SummaryChip(
                    count = missedCount,
                    label = stringResource(R.string.guardian_reminders_status_missed),
                    color = MissedRed,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Reminder list ─────────────────────────────────────────────────
            Text(
                text = stringResource(R.string.guardian_reminders_schedule),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                todayReminders.forEach { reminder ->
                    ReminderCard(reminder)
                }
            }

            Spacer(Modifier.height(80.dp)) // space for FAB
        }
    }
}

@Composable
private fun SummaryChip(
    count: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

@Composable
private fun ReminderCard(reminder: ReminderItem) {
    val (statusColor, statusLabel) = when (reminder.status) {
        ReminderStatus.COMPLETED -> CompletedGreen to R.string.guardian_reminders_status_done
        ReminderStatus.UPCOMING -> UpcomingBlue to R.string.guardian_reminders_status_upcoming
        ReminderStatus.MISSED -> MissedRed to R.string.guardian_reminders_status_missed
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time column
            Column(
                modifier = Modifier.width(56.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = reminder.time.substringBefore(" "),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = reminder.time.substringAfter(" "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(12.dp))

            // Pill icon
            Surface(
                shape = CircleShape,
                color = statusColor.copy(alpha = 0.12f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.Medication,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = statusColor
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Medicine info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.medicineName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${reminder.dose} · ${reminder.period}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Status pill
            Surface(
                shape = RoundedCornerShape(50),
                color = statusColor.copy(alpha = 0.12f)
            ) {
                Text(
                    text = stringResource(statusLabel),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
