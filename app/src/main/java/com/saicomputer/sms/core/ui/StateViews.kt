package com.saicomputer.sms.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.saicomputer.sms.core.ui.theme.appDimens

@Composable
fun LoadingSkeleton(
    modifier: Modifier = Modifier,
    rows: Int = 6
) {
    AppShimmerTheme {
        Column(
            modifier = modifier.padding(appDimens().spacingLg),
            verticalArrangement = Arrangement.spacedBy(appDimens().spacingMd)
        ) {
            repeat(rows) {
                ShimmerListRow()
            }
        }
    }
}

@Composable
fun ShimmerListRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = appDimens().spacingXs),
        horizontalArrangement = Arrangement.spacedBy(appDimens().spacingMd),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ShimmerCircle(size = appDimens().avatarSizeList)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(appDimens().spacingSm)
        ) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(appDimens().spacing14)
            )
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(appDimens().spacingMd)
            )
        }
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(0.18f)
                .height(appDimens().spacingLg)
        )
    }
}

@Composable
fun ShimmerPagingRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(appDimens().spacingLg),
        horizontalArrangement = Arrangement.Center
    ) {
        ShimmerListRow(modifier = Modifier.fillMaxWidth(0.92f))
    }
}

@Composable
fun FormLoadingSkeleton(
    modifier: Modifier = Modifier,
    fields: Int = 5
) {
    AppShimmerTheme {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(appDimens().spacingLg),
            verticalArrangement = Arrangement.spacedBy(appDimens().spacingLg)
        ) {
            repeat(fields) {
                Column(verticalArrangement = Arrangement.spacedBy(appDimens().spacingSm)) {
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth(0.35f)
                            .height(appDimens().spacingMd)
                    )
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(appDimens().minTouchHeight),
                        shape = appDimens().fieldShape
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardLoadingSkeleton(modifier: Modifier = Modifier) {
    AppShimmerTheme {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(appDimens().spacingLg),
            verticalArrangement = Arrangement.spacedBy(appDimens().spacingLg)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(appDimens().spacingSm)
            ) {
                repeat(2) {
                    ShimmerBox(
                        modifier = Modifier
                            .weight(1f)
                            .height(appDimens().statCardHeight),
                        shape = appDimens().statShape
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(appDimens().spacingSm)
            ) {
                repeat(2) {
                    ShimmerBox(
                        modifier = Modifier
                            .weight(1f)
                            .height(appDimens().statCardHeight),
                        shape = appDimens().statShape
                    )
                }
            }
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(appDimens().chartHeightLine),
                shape = appDimens().cardShape
            )
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(appDimens().chartHeightPie),
                shape = appDimens().cardShape
            )
            repeat(3) {
                ShimmerListRow()
            }
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    description: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(appDimens().spacing32),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.Inbox,
            contentDescription = null,
            modifier = Modifier.height(appDimens().iconSizeListBox),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(Modifier.height(appDimens().spacingMd))
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        if (description != null) {
            Spacer(Modifier.height(appDimens().spacing6))
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(appDimens().spacingLg))
            Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(appDimens().spacing32),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.WifiOff,
            contentDescription = null,
            modifier = Modifier.height(appDimens().iconSizeListBox),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(appDimens().spacingMd))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (onRetry != null) {
            Spacer(Modifier.height(appDimens().spacingLg))
            OutlinedButton(onClick = onRetry) { Text("Retry") }
        }
    }
}
