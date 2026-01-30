package com.tonio.albarapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Blue700,
    onPrimary = Color.White,
    primaryContainer = Blue50,
    onPrimaryContainer = Blue700,

    secondary = Orange700,
    onSecondary = Color.White,
    secondaryContainer = Orange100,
    onSecondaryContainer = Orange700,

    tertiary = Green700,
    onTertiary = Color.White,
    tertiaryContainer = Green100,
    onTertiaryContainer = Green700,

    error = Red700,
    onError = Color.White,
    errorContainer = Red100,
    onErrorContainer = Red700,

    background = Grey50,
    onBackground = Grey900,

    surface = Color.White,
    onSurface = Grey900,
    surfaceVariant = Grey100,
    onSurfaceVariant = Grey700,

    outline = Grey300,
    outlineVariant = Grey100
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue500,
    onPrimary = Grey900,
    primaryContainer = Blue700,
    onPrimaryContainer = Blue100,

    secondary = Orange500,
    onSecondary = Grey900,
    secondaryContainer = Orange700,
    onSecondaryContainer = Orange100,

    tertiary = Green500,
    onTertiary = Grey900,
    tertiaryContainer = Green700,
    onTertiaryContainer = Green100,

    error = Red500,
    onError = Grey900,
    errorContainer = Red700,
    onErrorContainer = Red100,

    background = Grey900,
    onBackground = Grey50,

    surface = Color(0xFF1C1C1E),
    onSurface = Grey50,
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = Grey300,

    outline = Grey700,
    outlineVariant = Grey700
)

@Composable
fun AlbarappTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Changed to false to use our custom colors
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}