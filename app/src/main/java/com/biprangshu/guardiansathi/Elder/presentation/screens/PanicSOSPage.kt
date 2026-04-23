package com.biprangshu.guardiansathi.Elder.presentation.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.biprangshu.guardiansathi.Elder.presentation.viewmodel.ElderSettingsViewModel
import com.biprangshu.guardiansathi.R
import kotlinx.coroutines.delay


@Composable
fun PanicSOSPage(
    onNavigateBack: () -> Unit = {},
    onImOkay: () -> Unit = {},
    onTimerFinished: () -> Unit = {},
    elderSettingsViewModel: ElderSettingsViewModel = hiltViewModel()
) {
    var secondsLeft by remember { mutableIntStateOf(10) }
    var timerFinished by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        for (i in 10 downTo 1) {
            secondsLeft = i
            delay(1000L)
        }
        timerFinished = 1
        elderSettingsViewModel.PanicSOS()
        onTimerFinished()
        onNavigateBack()
    }

    val progress by animateFloatAsState(
        targetValue = secondsLeft / 10f,
        label = "timer"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFB71C1C)),  // deep red — urgent
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text("⚠️", fontSize = 64.sp)

            Text(
                text = stringResource(R.string.ElderHome_5),
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )

            Text(
                text = stringResource(R.string.FallAlert_2)+" $secondsLeft seconds",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.85f)
            )

            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(80.dp),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f),
                strokeWidth = 6.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onImOkay()
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFFB71C1C)
                )
            ) {
                Text(
                    text = stringResource(R.string.FallAlert_3),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}