package com.example.santap.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,

    background = DarkBackground,
    onBackground = Color.White,

    surface = EndGradient,
    onSurface = Color.White,

    secondary = AmberAccent,
    onSecondary = Color.Black,

    error = DeepRed, // #D32F2F
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,

    background = LightBackground,
    onBackground = NeutralText,

    surface = Color(0xFFFFFFFF),
    onSurface = NeutralText,

    secondary = AmberAccent,
    onSecondary = Color.Black,

    error = DeepRed,
    onError = Color.White
)

@Composable
fun SANTAPTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}