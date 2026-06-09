package com.saicomputer.sms.core.ui.theme

import androidx.compose.material3.Shapes

/** @deprecated Use [appDimens]().toMaterialShapes() via MaterialTheme.shapes instead. */
@Deprecated("Use MaterialTheme.shapes from SaiSmsTheme")
val AppShapes: Shapes
    get() = error("AppShapes is provided by SaiSmsTheme; use MaterialTheme.shapes in composables")
