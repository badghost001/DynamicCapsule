package com.dynamiccapsule.app.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class CapsuleKind { IDLE, TIMER, MEDIA, CALL }

data class CapsuleState(
    val kind: CapsuleKind = CapsuleKind.IDLE,
    val title: String = "Ready when you are",
    val subtitle: String = "No live activity",
    val progress: Float? = null,
    val isPlaying: Boolean = false,
    val expanded: Boolean = false,
)

object CapsuleStore {
    private val _state = MutableStateFlow(CapsuleState())
    val state: StateFlow<CapsuleState> = _state.asStateFlow()

    fun set(state: CapsuleState) { _state.value = state }
    fun toggleExpanded() { _state.value = _state.value.copy(expanded = !_state.value.expanded) }
}
