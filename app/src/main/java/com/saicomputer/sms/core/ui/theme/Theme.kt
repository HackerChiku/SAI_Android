package com.saicomputer.sms.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = BrandBlue,
    onPrimary = BaseWhite,
    primaryContainer = BrandBlueTint,
    onPrimaryContainer = BrandBlueDark,
    secondary = BrandRed,
    onSecondary = BaseWhite,
    secondaryContainer = BrandRedTint,
    onSecondaryContainer = BrandRedDark,
    tertiary = BrandGold,
    onTertiary = BrandGoldDark,
    tertiaryContainer = BrandGoldTint,
    onTertiaryContainer = BrandGoldDark,
    background = OffWhite,
    onBackground = OnSurfaceLight,
    surface = BaseWhite,
    onSurface = OnSurfaceLight,
    surfaceVariant = OffWhite,
    onSurfaceVariant = OnSurfaceVariantLightColor,
    surfaceContainerLowest = BaseWhite,
    surfaceContainerLow = BaseWhite,
    surfaceContainer = BaseWhite,
    surfaceContainerHigh = OffWhite,
    surfaceContainerHighest = OffWhite,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    error = StatusRed,
    onError = BaseWhite
)

private val DarkColors = darkColorScheme(
    primary = BrandBlueLight,
    onPrimary = BrandBlueDark,
    primaryContainer = BrandBlue,
    onPrimaryContainer = BrandBlueTint,
    secondary = BrandRedLight,
    onSecondary = BrandRedDark,
    secondaryContainer = BrandRed,
    onSecondaryContainer = BrandRedTint,
    tertiary = BrandGoldLight,
    onTertiary = BrandGoldDark,
    tertiaryContainer = BrandGold,
    onTertiaryContainer = BrandGoldDark,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDarkColor,
    surfaceContainerLowest = SurfaceDark,
    surfaceContainerLow = SurfaceDarkElevated,
    surfaceContainer = SurfaceDarkElevated,
    surfaceContainerHigh = SurfaceVariantDark,
    surfaceContainerHighest = SurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF3A0006)
)

@Composable
fun SaiSmsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
