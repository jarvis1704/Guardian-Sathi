package com.biprangshu.guardiansathi.Global.Elder.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biprangshu.guardiansathi.Global.core.Language
import com.biprangshu.guardiansathi.Global.core.LanguageUtils
import com.biprangshu.guardiansathi.Global.core.setAppLanguage
import com.biprangshu.guardiansathi.Global.presentation.onboarding.LanguageItem
import com.biprangshu.guardiansathi.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeLanguagePage(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val languages = listOf(
        Language("English", "English", "en"),
        Language("Hindi", "हिन्दी", "hi"),
        Language("Assamese", "অসমীয়া", "as")
    )

    val currentCode = LanguageUtils.getSavedLanguage(context)
    val currentLanguage = languages.find { it.code == currentCode } ?: languages[0]
    var selected by remember { mutableStateOf(currentLanguage) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.Settings_ChangeLanguage)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                languages.forEach { lang ->
                    item {
                        LanguageItem(
                            language = lang,
                            isSelected = selected == lang,
                            onClick = { selected = lang }
                        )
                    }
                }
            }

            Button(
                onClick = { setAppLanguage(context, selected.code) },
                enabled = selected != currentLanguage,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .height(55.dp),
                shape = RoundedCornerShape(30.dp)
            ) {
                Text(
                    stringResource(R.string.apply_button),
                    fontSize = 24.sp
                )
            }
        }
    }
}
