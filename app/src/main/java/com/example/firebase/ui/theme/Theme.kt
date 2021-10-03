package com.example.firebase.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = Firebase200,
    primaryVariant = FirebaseVariant,
    onPrimary = Color.White,
    secondary = Teal200,
    onSecondary = Color.White,
    error = NoConnection,
    onError = Color.White,
    secondaryVariant = Connection
)

private val LightColorPalette = lightColors(
    primary = Firebase200,
    primaryVariant = FirebaseVariant,
    onPrimary = Color.White,
    secondary = Teal200,
    onSecondary = Color.White,
    error = NoConnection,
    onError = Color.White,
    secondaryVariant = Connection
)

@Composable
fun FirebaseTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable() () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}