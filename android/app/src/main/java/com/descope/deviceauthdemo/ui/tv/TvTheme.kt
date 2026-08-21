package com.descope.deviceauthdemo.ui.tv

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

private val DescopePurple = Color(0xFFB8B3FF)

// Android TV is almost always viewed in a dark room against a dark UI, so
// unlike the phone theme (which follows system light/dark), this one just
// commits to a single dark look.
private val TvColors = darkColorScheme(
    primary = DescopePurple,
    secondary = DescopePurple
)

@Composable
fun TvDeviceAuthDemoTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = TvColors, content = content)
}
