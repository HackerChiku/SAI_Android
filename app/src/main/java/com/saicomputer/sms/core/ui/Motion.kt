package com.saicomputer.sms.core.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.saicomputer.sms.core.result.UiState

/**
 * Crossfades between the Loading / Error / Success renditions of a [UiState] so
 * screens transition smoothly instead of swapping content abruptly.
 *
 * Keyed on the state's class so re-emissions of the same variant (e.g. list
 * refreshes) don't retrigger the fade.
 */
@Composable
fun <T> CrossfadeUiState(
    state: UiState<T>,
    modifier: Modifier = Modifier,
    durationMillis: Int = 220,
    loading: @Composable () -> Unit,
    error: @Composable (String) -> Unit,
    success: @Composable (T) -> Unit
) {
    Crossfade(
        targetState = state,
        modifier = modifier,
        animationSpec = tween(durationMillis),
        label = "uiStateCrossfade"
    ) { s ->
        when (s) {
            is UiState.Loading -> loading()
            is UiState.Error -> error(s.message)
            is UiState.Success -> success(s.data)
        }
    }
}
