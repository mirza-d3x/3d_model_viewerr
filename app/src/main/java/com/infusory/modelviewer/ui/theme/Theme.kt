package com.infusory.modelviewer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AccentCyan,
    secondary = AccentPurple,
    tertiary = AccentEmerald,
    background = Slate950,
    surface = Slate900,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Slate200,
    onSurface = Slate200
)

@Composable
fun ModelViewerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
