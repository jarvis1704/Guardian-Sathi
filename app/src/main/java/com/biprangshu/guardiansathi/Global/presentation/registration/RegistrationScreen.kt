package com.biprangshu.guardiansathi.Global.presentation.registration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.RadioButton
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
fun RegistrationRoot(
    onNavigateToMain: () -> Unit,
    viewModel: RegistrationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is RegistrationEvent.NavigateToMain -> onNavigateToMain()
            }
        }
    }

    RegistrationScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun RegistrationScreen(
    state: RegistrationState,
    onAction: (RegistrationAction) -> Unit
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
            Text(text = "Complete Registration")
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(text = "Are you an Elder or a Guardian?")
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = state.selectedRole == "ELDER",
                        onClick = { onAction(RegistrationAction.OnRoleSelect("ELDER")) }
                    )
                    Text("Elder")
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = state.selectedRole == "GUARDIAN",
                        onClick = { onAction(RegistrationAction.OnRoleSelect("GUARDIAN")) }
                    )
                    Text("Guardian")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { onAction(RegistrationAction.OnSubmitClick) },
                    enabled = state.selectedRole != null
                ) {
                    Text("Submit")
                }
            }
        }
    }
}

@Preview
@Composable
private fun RegistrationScreenPreview() {
    RegistrationScreen(state = RegistrationState(), onAction = {})
}
