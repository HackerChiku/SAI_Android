package com.saicomputer.sms.core.ui.theme

import androidx.compose.ui.graphics.Color

// ---- Brand palette ----
// Primary: blue. Secondary: black + red.

val BrandBlue = Color(0xFF0A3D91)
val BrandBlueDark = Color(0xFF06245A)
val BrandBlueTint = Color(0xFFE6EDF8)
val BrandBlueLight = Color(0xFF9DB8F0)

val BrandBlack = Color(0xFF000000)
val BrandBlackDark = Color(0xFF000000)
val BrandBlackTint = Color(0xFFE8E8E8)
val BrandBlackLight = Color(0xFF3D3D3D)

val BrandRed = Color(0xFFBE123C)
val BrandRedDark = Color(0xFF52071B)
val BrandRedTint = Color(0xFFFBE0E6)
val BrandRedLight = Color(0xFFF093A6)

/** @deprecated Gold removed from palette; maps to red secondary accent. */
val BrandGold = BrandRed
val BrandGoldDark = BrandRedDark
val BrandGoldTint = BrandRedTint
val BrandGoldLight = BrandRedLight

// ---- Neutrals ----
val BaseWhite = Color(0xFFFFFFFF)
val OffWhite = Color(0xFFF7F9FC)
val ListItemSurface = Color(0xFFEDF0F4)
val SurfaceTint = Color(0xFFF2F5FA)
val SurfaceVariantLight = Color(0xFFE6EAF1)
val OutlineLight = Color(0xFFC4CAD6)
val OutlineVariantLight = Color(0xFFDCE1EA)
val OnSurfaceLight = Color(0xFF000000)
val OnSurfaceVariantLightColor = Color(0xFF424242)

// ---- Dark surfaces ----
val SurfaceDark = Color(0xFF0A0A0A)
val SurfaceDarkElevated = Color(0xFF141414)
val SurfaceVariantDark = Color(0xFF1F1F1F)
val OutlineDark = Color(0xFF404040)
val OutlineVariantDark = Color(0xFF2A2A2A)
val OnSurfaceDark = Color(0xFFF5F5F5)
val OnSurfaceVariantDarkColor = Color(0xFFB0B0B0)

// ---- Status / semantic (blue primary, black muted, red accent) ----
val StatusBlue = BrandBlue
val StatusEmerald = BrandBlue
val StatusAmber = BrandRed
val StatusGray = BrandBlackLight
val StatusRed = BrandRed
val StatusZinc = BrandBlackLight
val StatusPurple = BrandBlue
