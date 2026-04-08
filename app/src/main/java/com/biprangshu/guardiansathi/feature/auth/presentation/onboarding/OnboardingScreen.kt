package com.biprangshu.guardiansathi.feature.auth.presentation.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun OnboardingRoot(
    onNavigateToLogin: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is OnboardingEvent.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    OnboardingScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun OnboardingScreen(
    state: OnboardingState,
    onAction: (OnboardingAction) -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Onboarding Page ${state.currentPage + 1} of 3")
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(text = "Placeholder for Onboarding UI details.")
            
            Spacer(modifier = Modifier.weight(1f))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { onAction(OnboardingAction.OnSkipClick) }) {
                    Text("Skip")
                }
                
                Button(onClick = { onAction(OnboardingAction.OnNextClick) }) {
                    Text(if (state.isLastPage) "Get Started" else "Next")
                }
            }
        }
    }
}

@Preview
@Composable
private fun OnboardingScreenPreview() {
    OnboardingScreen(state = OnboardingState(), onAction = {})
}
