package com.saicomputer.sms.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import com.saicomputer.sms.core.ui.theme.appDimens
import com.valentinilk.shimmer.LocalShimmerTheme
import com.valentinilk.shimmer.defaultShimmerTheme
import com.valentinilk.shimmer.shimmer

@Composable
fun AppShimmerTheme(content: @Composable () -> Unit) {
    val base = MaterialTheme.colorScheme.surfaceVariant
    val highlight = MaterialTheme.colorScheme.surfaceContainerHighest
    val theme = remember(base, highlight) {
        defaultShimmerTheme.copy(
            shaderColors = listOf(base, highlight, base)
        )
    }
    CompositionLocalProvider(LocalShimmerTheme provides theme, content = content)
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = appDimens().fieldShape,
    color: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Box(
        modifier = modifier
            .shimmer()
            .background(color, shape)
    )
}

@Composable
fun ShimmerCircle(
    size: Dp,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    ShimmerBox(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = color
    )
}

@Composable
fun ThemedShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = appDimens().fieldShape,
    color: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    AppShimmerTheme {
        ShimmerBox(modifier = modifier, shape = shape, color = color)
    }
}

@Composable
fun ThemedShimmerCircle(
    size: Dp,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    AppShimmerTheme {
        ShimmerCircle(size = size, modifier = modifier, color = color)
    }
}
