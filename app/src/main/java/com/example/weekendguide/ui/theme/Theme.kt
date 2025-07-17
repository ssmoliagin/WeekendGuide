package com.example.weekendguide.ui.theme

import android.app.Activity
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

val LightColorScheme = lightColorScheme(
    primary = Blue,
    primaryContainer = Blue,
    secondary = LightBlue,
    tertiary = YellowLight,
    background = White,
    surface = Gray,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Dark,
    onBackground = Dark,
    onSurface = Dark
)

val DarkColorScheme = darkColorScheme(
    primary = Blue,
    primaryContainer = Dark,
    secondary = LightBlue,
    tertiary = Yellow,
    background = Dark,
    surface = Color(0xFF121212),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = White,
    onSurface =  White
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