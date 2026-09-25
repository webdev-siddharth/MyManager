package com.core2studio.mymanager.utils

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

/**
 * Debounced draft auto-save.
 *
 * The collector runs in a scope that outlives the ViewModel (so a save can be
 * flushed from ViewModel.onCleared() rather than being cancelled with it), and
 * [flushAndCancel] stops it so cleared ViewModels don't leave collectors behind.
 */
@OptIn(kotlinx.coroutines.FlowPreview::class)
class DraftAutoSaveManager<T>(
    private val saveAction: suspend (T) -> Unit,
    private val scope: CoroutineScope,
    private val debounceMs: Long = 0
) {
    private val _pendingSave = MutableStateFlow<T?>(null)
    val pendingSave: StateFlow<T?> = _pendingSave

    private val collectJob: Job = scope.launch {
        val source = _pendingSave.filterNotNull().distinctUntilChanged()
        val timed = if (debounceMs > 0) source.debounce(debounceMs) else source
        timed.collect { state ->
            try {
                saveAction(state)
            } catch (e: Exception) {
                Log.e(TAG, "Draft auto-save failed", e)
            }
        }
    }

    fun scheduleSave(state: T) {
        _pendingSave.value = state
    }

    /** Drops anything not yet written — call after submit/discard so a queued save can't resurrect the draft. */
    fun clearPending() {
        _pendingSave.value = null
    }

    /** Persists the final state (if any) and stops collecting. Safe to call from ViewModel.onCleared(). */
    fun flushAndCancel(finalState: T?) {
        collectJob.cancel()
        _pendingSave.value = null
        if (finalState != null) {
            scope.launch {
                try {
                    saveAction(finalState)
                } catch (e: Exception) {
                    Log.e(TAG, "Draft auto-save flush failed", e)
                }
            }
        }
    }

    companion object {
        private const val TAG = "DraftAutoSaveManager"
    }
}
