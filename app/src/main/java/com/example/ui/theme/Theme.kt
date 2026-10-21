package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

@Composable
fun AmarHisabTheme(
    themeColors: AppThemeColors = AppThemes.BlueTheme,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val lightColorScheme = lightColorScheme(
        primary = themeColors.primary,
        onPrimary = White,
        primaryContainer = themeColors.container,
        onPrimaryContainer = themeColors.primaryDark,
        secondary = themeColors.primaryDark,
        onSecondary = White,
        secondaryContainer = themeColors.container,
        onSecondaryContainer = themeColors.primaryDark,
        tertiary = themeColors.income,
        onTertiary = White,
        background = themeColors.background,
        onBackground = themeColors.textPrimary,
        surface = themeColors.surface,
        onSurface = themeColors.textPrimary,
        surfaceVariant = themeColors.container,
        onSurfaceVariant = themeColors.textPrimary,
        outline = themeColors.border,
        error = themeColors.expense,
        onError = White
    )

    CompositionLocalProvider(
        LocalAppTheme provides themeColors
    ) {
        MaterialTheme(
            colorScheme = lightColorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
