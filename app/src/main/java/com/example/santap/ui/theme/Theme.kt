package com.example.santap.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ForestGreen, // #228B22
    onPrimary = Color.White,

    background = DarkBackground,
    onBackground = Color.White, // Teks terlihat putih di background gelap

    surface = EndGradient,
    onSurface = Color.White,

    secondary = AmberAccent, // #FFC300
    onSecondary = Color.Black,

    error = DeepRed, // #D32F2F
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen, // #228B22
    onPrimary = Color.White,

    background = LightBackground,
    onBackground = NeutralText, // Teks terlihat gelap di background terang

    surface = SurfaceWhite,
    onSurface = NeutralText,

    secondary = AmberAccent, // #FFC300
    onSecondary = Color.Black, // Teks hitam di atas aksen Amber/Kuning

    error = DeepRed, // #D32F2F
    onError = Color.White
)

@Composable
fun SANTAPTheme(
    // [PERBAIKAN]: Mengatur default ke False (Tema Terang) untuk visibilitas yang lebih baik
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Pastikan Typography sudah didefinisikan di Type.kt
        content = content
    )
}