package com.biprangshu.guardiansathi.feature.auth.presentation.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
fun LoginRoot(
    onNavigateToRegistration: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LoginEvent.NavigateToRegistration -> onNavigateToRegistration()
            }
        }
    }

    LoginScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun LoginScreen(
    state: LoginState,
    onAction: (LoginAction) -> Unit
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
            Text(text = "Login")
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(text = "Placeholder for Phone/Google Login fields.")
            Spacer(modifier = Modifier.height(32.dp))

            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Button(onClick = { onAction(LoginAction.OnLoginClick) }) {
                    Text("Mock Login")
                }
            }
        }
    }
}

@Preview
@Composable
private fun LoginScreenPreview() {
    LoginScreen(state = LoginState(), onAction = {})
}
