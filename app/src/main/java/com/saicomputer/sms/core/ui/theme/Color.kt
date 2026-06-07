package com.saicomputer.sms.core.ui.theme

import androidx.compose.ui.graphics.Color

// ---- Brand palette (Academic Precision) ----
// Primary: deep blue. Secondary: crimson. Tertiary: gold. Neutral near-black.
val BrandBlue = Color(0xFF0A3D91)        // primary
val BrandBlueDark = Color(0xFF06245A)    // on-container / pressed
val BrandBlueTint = Color(0xFFE6EDF8)    // light primary container
val BrandBlueLight = Color(0xFF9DB8F0)   // dark-mode primary

val BrandRed = Color(0xFFBE123C)         // secondary (crimson)
val BrandRedDark = Color(0xFF52071B)     // on-container / pressed
val BrandRedTint = Color(0xFFFBE0E6)     // light secondary container
val BrandRedLight = Color(0xFFF093A6)    // dark-mode secondary

val BrandGold = Color(0xFFEAB308)        // tertiary (gold)
val BrandGoldDark = Color(0xFF5C4400)    // on-container / pressed
val BrandGoldTint = Color(0xFFFCF3D4)    // light tertiary container
val BrandGoldLight = Color(0xFFF3D261)   // dark-mode tertiary

// ---- Neutrals (white-base, near-black ink) ----
val BaseWhite = Color(0xFFFFFFFF)
val OffWhite = Color(0xFFF7F9FC)         // soft card surface
val SurfaceTint = Color(0xFFF2F5FA)      // app background
val SurfaceVariantLight = Color(0xFFE6EAF1)
val OutlineLight = Color(0xFFC4CAD6)
val OutlineVariantLight = Color(0xFFDCE1EA)
val OnSurfaceLight = Color(0xFF020617)
val OnSurfaceVariantLightColor = Color(0xFF4A5163)

// ---- Dark surfaces (neutral #020617 base) ----
val SurfaceDark = Color(0xFF020617)
val SurfaceDarkElevated = Color(0xFF0C1326)
val SurfaceVariantDark = Color(0xFF1A2236)
val OutlineDark = Color(0xFF334155)
val OutlineVariantDark = Color(0xFF1E293B)
val OnSurfaceDark = Color(0xFFE6EAF2)
val OnSurfaceVariantDarkColor = Color(0xFFAAB3C5)

// ---- Status / semantic colors ----
// Brand-only: blue (primary/positive), gold (attention), crimson (negative),
// neutral ink (muted). No grey, no colors outside the brand palette.
val StatusBlue = BrandBlue
val StatusEmerald = BrandBlue   // success / active -> primary blue
val StatusAmber = BrandGold     // warning / pending -> gold
val StatusGray = OnSurfaceLight // muted -> brand neutral ink
val StatusRed = BrandRed        // error / negative -> crimson
val StatusZinc = OnSurfaceLight // muted -> brand neutral ink
val StatusPurple = BrandBlue    // -> primary blue
