package com.biprangshu.guardiansathi.Global.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import com.biprangshu.guardiansathi.R

@Composable
fun ConnectionSuccessDialog(
    name: String,
    photourl_1: String,
    photourl_2: String,
    onContinue: () -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = null,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Title
                Text(
                    text = stringResource(R.string.ConnectionSuccess_H),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Avatars (placeholder)
                Row(
                    modifier = Modifier
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AsyncImage(
                        model = photourl_1,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        error = painterResource(R.drawable.ic_profile_placeholder),
                        placeholder = painterResource(R.drawable.ic_profile_placeholder)
                    )
                    AsyncImage(
                        model = photourl_2,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        error = painterResource(R.drawable.ic_profile_placeholder),
                        placeholder = painterResource(R.drawable.ic_profile_placeholder)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Message
                Text(
                    text = stringResource(R.string.ConnectionSuccess_D),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(44.dp))

                // Continue button
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(stringResource(R.string.continue_button))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Retry
                TextButton(onClick = onRetry) {
                    Text(
                        text = stringResource(R.string.ConnectionSuccess_retry),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp)
    )
}