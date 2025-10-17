package com.weekendguide.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

val LightColorScheme = lightColorScheme(
    background = White, // White/Black
    onBackground = Black, // Black/White
    surface = GrayLight, // GrayLight/Gray
    onSurface = Gray, // Gray/Gray
    primary = Blue, // Blue/BlueLight
    primaryContainer = Blue, // Blue/Black
    onSecondary = White, // White/DarkGray
    tertiary = YellowLight, // YellowLight/Yellow
    surfaceVariant = White, // surface + elevation  4.dp

)

val DarkColorScheme = darkColorScheme(
    background = Black,
    onBackground = White,
    surface = DarkGray,
    onSurface = Gray,
    primary = BlueLight,
    primaryContainer = Black,
    onSecondary = DarkGray,
    tertiary = Yellow,
    surfaceVariant = CardGrayDark,

)

@Composable
fun WeekendGuideTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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