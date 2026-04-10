package com.biprangshu.guardiansathi.Elder.presentation.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biprangshu.guardiansathi.Elder.core.generateQrCodeAsBitmap
import com.biprangshu.guardiansathi.Elder.core.shareQrCodeToWhatsApp
import com.biprangshu.guardiansathi.Global.core.isGestureNav
import com.biprangshu.guardiansathi.Global.presentation.registration.RegistrationScreen
import com.biprangshu.guardiansathi.Global.presentation.registration.RegistrationState
import com.biprangshu.guardiansathi.R

@Composable
fun LinkGuardianPage(){
    //qr code generation, this is a test user id
    val userId = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
    val qrBitmap: Bitmap = remember(userId) {
        generateQrCodeAsBitmap(content = "user_id:$userId")
    }
    val qrImageBitmap = remember(qrBitmap) { qrBitmap.asImageBitmap() }
    val context = LocalContext.current
    //test code
    val code = "SAATHI-1234"
    val message = stringResource(R.string.LinkGuardian_Share_Message)+" "+code

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .padding(top = 50.dp)
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = stringResource(R.string.LinkGuardian_T),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = stringResource(R.string.LinkGuardian_S),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // QR Card
        Box(
            modifier = Modifier
                .size(260.dp)
                .border(
                    width = 4.dp,
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Replace with your QR image
//            Image(
//                painter = painterResource(id = R.drawable.ph_elder_technology),
//                contentDescription = "QR Code",
//                modifier = Modifier.size(200.dp)
//            )
            Image(
                bitmap = qrImageBitmap,
                contentDescription = "QR Code for user",
                modifier = Modifier
                    .size(200.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.LinkGuardian_S2),
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Code box
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = code,
                modifier = Modifier
                    .padding(20.dp)
                    .padding(horizontal = 6.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Waiting status
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = stringResource(R.string.LinkGuardian_S3),
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Share button
        Button(
            onClick = {
                shareQrCodeToWhatsApp(
                    context = context,
                    bitmap = qrBitmap,
                    message = message
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                tint = Color.White
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = stringResource(R.string.LinkGuardian_Share),
                fontSize = 16.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.LinkGuardian_Share_S),
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = if (isGestureNav) 20.dp else 70.dp)
        )
    }
}

@Preview
@Composable
private fun linkScreenPreview() {
    LinkGuardianPage()
}