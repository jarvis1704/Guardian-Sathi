package com.biprangshu.guardiansathi.Global.presentation.registration

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biprangshu.guardiansathi.Global.core.isGestureNav
import com.biprangshu.guardiansathi.R

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
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding)
//                .padding(16.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            Text(text = "Complete Registration")
//            Spacer(modifier = Modifier.height(32.dp))
//
//            Text(text = "Are you an Elder or a Guardian?")
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceEvenly
//            ) {
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    RadioButton(
//                        selected = state.selectedRole == "ELDER",
//                        onClick = { onAction(RegistrationAction.OnRoleSelect("ELDER")) }
//                    )
//                    Text("Elder")
//                }
//
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    RadioButton(
//                        selected = state.selectedRole == "GUARDIAN",
//                        onClick = { onAction(RegistrationAction.OnRoleSelect("GUARDIAN")) }
//                    )
//                    Text("Guardian")
//                }
//            }
//
//            Spacer(modifier = Modifier.height(32.dp))
//
//            if (state.isLoading) {
//                CircularProgressIndicator()
//            } else {
//                Button(
//                    onClick = { onAction(RegistrationAction.OnSubmitClick) },
//                    enabled = state.selectedRole != null
//                ) {
//                    Text("Submit")
//                }
//            }
//        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .background(MaterialTheme.colorScheme.background)
        ){
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.TopCenter)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(vertical = 24.dp)
                    .padding(top = 30.dp)
            ) {

                Spacer(modifier = Modifier.height(24.dp))

                // Title
                Text(
                    text = stringResource(R.string.registration_type_T),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle
                Text(
                    text = stringResource(R.string.registration_type_S),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(0.7f)
                )

                Spacer(modifier = Modifier.height(32.dp))

                LazyColumn(
                ) {
                    item {
                        // Elder Card
                        RoleCard(
                            icon = R.drawable.ic_elder,
                            title = stringResource(R.string.registration_type_elder_T),
                            description = stringResource(R.string.registration_type_elder_S),
                            isselected = state.selectedRole == "ELDER",
                            onClick = {
                                onAction(RegistrationAction.OnRoleSelect("ELDER"))
                            }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Guardian Card
                        RoleCard(
                            icon = R.drawable.ic_guardian,
                            title = stringResource(R.string.registration_type_guardian_T),
                            description = stringResource(R.string.registration_type_guardian_S),
                            isselected = state.selectedRole == "GUARDIAN",
                            onClick = {
                                onAction(RegistrationAction.OnRoleSelect("GUARDIAN"))
                            }
                        )
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }

            //continue button
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp)
            ) {
                Button(
                    enabled = state.selectedRole != null,
                    onClick = {
                        onAction(RegistrationAction.OnSubmitClick)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(30.dp)
                ) {
                    Text(
                        stringResource(R.string.continue_button),
                        fontSize = 24.sp
                    )
                }
                Spacer(modifier = Modifier.height(if (isGestureNav) 44.dp else 100.dp))
            }

        }
    }
}

@Composable
fun RoleCard(
    icon: Int,
    title: String,
    description: String,
    isselected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(2.dp, if (isselected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            // Icon circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isselected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(icon),
                    contentDescription = "",
                    modifier = Modifier
                        .fillMaxSize(0.6f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                fontSize = 13.sp,
            )
        }
    }
}

@Preview
@Composable
private fun RegistrationScreenPreview() {
    RegistrationScreen(state = RegistrationState(), onAction = {})
}
