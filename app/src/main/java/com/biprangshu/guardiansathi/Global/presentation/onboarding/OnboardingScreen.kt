package com.biprangshu.guardiansathi.Global.presentation.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biprangshu.guardiansathi.Global.core.isGestureNav
import com.biprangshu.guardiansathi.R

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                when(state.currentPage){
                    0->{
                        Image(
                            painter = painterResource(R.drawable.ph_elder_technology),
                            contentDescription = "",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.9f),
                            contentScale = ContentScale.Crop
                            )
                        // Title
                        Text(
                            text = stringResource(R.string.onboarding_1_T),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .padding(13.dp)
                                .padding(top = 5.dp, bottom = 6.dp)
                        )

                        // Description
                        Text(
                            text = stringResource(R.string.onboarding_1_S),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                            lineHeight = 24.sp,
                            modifier = Modifier.padding(horizontal = 13.dp)
                        )
                    }
                    1->{
                        Image(
                            painter = painterResource(R.drawable.ph_elder_technology),
                            contentDescription = "",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.9f),
                            contentScale = ContentScale.Crop
                        )
                        // Title
                        Text(
                            text = stringResource(R.string.onboarding_2_T),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .padding(13.dp)
                                .padding(top = 5.dp, bottom = 6.dp)
                        )

                        // Description
                        Text(
                            text = stringResource(R.string.onboarding_2_S),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                            lineHeight = 24.sp,
                            modifier = Modifier.padding(horizontal = 13.dp)
                        )
                    }
                    2->{
                        Image(
                            painter = painterResource(R.drawable.ph_elder_technology),
                            contentDescription = "",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.9f),
                            contentScale = ContentScale.Crop
                        )
                        // Title
                        Text(
                            text = stringResource(R.string.onboarding_3_T),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .padding(13.dp)
                                .padding(top = 5.dp, bottom = 6.dp)
                        )

                        // Description
                        Text(
                            text = stringResource(R.string.onboarding_3_S),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                            lineHeight = 24.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                    else -> {

                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = if (isGestureNav) 40.dp else 90.dp)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { onAction(OnboardingAction.OnSkipClick) }) {
                    Text(
                        stringResource(R.string.onboarding_B2),
                        modifier = Modifier,
                        fontSize = 20.sp
                    )
                }
                
                Button(onClick = { onAction(OnboardingAction.OnNextClick) }) {
                    Text(
                        if (state.isLastPage) stringResource(R.string.onboarding_B3) else stringResource(R.string.onboarding_B1),
                        modifier = Modifier
                            .padding(2.dp),
                        fontSize = 20.sp
                    )
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
