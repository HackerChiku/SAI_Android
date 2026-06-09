package com.saicomputer.sms.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun SaiSmsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    ProvideAppTheme(darkTheme = darkTheme) {
        ProvideAppDimens {
            val appColorSet = appColors()
            val dimens = appDimens()
            val typography = rememberAppTypography()
            MaterialTheme(
                colorScheme = appColorSet.toMaterialColorScheme(),
                typography = typography,
                shapes = dimens.toMaterialShapes(),
                content = content
            )
        }
    }
}
