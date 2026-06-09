package com.saicomputer.sms.core.ui.theme

import android.content.Context
import androidx.annotation.DimenRes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saicomputer.sms.R

data class AppDimenSet(
    val spacingNone: Dp,
    val spacingXxs: Dp,
    val spacingXs: Dp,
    val spacing5: Dp,
    val spacing6: Dp,
    val spacing7: Dp,
    val spacingSm: Dp,
    val spacing10: Dp,
    val spacingMd: Dp,
    val spacing14: Dp,
    val spacingLg: Dp,
    val spacingXl: Dp,
    val spacingXxl: Dp,
    val spacing28: Dp,
    val spacing32: Dp,
    val cornerRadiusExtraSmall: Dp,
    val cornerRadiusSmall: Dp,
    val cornerRadiusField: Dp,
    val cornerRadiusCard: Dp,
    val cornerRadiusStat: Dp,
    val cornerRadiusLarge: Dp,
    val cornerRadiusExtraLarge: Dp,
    val cornerRadiusPill: Dp,
    val cornerRadiusProgress: Dp,
    val iconSizeXs: Dp,
    val iconSizeSm: Dp,
    val iconSizeMd: Dp,
    val iconSizeLg: Dp,
    val iconSizeXl: Dp,
    val iconSizeXxl: Dp,
    val iconSizeListBox: Dp,
    val iconSizeListInner: Dp,
    val iconSizeListBoxLg: Dp,
    val iconDotSm: Dp,
    val iconDotMd: Dp,
    val avatarSizeList: Dp,
    val avatarSizeProfile: Dp,
    val avatarSizeMenuSm: Dp,
    val avatarSizeMenuLg: Dp,
    val avatarSizeHero: Dp,
    val bottomNavHeight: Dp,
    val bottomNavIndicatorWidth: Dp,
    val bottomNavIndicatorHeight: Dp,
    val elevationCard: Dp,
    val elevationFab: Dp,
    val strokeHairline: Dp,
    val strokeMedium: Dp,
    val strokeDashed: Dp,
    val dividerHeight: Dp,
    val fabScrollClearance: Dp,
    val chartHeightLine: Dp,
    val chartHeightPie: Dp,
    val dropdownMenuWidth: Dp,
    val loginLogoSize: Dp,
    val statCardHeight: Dp,
    val dialogMaxHeight: Dp,
    val formMinHeight: Dp,
    val columnWidthNarrow: Dp,
    val installmentIndexWidth: Dp,
    val progressBarHeight: Dp,
    val tabIndicatorHeight: Dp,
    val badgePaddingH: Dp,
    val badgePaddingV: Dp,
    val callButtonSize: Dp,
    val minTouchHeight: Dp,
    val heroStatMinHeight: Dp
) {
    val cardShape get() = RoundedCornerShape(cornerRadiusCard)
    val fieldShape get() = RoundedCornerShape(cornerRadiusField)
    val iconShape get() = RoundedCornerShape(cornerRadiusSmall)
    val pillShape get() = RoundedCornerShape(cornerRadiusPill)
    val logoShape get() = RoundedCornerShape(cornerRadiusLarge)
    val statShape get() = RoundedCornerShape(cornerRadiusStat)

    fun toMaterialShapes(): Shapes = Shapes(
        extraSmall = RoundedCornerShape(cornerRadiusExtraSmall),
        small = RoundedCornerShape(cornerRadiusSmall),
        medium = RoundedCornerShape(cornerRadiusCard),
        large = RoundedCornerShape(cornerRadiusLarge),
        extraLarge = RoundedCornerShape(cornerRadiusExtraLarge)
    )

    companion object {
        const val avatarInitialsDivisor = 2.6f
    }
}

val LocalAppDimens = staticCompositionLocalOf<AppDimenSet> {
    error("AppDimenSet not provided. Wrap content in SaiSmsTheme.")
}

@Composable
fun appDimens(): AppDimenSet = LocalAppDimens.current

private fun Context.dimenDp(@DimenRes id: Int): Dp =
    (resources.getDimension(id) / resources.displayMetrics.density).dp

private fun Context.dimenSp(@DimenRes id: Int): TextUnit =
    (resources.getDimension(id) / resources.displayMetrics.scaledDensity).sp

fun appDimenSetFromContext(context: Context): AppDimenSet = AppDimenSet(
    spacingNone = context.dimenDp(R.dimen.spacing_none),
    spacingXxs = context.dimenDp(R.dimen.spacing_xxs),
    spacingXs = context.dimenDp(R.dimen.spacing_xs),
    spacing5 = context.dimenDp(R.dimen.spacing_5),
    spacing6 = context.dimenDp(R.dimen.spacing_6),
    spacing7 = context.dimenDp(R.dimen.spacing_7),
    spacingSm = context.dimenDp(R.dimen.spacing_sm),
    spacing10 = context.dimenDp(R.dimen.spacing_10),
    spacingMd = context.dimenDp(R.dimen.spacing_md),
    spacing14 = context.dimenDp(R.dimen.spacing_14),
    spacingLg = context.dimenDp(R.dimen.spacing_lg),
    spacingXl = context.dimenDp(R.dimen.spacing_xl),
    spacingXxl = context.dimenDp(R.dimen.spacing_xxl),
    spacing28 = context.dimenDp(R.dimen.spacing_28),
    spacing32 = context.dimenDp(R.dimen.spacing_32),
    cornerRadiusExtraSmall = context.dimenDp(R.dimen.corner_radius_extra_small),
    cornerRadiusSmall = context.dimenDp(R.dimen.corner_radius_small),
    cornerRadiusField = context.dimenDp(R.dimen.corner_radius_field),
    cornerRadiusCard = context.dimenDp(R.dimen.corner_radius_card),
    cornerRadiusStat = context.dimenDp(R.dimen.corner_radius_stat),
    cornerRadiusLarge = context.dimenDp(R.dimen.corner_radius_large),
    cornerRadiusExtraLarge = context.dimenDp(R.dimen.corner_radius_extra_large),
    cornerRadiusPill = context.dimenDp(R.dimen.corner_radius_pill),
    cornerRadiusProgress = context.dimenDp(R.dimen.corner_radius_progress),
    iconSizeXs = context.dimenDp(R.dimen.icon_size_xs),
    iconSizeSm = context.dimenDp(R.dimen.icon_size_sm),
    iconSizeMd = context.dimenDp(R.dimen.icon_size_md),
    iconSizeLg = context.dimenDp(R.dimen.icon_size_lg),
    iconSizeXl = context.dimenDp(R.dimen.icon_size_xl),
    iconSizeXxl = context.dimenDp(R.dimen.icon_size_xxl),
    iconSizeListBox = context.dimenDp(R.dimen.icon_size_list_box),
    iconSizeListInner = context.dimenDp(R.dimen.icon_size_list_inner),
    iconSizeListBoxLg = context.dimenDp(R.dimen.icon_size_list_box_lg),
    iconDotSm = context.dimenDp(R.dimen.icon_dot_sm),
    iconDotMd = context.dimenDp(R.dimen.icon_dot_md),
    avatarSizeList = context.dimenDp(R.dimen.avatar_size_list),
    avatarSizeProfile = context.dimenDp(R.dimen.avatar_size_profile),
    avatarSizeMenuSm = context.dimenDp(R.dimen.avatar_size_menu_sm),
    avatarSizeMenuLg = context.dimenDp(R.dimen.avatar_size_menu_lg),
    avatarSizeHero = context.dimenDp(R.dimen.avatar_size_hero),
    bottomNavHeight = context.dimenDp(R.dimen.bottom_nav_height),
    bottomNavIndicatorWidth = context.dimenDp(R.dimen.bottom_nav_indicator_width),
    bottomNavIndicatorHeight = context.dimenDp(R.dimen.bottom_nav_indicator_height),
    elevationCard = context.dimenDp(R.dimen.elevation_card),
    elevationFab = context.dimenDp(R.dimen.elevation_fab),
    strokeHairline = context.dimenDp(R.dimen.stroke_hairline),
    strokeMedium = context.dimenDp(R.dimen.stroke_medium),
    strokeDashed = context.dimenDp(R.dimen.stroke_dashed),
    dividerHeight = context.dimenDp(R.dimen.divider_height),
    fabScrollClearance = context.dimenDp(R.dimen.fab_scroll_clearance),
    chartHeightLine = context.dimenDp(R.dimen.chart_height_line),
    chartHeightPie = context.dimenDp(R.dimen.chart_height_pie),
    dropdownMenuWidth = context.dimenDp(R.dimen.dropdown_menu_width),
    loginLogoSize = context.dimenDp(R.dimen.login_logo_size),
    statCardHeight = context.dimenDp(R.dimen.stat_card_height),
    dialogMaxHeight = context.dimenDp(R.dimen.dialog_max_height),
    formMinHeight = context.dimenDp(R.dimen.form_min_height),
    columnWidthNarrow = context.dimenDp(R.dimen.column_width_narrow),
    installmentIndexWidth = context.dimenDp(R.dimen.installment_index_width),
    progressBarHeight = context.dimenDp(R.dimen.progress_bar_height),
    tabIndicatorHeight = context.dimenDp(R.dimen.tab_indicator_height),
    badgePaddingH = context.dimenDp(R.dimen.badge_padding_h),
    badgePaddingV = context.dimenDp(R.dimen.badge_padding_v),
    callButtonSize = context.dimenDp(R.dimen.call_button_size),
    minTouchHeight = context.dimenDp(R.dimen.min_touch_height),
    heroStatMinHeight = context.dimenDp(R.dimen.hero_stat_min_height)
)

@Composable
fun rememberAppDimenSet(): AppDimenSet {
    val context = LocalContext.current
    return remember(context) { appDimenSetFromContext(context) }
}

fun typographyFromContext(context: Context): Typography {
    fun sp(@DimenRes id: Int) = context.dimenSp(id)
    val default = FontFamily.Default
    return Typography(
        headlineMedium = TextStyle(
            fontFamily = default,
            fontWeight = FontWeight.Bold,
            fontSize = sp(R.dimen.text_size_headline_medium),
            lineHeight = sp(R.dimen.text_size_headline_medium_line),
            letterSpacing = (-0.5).sp
        ),
        headlineSmall = TextStyle(
            fontFamily = default,
            fontWeight = FontWeight.Bold,
            fontSize = sp(R.dimen.text_size_headline_small),
            lineHeight = sp(R.dimen.text_size_headline_small_line),
            letterSpacing = (-0.25).sp
        ),
        titleLarge = TextStyle(
            fontFamily = default,
            fontWeight = FontWeight.SemiBold,
            fontSize = sp(R.dimen.text_size_title_large),
            lineHeight = sp(R.dimen.text_size_title_large_line),
            letterSpacing = (-0.2).sp
        ),
        titleMedium = TextStyle(
            fontFamily = default,
            fontWeight = FontWeight.SemiBold,
            fontSize = sp(R.dimen.text_size_title_medium),
            lineHeight = sp(R.dimen.text_size_title_medium_line),
            letterSpacing = 0.1.sp
        ),
        titleSmall = TextStyle(
            fontFamily = default,
            fontWeight = FontWeight.Medium,
            fontSize = sp(R.dimen.text_size_title_small),
            lineHeight = sp(R.dimen.text_size_title_small_line),
            letterSpacing = 0.1.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = default,
            fontWeight = FontWeight.Normal,
            fontSize = sp(R.dimen.text_size_body_large),
            lineHeight = sp(R.dimen.text_size_body_large_line),
            letterSpacing = 0.15.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = default,
            fontWeight = FontWeight.Normal,
            fontSize = sp(R.dimen.text_size_body_medium),
            lineHeight = sp(R.dimen.text_size_body_medium_line),
            letterSpacing = 0.2.sp
        ),
        labelLarge = TextStyle(
            fontFamily = default,
            fontWeight = FontWeight.SemiBold,
            fontSize = sp(R.dimen.text_size_label_large),
            lineHeight = sp(R.dimen.text_size_label_large_line),
            letterSpacing = 0.3.sp
        ),
        labelMedium = TextStyle(
            fontFamily = default,
            fontWeight = FontWeight.Medium,
            fontSize = sp(R.dimen.text_size_label_medium),
            lineHeight = sp(R.dimen.text_size_label_medium_line),
            letterSpacing = 0.3.sp
        ),
        labelSmall = TextStyle(
            fontFamily = default,
            fontWeight = FontWeight.Medium,
            fontSize = sp(R.dimen.text_size_label_small),
            lineHeight = sp(R.dimen.text_size_label_small_line),
            letterSpacing = 0.4.sp
        ),
        bodySmall = TextStyle(
            fontFamily = default,
            fontWeight = FontWeight.SemiBold,
            fontSize = sp(R.dimen.text_size_action),
            lineHeight = sp(R.dimen.text_size_title_small_line),
            letterSpacing = 0.2.sp
        ),
        displaySmall = TextStyle(
            fontFamily = default,
            fontWeight = FontWeight.Medium,
            fontSize = sp(R.dimen.text_size_label_tiny),
            lineHeight = sp(R.dimen.text_size_label_medium_line),
            letterSpacing = 0.3.sp
        )
    )
}

@Composable
fun rememberAppTypography(): Typography {
    val context = LocalContext.current
    return remember(context) { typographyFromContext(context) }
}

@Composable
fun ProvideAppDimens(content: @Composable () -> Unit) {
    val dimens = rememberAppDimenSet()
    CompositionLocalProvider(LocalAppDimens provides dimens, content = content)
}
