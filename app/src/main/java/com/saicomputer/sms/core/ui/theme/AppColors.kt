package com.saicomputer.sms.core.ui.theme

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.ColorRes
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.saicomputer.sms.R

data class AppColorSet(
    val brandPrimary: Color,
    val brandPrimaryDark: Color,
    val brandPrimaryContainer: Color,
    val brandOnPrimary: Color,
    val brandSecondary: Color,
    val brandSecondaryDark: Color,
    val brandSecondaryContainer: Color,
    val brandOnSecondary: Color,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceVariant: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val outlineVariant: Color,
    val success: Color,
    val warning: Color,
    val info: Color,
    val error: Color,
    val neutral: Color,
    val neutralMuted: Color,
    val purple: Color,
    val accentBlue: Color,
    val accentGreen: Color,
    val accentPurple: Color,
    val accentCyan: Color,
    val accentRed: Color,
    val rowPaid: Color,
    val rowOverdue: Color,
    val rowPending: Color,
    val rowSelected: Color,
    val warningContainer: Color,
    val callAction: Color,
    val callActionContainer: Color,
    val chartLine: Color,
    val chartGrid: Color,
    val chartAxis: Color,
    val chartAxisX: Color,
    val chartFillStart: Color,
    val chartUpi: Color,
    val chartCash: Color,
    val chartQr: Color,
    val chartPoint: Color,
    val avatarPalettes: List<Pair<Color, Color>>,
    val primaryContainerOn: Color,
    val secondaryContainerOn: Color,
    val tertiaryContainer: Color,
    val tertiaryContainerOn: Color,
    val highlightContainer: Color,
    val listItemSurface: Color
) {
    val isDark: Boolean get() = background.luminance() < 0.5f

    fun toMaterialColorScheme(): ColorScheme {
        val colors = mapOf(
            "primary" to brandPrimary,
            "onPrimary" to brandOnPrimary,
            "primaryContainer" to brandPrimaryContainer,
            "onPrimaryContainer" to primaryContainerOn,
            "secondary" to neutral,
            "onSecondary" to onSurface,
            "secondaryContainer" to surfaceElevated,
            "onSecondaryContainer" to onSurface,
            "tertiary" to brandSecondary,
            "onTertiary" to brandOnSecondary,
            "tertiaryContainer" to tertiaryContainer,
            "onTertiaryContainer" to tertiaryContainerOn,
            "background" to background,
            "onBackground" to onSurface,
            "surface" to surface,
            "onSurface" to onSurface,
            "surfaceVariant" to surfaceVariant,
            "onSurfaceVariant" to onSurfaceVariant,
            "surfaceContainerLowest" to background,
            "surfaceContainerLow" to surface,
            "surfaceContainer" to surface,
            "surfaceContainerHigh" to surfaceElevated,
            "surfaceContainerHighest" to surfaceElevated,
            "outline" to outline,
            "outlineVariant" to outlineVariant,
            "error" to error,
            "onError" to brandOnPrimary
        )
        return if (isDark) {
            darkColorScheme(
                primary = colors["primary"]!!,
                onPrimary = colors["onPrimary"]!!,
                primaryContainer = colors["primaryContainer"]!!,
                onPrimaryContainer = colors["onPrimaryContainer"]!!,
                secondary = colors["secondary"]!!,
                onSecondary = colors["onSecondary"]!!,
                secondaryContainer = colors["secondaryContainer"]!!,
                onSecondaryContainer = colors["onSecondaryContainer"]!!,
                tertiary = colors["tertiary"]!!,
                onTertiary = colors["onTertiary"]!!,
                tertiaryContainer = colors["tertiaryContainer"]!!,
                onTertiaryContainer = colors["onTertiaryContainer"]!!,
                background = colors["background"]!!,
                onBackground = colors["onBackground"]!!,
                surface = colors["surface"]!!,
                onSurface = colors["onSurface"]!!,
                surfaceVariant = colors["surfaceVariant"]!!,
                onSurfaceVariant = colors["onSurfaceVariant"]!!,
                surfaceContainerLowest = colors["surfaceContainerLowest"]!!,
                surfaceContainerLow = colors["surfaceContainerLow"]!!,
                surfaceContainer = colors["surfaceContainer"]!!,
                surfaceContainerHigh = colors["surfaceContainerHigh"]!!,
                surfaceContainerHighest = colors["surfaceContainerHighest"]!!,
                outline = colors["outline"]!!,
                outlineVariant = colors["outlineVariant"]!!,
                error = colors["error"]!!,
                onError = colors["onError"]!!
            )
        } else {
            lightColorScheme(
                primary = colors["primary"]!!,
                onPrimary = colors["onPrimary"]!!,
                primaryContainer = colors["primaryContainer"]!!,
                onPrimaryContainer = colors["onPrimaryContainer"]!!,
                secondary = colors["secondary"]!!,
                onSecondary = colors["onSecondary"]!!,
                secondaryContainer = colors["secondaryContainer"]!!,
                onSecondaryContainer = colors["onSecondaryContainer"]!!,
                tertiary = colors["tertiary"]!!,
                onTertiary = colors["onTertiary"]!!,
                tertiaryContainer = colors["tertiaryContainer"]!!,
                onTertiaryContainer = colors["onTertiaryContainer"]!!,
                background = colors["background"]!!,
                onBackground = colors["onBackground"]!!,
                surface = colors["surface"]!!,
                onSurface = colors["onSurface"]!!,
                surfaceVariant = colors["surfaceVariant"]!!,
                onSurfaceVariant = colors["onSurfaceVariant"]!!,
                surfaceContainerLowest = colors["surfaceContainerLowest"]!!,
                surfaceContainerLow = colors["surfaceContainerLow"]!!,
                surfaceContainer = colors["surfaceContainer"]!!,
                surfaceContainerHigh = colors["surfaceContainerHigh"]!!,
                surfaceContainerHighest = colors["surfaceContainerHighest"]!!,
                outline = colors["outline"]!!,
                outlineVariant = colors["outlineVariant"]!!,
                error = colors["error"]!!,
                onError = colors["onError"]!!
            )
        }
    }
}

val LocalAppColors = staticCompositionLocalOf<AppColorSet> {
    error("AppColorSet not provided. Wrap content in SaiSmsTheme.")
}

@Composable
fun appColors(): AppColorSet = LocalAppColors.current

fun Context.createThemedContext(isDark: Boolean): Context {
    val config = Configuration(resources.configuration)
    config.uiMode = (config.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
        if (isDark) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
    return createConfigurationContext(config)
}

private fun Context.appColor(@ColorRes resId: Int): Color =
    Color(ContextCompat.getColor(this, resId))

private fun appColorSetFromContext(context: Context): AppColorSet {
    return AppColorSet(
        brandPrimary = context.appColor(R.color.brand_primary),
        brandPrimaryDark = context.appColor(R.color.brand_primary_dark),
        brandPrimaryContainer = context.appColor(R.color.brand_primary_container),
        brandOnPrimary = context.appColor(R.color.brand_on_primary),
        brandSecondary = context.appColor(R.color.brand_secondary),
        brandSecondaryDark = context.appColor(R.color.brand_secondary_dark),
        brandSecondaryContainer = context.appColor(R.color.brand_secondary_container),
        brandOnSecondary = context.appColor(R.color.brand_on_secondary),
        background = context.appColor(R.color.background),
        surface = context.appColor(R.color.surface),
        surfaceElevated = context.appColor(R.color.surface_elevated),
        surfaceVariant = context.appColor(R.color.surface_variant),
        onSurface = context.appColor(R.color.on_surface),
        onSurfaceVariant = context.appColor(R.color.on_surface_variant),
        outline = context.appColor(R.color.outline),
        outlineVariant = context.appColor(R.color.outline_variant),
        success = context.appColor(R.color.success),
        warning = context.appColor(R.color.warning),
        info = context.appColor(R.color.info),
        error = context.appColor(R.color.error),
        neutral = context.appColor(R.color.neutral),
        neutralMuted = context.appColor(R.color.neutral_muted),
        purple = context.appColor(R.color.purple),
        accentBlue = context.appColor(R.color.accent_blue),
        accentGreen = context.appColor(R.color.accent_green),
        accentPurple = context.appColor(R.color.accent_purple),
        accentCyan = context.appColor(R.color.accent_cyan),
        accentRed = context.appColor(R.color.accent_red),
        rowPaid = context.appColor(R.color.row_paid),
        rowOverdue = context.appColor(R.color.row_overdue),
        rowPending = context.appColor(R.color.row_pending),
        rowSelected = context.appColor(R.color.row_selected),
        warningContainer = context.appColor(R.color.warning_container),
        callAction = context.appColor(R.color.call_action),
        callActionContainer = context.appColor(R.color.call_action_container),
        chartLine = context.appColor(R.color.chart_line),
        chartGrid = context.appColor(R.color.chart_grid),
        chartAxis = context.appColor(R.color.chart_axis),
        chartAxisX = context.appColor(R.color.chart_axis_x),
        chartFillStart = context.appColor(R.color.chart_fill_start),
        chartUpi = context.appColor(R.color.chart_upi),
        chartCash = context.appColor(R.color.chart_cash),
        chartQr = context.appColor(R.color.chart_qr),
        chartPoint = context.appColor(R.color.chart_point),
        avatarPalettes = listOf(
            context.appColor(R.color.avatar_1_bg) to context.appColor(R.color.avatar_1_fg),
            context.appColor(R.color.avatar_2_bg) to context.appColor(R.color.avatar_2_fg),
            context.appColor(R.color.avatar_3_bg) to context.appColor(R.color.avatar_3_fg),
            context.appColor(R.color.avatar_4_bg) to context.appColor(R.color.avatar_4_fg),
            context.appColor(R.color.avatar_5_bg) to context.appColor(R.color.avatar_5_fg),
            context.appColor(R.color.avatar_6_bg) to context.appColor(R.color.avatar_6_fg)
        ),
        primaryContainerOn = context.appColor(R.color.primary_container_on),
        secondaryContainerOn = context.appColor(R.color.secondary_container_on),
        tertiaryContainer = context.appColor(R.color.tertiary_container),
        tertiaryContainerOn = context.appColor(R.color.tertiary_container_on),
        highlightContainer = context.appColor(R.color.highlight_container),
        listItemSurface = context.appColor(R.color.list_item_surface)
    )
}

@Composable
fun rememberAppColorSet(darkTheme: Boolean): AppColorSet {
    val baseContext = LocalContext.current
    return remember(baseContext, darkTheme) {
        appColorSetFromContext(baseContext.createThemedContext(darkTheme))
    }
}

@Composable
fun ProvideAppTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val appColorSet = rememberAppColorSet(darkTheme)
    CompositionLocalProvider(LocalAppColors provides appColorSet) {
        content()
    }
}

fun Color.toArgbInt(): Int = android.graphics.Color.argb(
    (alpha * 255).toInt(),
    (red * 255).toInt(),
    (green * 255).toInt(),
    (blue * 255).toInt()
)
