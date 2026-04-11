package com.biprangshu.guardiansathi.Guardian.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biprangshu.guardiansathi.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GuardianHomeScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        // top Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 🔝 Top Section
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top=50.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Text(
                    text = "Currently Monitoring",
                    fontSize = 14.sp,
                    color = Color.White
                )

                Row(
                    modifier = Modifier
                        .padding(top = 24.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                    ) { }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Elder Name",
                        fontSize = 24.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                Row(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_google),
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Safe at Home",
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            FlowRow(
                maxItemsInEachRow = 2,
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(Modifier.weight(1f).padding(end = 6.dp)) {
                    HomeCard(
                        R.drawable.ic_google,
                        title = "85%",
                        sub = "Battery",
                        onclick = {}
                    )
                }
                Row(Modifier.weight(1f).padding(start = 6.dp)) {
                    HomeCard(
                        R.drawable.ic_google,
                        title = "SafeZone",
                        sub = "Location",
                        onclick = {}
                    )
                }
                Row(Modifier.weight(1f).padding(end = 6.dp)) {
                    HomeCard(
                        R.drawable.ic_google,
                        title = "Active",
                        sub = "Fall guard active",
                        onclick = {}
                    )
                }
                Row(Modifier.weight(1f).padding(start = 6.dp)) {
                    HomeCard(
                        R.drawable.ic_google,
                        title = "In 30 Mins",
                        sub = "Medicine reminder",
                        onclick = {}
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            //upcoming section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    "Upcoming",
                    Modifier.padding(bottom = 6.dp))
                repeat(4){
                    UpcomingCard(
                        icon = R.drawable.ic_google,
                        title = "fasdf",
                        sub = "thosajf",
                        onclick = {}
                    )
                }
            }
        }
    }
}

@Composable
fun HomeCard(
    icon: Int,
    title: String,
    sub: String,
    onclick:()-> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
            )
            Text(
                text = title,
                modifier = Modifier.padding(2.dp),
                fontSize = 24.sp
            )
            Text(
                text = sub,
                modifier = Modifier.padding(2.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun UpcomingCard(
    icon: Int,
    title: String,
    sub: String,
    onclick:()-> Unit
){
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
            )
            Text(
                text = title,
                modifier = Modifier.padding(2.dp),
                fontSize = 24.sp
            )
            Text(
                text = sub,
                modifier = Modifier.padding(2.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}