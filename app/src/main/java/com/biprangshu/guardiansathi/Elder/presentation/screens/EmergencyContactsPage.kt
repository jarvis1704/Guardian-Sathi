package com.biprangshu.guardiansathi.Elder.presentation.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biprangshu.guardiansathi.Elder.presentation.viewmodel.EmergencyNumbersViewmodel


data class EmergencyContactUi(
    val name: String,
    val subtitle: String,
    val phone: String
)

data class EmergencyNumber(
    val type: String,
    val name: String,
    val phoneNumber: String,
    val address: String = "",
    val distanceMeters: Double = 0.0,
    val isLocal: Boolean = false // true = Places API, false = hardcoded national
)

@Composable
fun EmergencyContactsPage(
    viewModel: EmergencyNumbersViewmodel = hiltViewModel()
){

    val context = LocalContext.current
    val onCallClick = { phoneNumber: String ->
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = "tel:$phoneNumber".toUri()
        }
        context.startActivity(intent)
    }
    val personalContacts: List<EmergencyContactUi> = emptyList()

    val nationalHelplines = listOf(
        EmergencyContactUi("Police Station",  "100",   "100"),
        EmergencyContactUi("Ambulance",       "108",   "108"),
        EmergencyContactUi("Fire Brigade",    "101",   "101"),
        EmergencyContactUi("Elder Helpline",  "14567", "14567"),
        EmergencyContactUi("Women Helpline",  "1091",  "1091"),
    )

    val state by viewModel.emergencyNumbersState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.loadEmergencyNumbers(context)
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 24.dp)
    ) {
        item {
            Spacer(Modifier.height(50.dp))
        }
        item {
            Text(
                text = "Emergency Contacts",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // personal section
        item {
            SectionHeader(
                icon = Icons.Rounded.Person,
                label = "Personal",
                color = MaterialTheme.colorScheme.primary
            )
        }
        items(personalContacts) { contact ->
            NationalContactRow(
                name = contact.name,
                subtitle = contact.subtitle,
                onCallClick = { onCallClick(contact.phone) }
            )
        }
        if (personalContacts.isEmpty()) {
            item {
                Text(
                    text = "No personal contacts added yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        // local helplines section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(
                icon = Icons.Rounded.LocationOn,
                label = "Local Helplines",
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        if (state.isFetching){
            item {
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20))
                        .background(MaterialTheme.colorScheme.surface),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Fetching nearby places",
                        modifier = Modifier.padding(12.dp))
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }else{
            items(state.emergencyNumbers) { number ->
                LocalContactRow(
                    title = number.type,
                    name = number.name,
                    subtitle = number.phoneNumber,
                    onCallClick = { onCallClick(number.phoneNumber) }
                )
            }
        }

        // national helplines section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(
                icon = Icons.Rounded.LocationOn,
                label = "National Helplines",
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        items(nationalHelplines) { contact ->
            NationalContactRow(
                name = contact.name,
                subtitle = contact.phone,
                onCallClick = { onCallClick(contact.phone) }
            )
        }
        item {
            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
private fun SectionHeader(
    icon: ImageVector,
    label: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = color,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun NationalContactRow(
    name: String,
    subtitle: String,
    onCallClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Button(
                onClick = onCallClick,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Call,
                    contentDescription = "Call",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Call",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}



@Composable
private fun LocalContactRow(
    title: String,
    name: String,
    subtitle: String,
    onCallClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column (
                modifier = Modifier
                    .fillMaxWidth(0.65f)
            ){
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(6.dp))

                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Button(
                onClick = onCallClick,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Call,
                    contentDescription = "Call",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Call",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}