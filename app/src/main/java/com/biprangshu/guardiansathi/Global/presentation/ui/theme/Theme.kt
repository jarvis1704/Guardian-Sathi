package com.biprangshu.guardiansathi.Global.presentation.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xff2771DF),
    onPrimary = Color.White,
    primaryContainer = Color(0xff004596),
    onPrimaryContainer = Color(0xffD6E3FF),
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xff121212),
    onBackground = Color.White,
    surface = Color(0xff1C1B1F),
    onSurface = Color.White,
    surfaceVariant = Color(0xff44474E),
    onSurfaceVariant = Color(0xffC4C6D0),
    outline = Color(0xff8E9099)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xff0058BD),
    onPrimary = Color.White,
    primaryContainer = Color(0xffD6E3FF),
    onPrimaryContainer = Color(0xff001B3F),
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(0xffF5F7FA),
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color(0xffE1E2EC),
    onSurfaceVariant = Color(0xff44474E),
    outline = Color(0xff74777F)
)

@Composable
fun GuardianSathiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set to false by default to ensure your branding colors are used
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
