package com.core2studio.mymanager.ui.components

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * Hosts a ViewModel's `errorMessage` as a snackbar and clears it once shown, so a
 * failed write is never silently swallowed.
 *
 * Usage inside a Scaffold:
 *     snackbarHost = { ErrorSnackbarHost(errorMessage = uiState.errorMessage, onDismissError = { vm.clearError() }) }
 */
@Composable
fun ErrorSnackbarHost(
    errorMessage: String?,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        val message = errorMessage ?: return@LaunchedEffect
        hostState.showSnackbar(message)
        onDismissError()
    }

    SnackbarHost(hostState = hostState, modifier = modifier)
}
