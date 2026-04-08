package com.biprangshu.guardiansathi.feature.auth.presentation.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biprangshu.guardiansathi.core.domain.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageSelectionViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _events = MutableSharedFlow<Unit>()
    val events = _events.asSharedFlow()

    fun onLanguageSelected() {
        viewModelScope.launch {
            sessionRepository.setLanguageSelected(true)
            _events.emit(Unit)
        }
    }
}

@Composable
fun LanguageSelectionPlaceholderRoot(
    onNavigateNext: () -> Unit,
    viewModel: LanguageSelectionViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.events.collect {
            onNavigateNext()
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Language Selection")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.onLanguageSelected() }) {
                Text("Select English & Continue")
            }
        }
    }
}
