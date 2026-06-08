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
    secondary = BrandBlack,
    onSecondary = BaseWhite,
    secondaryContainer = BrandBlackTint,
    onSecondaryContainer = BrandBlack,
    tertiary = BrandRed,
    onTertiary = BaseWhite,
    tertiaryContainer = BrandRedTint,
    onTertiaryContainer = BrandRedDark,
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
    error = BrandRed,
    onError = BaseWhite
)

private val DarkColors = darkColorScheme(
    primary = BrandBlueLight,
    onPrimary = BrandBlueDark,
    primaryContainer = BrandBlue,
    onPrimaryContainer = BrandBlueTint,
    secondary = BrandBlackLight,
    onSecondary = OnSurfaceDark,
    secondaryContainer = Color(0xFF2A2A2A),
    onSecondaryContainer = OnSurfaceDark,
    tertiary = BrandRedLight,
    onTertiary = BrandRedDark,
    tertiaryContainer = BrandRed,
    onTertiaryContainer = BrandRedTint,
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
    error = BrandRedLight,
    onError = BrandRedDark
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
