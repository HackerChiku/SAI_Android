package com.saicomputer.sms.core.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
        AppTitleBarRow(
            leading = {
                TitleBarBackButton(onBack = onBack)
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.surface
                )
            },
            actions = {
                actions()
                if (showProfile) {
                    ProfileMenuButton(user = user)
                }
            }
        )
    }
}
