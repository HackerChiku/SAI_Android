package com.saicomputer.sms.core.ui.theme

import androidx.compose.material3.Typography

/** @deprecated Typography is built from dimens.xml in [rememberAppTypography]. */
@Deprecated("Use MaterialTheme.typography from SaiSmsTheme")
val Typography: Typography
    get() = error("Typography is provided by SaiSmsTheme; use MaterialTheme.typography in composables")
