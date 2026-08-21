package com.descope.deviceauthdemo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DescopePurple = Color(0xFF635BFF)
private val DescopePurpleDark = Color(0xFFB8B3FF)

private val LightColors = lightColorScheme(
    primary = DescopePurple,
    secondary = DescopePurple
)

private val DarkColors = darkColorScheme(
    primary = DescopePurpleDark,
    secondary = DescopePurpleDark
)

@Composable
fun DeviceAuthDemoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
