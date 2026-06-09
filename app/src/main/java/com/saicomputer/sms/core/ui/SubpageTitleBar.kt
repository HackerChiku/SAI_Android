package com.saicomputer.sms.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.saicomputer.sms.core.ui.theme.appDimens
import com.saicomputer.sms.data.model.User

@Composable
fun SubpageTitleBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    user: User? = null,
    showProfile: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {}
) {
    AppTopBarBox(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = appDimens().spacingXs, vertical = appDimens().spacingSm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(appDimens().spacingXs)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.surface
                )
            }
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.weight(1f)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(appDimens().spacingSm),
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )
            if (showProfile) {
                ProfileMenuButton(user = user)
            }
        }
    }
}
