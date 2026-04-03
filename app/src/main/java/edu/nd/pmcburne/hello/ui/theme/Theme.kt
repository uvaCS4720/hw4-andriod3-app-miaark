package edu.nd.pmcburne.hello.ui.theme

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

private val DarkColors = darkColorScheme(
    primary = Orange500,
    onPrimary = Color.White,
    secondary = Orange300,
    onSecondary = Color.White,
    background = Gray900,
    onBackground = Gray50,
    surface = Gray900,
    onSurface = Gray50
)

private val LightColors = lightColorScheme(
    primary = Orange500,
    onPrimary = Color.White,
    secondary = Orange300,
    onSecondary = Color.White,
    background = Gray50,
    onBackground = Gray900,
    surface = Color.White,
    onSurface = Color.Black,
)



@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // could detect system theme
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}