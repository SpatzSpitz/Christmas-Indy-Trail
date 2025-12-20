package com.example.christmasindytrail.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = CyanAccent,
    onPrimary = Color.Black,
    secondary = GoldAccent,
    onSecondary = Color.Black,
    background = Obsidian,
    onBackground = Color.White,
    surface = SurfaceDark,
    onSurface = Color.White,
    surfaceVariant = SurfaceDarker,
    onSurfaceVariant = TextMuted,
)

private val LightColors = lightColorScheme(
    primary = CyanAccent,
    onPrimary = Color.Black,
    secondary = GoldAccent,
    onSecondary = Color.Black,
    background = Obsidian,
    onBackground = Color.White,
    surface = SurfaceDark,
    onSurface = Color.White,
    surfaceVariant = SurfaceDarker,
    onSurfaceVariant = TextMuted,
)

@Composable
fun ChristmasIndyTrailTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme || isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
