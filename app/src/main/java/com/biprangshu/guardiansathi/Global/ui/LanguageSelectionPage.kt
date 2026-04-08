package com.biprangshu.guardiansathi.Global.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biprangshu.guardiansathi.Global.setAppLanguage
import com.biprangshu.guardiansathi.Global.ui.theme.GuardianSathiTheme
import com.biprangshu.guardiansathi.R

@Composable
fun LanguageSelectionPage() {
    val languages = listOf(
        Language("English", "English", "en"),
        Language("Hindi", "हिन्दी", "hi"),
        Language("Assamese", "অসমীয়া","as")
    )

    var selected by remember { mutableStateOf(languages[0]) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            // Title
            Text(
                text = "Choose your language",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Select your preferred language to personalize your Saathi experience.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 34.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Language List
            languages.forEach { lang ->
                LanguageItem(
                    language = lang,
                    isSelected = selected == lang,
                    onClick = { selected = lang }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            // Continue Button
            Button(
                onClick = {
                    setAppLanguage(selected.code)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(30.dp)
            ) {
                Text("Continue")
            }
        }
    }
}

@Composable
fun LanguageItem(
    language: Language,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Use Container colors for the background and OnContainer for text when selected
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = backgroundColor,
        contentColor = contentColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = language.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = language.native,
                    fontSize = 14.sp,
                    color = contentColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

data class Language(
    val name: String,
    val native: String,
    val code: String
)

@Preview(showBackground = true)
@Composable
fun LanguagePagePreview() {
    GuardianSathiTheme(dynamicColor = false) {
        LanguageSelectionPage()
    }
}
