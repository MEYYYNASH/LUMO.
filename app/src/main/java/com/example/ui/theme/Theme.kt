package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val LocalLumoPalette = staticCompositionLocalOf { ObsidianPalette }

val LumoShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun LumoTheme(
    themeOption: LumoThemeOption = LumoThemeOption.OBSIDIAN,
    activeDestination: LumoDestination = LumoDestination.HOME,
    colorReactiveState: LumoColorReactiveState = LumoColorReactiveState(),
    language: LumoLanguage = LumoLanguage.KHMER,
    content: @Composable () -> Unit
) {
    val palette = remember(themeOption) { paletteForTheme(themeOption) }
    val activeAccent = remember(palette, activeDestination) { palette.accentForTab(activeDestination) }
    val stableSecondaryAccent = colorReactiveState.accentColor

    val colorScheme = remember(palette, activeAccent, stableSecondaryAccent) {
        if (palette.option.isDark) {
            darkColorScheme(
                primary = activeAccent,
                onPrimary = Color.White,
                primaryContainer = palette.elevatedSurface,
                onPrimaryContainer = palette.primaryText,
                secondary = stableSecondaryAccent,
                onSecondary = Color.White,
                secondaryContainer = palette.elevatedSurface,
                onSecondaryContainer = palette.primaryText,
                tertiary = palette.searchAccent,
                background = palette.background,
                onBackground = palette.primaryText,
                surface = palette.surface,
                onSurface = palette.primaryText,
                surfaceVariant = palette.elevatedSurface,
                onSurfaceVariant = palette.secondaryText,
                outline = palette.divider,
                outlineVariant = palette.divider.copy(alpha = 0.6f)
            )
        } else {
            lightColorScheme(
                primary = activeAccent,
                onPrimary = Color.White,
                primaryContainer = palette.elevatedSurface,
                onPrimaryContainer = palette.primaryText,
                secondary = stableSecondaryAccent,
                onSecondary = Color.White,
                secondaryContainer = palette.elevatedSurface,
                onSecondaryContainer = palette.primaryText,
                tertiary = palette.searchAccent,
                background = palette.background,
                onBackground = palette.primaryText,
                surface = palette.surface,
                onSurface = palette.primaryText,
                surfaceVariant = palette.elevatedSurface,
                onSurfaceVariant = palette.secondaryText,
                outline = palette.divider,
                outlineVariant = palette.divider.copy(alpha = 0.7f)
            )
        }
    }

    CompositionLocalProvider(
        LocalLumoPalette provides palette,
        LocalColorReactiveState provides colorReactiveState,
        LocalLumoLanguage provides language
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = LumoShapes,
            content = content
        )
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    LumoTheme(
        themeOption = if (darkTheme) LumoThemeOption.OBSIDIAN else LumoThemeOption.SNOW,
        content = content
    )
}
