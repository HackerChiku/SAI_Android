package com.saicomputer.sms.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.saicomputer.sms.core.ui.theme.appDimens

@Composable
fun AppTitleBarRow(
    modifier: Modifier = Modifier,
    leading: @Composable RowScope.() -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = appDimens().iconSizeMd, vertical = appDimens().spacingLg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(appDimens().spacingSm),
            content = leading
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(appDimens().spacingSm),
            verticalAlignment = Alignment.CenterVertically,
            content = actions
        )
    }
}

@Composable
fun TitleBarBackButton(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(appDimens().iconSizeXxl)
            .clip(CircleShape)
            .clickable(onClick = onBack),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = "Back",
            tint = MaterialTheme.colorScheme.surface,
            modifier = Modifier.size(appDimens().iconSizeListInner)
        )
    }
}
