package com.saicomputer.sms.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/** Edge-to-edge top bar: background fills the status bar area; content sits below it. */
@Composable
fun AppTopBarBox(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
            content = content
        )
    }
}
