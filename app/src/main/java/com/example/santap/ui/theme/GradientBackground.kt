package com.example.santap.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Preset warna gradasi untuk layar login & register
val LoginGradientColors = listOf(StartGradient, EndGradient)
val RegisterGradientColors = listOf(EndGradient, ForestGreen)

/**
 * Wrapper background gradasi vertikal untuk layar SANTAP.
 * Tinggal panggil SantapGradientBackground { ... } di screen.
 */
@Composable
fun SantapGradientBackground(
    modifier: Modifier = Modifier,
    colors: List<Color>,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(colors = colors)
            )
    ) {
        content()
    }
}
