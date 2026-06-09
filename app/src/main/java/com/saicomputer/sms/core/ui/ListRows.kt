package com.saicomputer.sms.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.saicomputer.sms.core.ui.theme.appColors
import com.saicomputer.sms.data.model.EmailStatus
import com.saicomputer.sms.core.ui.theme.appDimens

@Composable
fun ListItemCard(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = appColors().listItemSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = appDimens().strokeHairline)
    ) {
        content()
    }
}

@Composable
fun ListItemIconBox(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer
) {
    Box(
        modifier = modifier
            .size(appDimens().iconSizeListBoxLg)
            .clip(appDimens().cardShape)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(appDimens().iconSizeXl)
        )
    }
}

@Composable
fun emailStatusColor(status: EmailStatus): Color {
    val colors = appColors()
    return when (status) {
        EmailStatus.Sent -> colors.success
        EmailStatus.Queued -> colors.warning
        EmailStatus.Failed -> colors.error
        EmailStatus.NotSent -> MaterialTheme.colorScheme.primary
        EmailStatus.NotApplicable -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}
