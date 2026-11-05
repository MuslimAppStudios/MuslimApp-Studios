package com.example.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldNightPrimary,
    onPrimary = Color.Black,
    primaryContainer = EmeraldNightCard,
    onPrimaryContainer = GoldNight,
    secondary = GoldNight,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF332A15),
    onSecondaryContainer = GoldLight,
    tertiary = Color(0xFF64B5F6),
    background = EmeraldNightBackground,
    onBackground = TextLight,
    surface = EmeraldNightSurface,
    onSurface = TextLight,
    surfaceVariant = EmeraldNightCard,
    onSurfaceVariant = TextLightMuted,
    outline = Color(0xFF2C4A3C)
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2F3EB),
    onPrimaryContainer = EmeraldDark,
    secondary = GoldDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF7DD),
    onSecondaryContainer = Color(0xFF5A4308),
    tertiary = Color(0xFF0277BD),
    background = SandBackground,
    onBackground = TextDark,
    surface = CreamSurface,
    onSurface = TextDark,
    surfaceVariant = Color(0xFFEBF3EE),
    onSurfaceVariant = TextMuted,
    outline = Color(0xFFCFDFD7)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use intentional emerald-gold palette
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
