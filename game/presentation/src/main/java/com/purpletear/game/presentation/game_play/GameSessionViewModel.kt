package com.purpletear.game.presentation.game_play

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.sutoko.game.model.GameSessionState
import com.purpletear.sutoko.game.usecase.ObserveGameSessionUseCase
import com.purpletear.sutoko.game.usecase.ObserveMemoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * ViewModel for game session management.
 * Manages game session state reactively using ObserveGameSessionUseCase.
 * Automatically updates when user progress changes (e.g., chapter advancement).
 */
@HiltViewModel
class GameSessionViewModel @Inject constructor(
    private val observeGameSession: ObserveGameSessionUseCase,
    private val observeMemories: ObserveMemoriesUseCase,
) : ViewModel() {

    private val _sessionState = MutableStateFlow<GameSessionState>(GameSessionState.Loading)
    val sessionState: StateFlow<GameSessionState> = _sessionState.asStateFlow()

    private val _showRestartDialog = MutableStateFlow(false)
    val showRestartDialog: StateFlow<Boolean> = _showRestartDialog.asStateFlow()

    private val _memories = MutableStateFlow<Map<String, String>>(emptyMap())
    val memories: StateFlow<Map<String, String>> = _memories.asStateFlow()

    private var currentGameId: String? = null
    private var sessionJob: Job? = null
    private var memoriesJob: Job? = null

    fun initialize(gameId: String) {
        if (currentGameId == gameId) return
        currentGameId = gameId

        sessionJob?.cancel()
        memoriesJob?.cancel()

        sessionJob = observeGameSession(gameId)
            .onEach { _sessionState.value = it }
            .launchIn(viewModelScope)

        memoriesJob = observeMemories(gameId)
            .onEach { _memories.value = it }
            .launchIn(viewModelScope)
    }

    fun onRestartPressed() {
        _showRestartDialog.value = true
    }

    fun onRestartDialogConfirm() {
        _showRestartDialog.value = false
        // TODO: perform actual restart
    }

    fun onRestartDialogDismiss() {
        _showRestartDialog.value = false
    }

    override fun onCleared() {
        sessionJob?.cancel()
        sessionJob = null
        memoriesJob?.cancel()
        memoriesJob = null
        super.onCleared()
    }
}
