package com.biprangshu.guardiansathi.Global.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biprangshu.guardiansathi.Global.ui.theme.GuardianSathiTheme


@Composable
fun LanguageSelectionPage() {
    val languages = listOf(
        Language("English", "English"),
        Language("Hindi", "हिन्दी"),
        Language("Bengali", "বাংলা"),
        Language("Tamil", "தமிழ்")
    )

    var selected by remember { mutableStateOf(languages[0]) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 50.dp)
            .padding(16.dp)
    ) {
//        Spacer(Modifier.height(10.dp))

        // Title
        Text(
            text = "Choose your language",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Select your preferred language to personalize your Saathi experience.",
            fontSize = 14.sp,
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
            onClick = { /* TODO */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(30.dp)
        ) {
            Text("Continue")
        }
    }
}

@Composable
fun LanguageItem(
    language: Language,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(backgroundColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(30.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = language.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Text(
                text = language.native,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        if (isSelected) {
//            Icon(
//                imageVector = Icons.Default.CheckCircle,
//                contentDescription = null,
//                tint = Color(0xFF3B82F6)
//            )
        }
    }
}

data class Language(
    val name: String,
    val native: String
)

@Preview(showBackground = true)
@Composable
fun LanguagePagePreview() {
    GuardianSathiTheme {
        LanguageSelectionPage()
    }
}