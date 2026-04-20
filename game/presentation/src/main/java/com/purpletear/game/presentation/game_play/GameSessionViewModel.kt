package com.purpletear.game.presentation.game_play

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.sutoko.game.model.GameSessionState
import com.purpletear.sutoko.game.usecase.GetChaptersUseCase
import com.purpletear.sutoko.game.usecase.ObserveGameSessionUseCase
import com.purpletear.sutoko.game.usecase.ObserveMemoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel for game session management.
 * Manages game session state reactively using ObserveGameSessionUseCase.
 * Automatically updates when user progress changes (e.g., chapter advancement).
 */
@HiltViewModel
class GameSessionViewModel @Inject constructor(
    private val observeGameSession: ObserveGameSessionUseCase,
    private val getChapters: GetChaptersUseCase,
    private val observeMemories: ObserveMemoriesUseCase,
) : ViewModel() {

    private val _sessionState = MutableStateFlow<GameSessionState>(GameSessionState.Loading)
    val sessionState: StateFlow<GameSessionState> = _sessionState.asStateFlow()

    private val _showRestartDialog = mutableStateOf(false)
    val showRestartDialog: State<Boolean> get() = _showRestartDialog

    private var currentGameId: String? = null
    private var memoriesFlow: StateFlow<Map<String, String>>? = null

    fun initialize(gameId: String) {
        if (currentGameId == gameId) return
        currentGameId = gameId

        observeGameSession(gameId)
            .onEach { state ->
                _sessionState.value = state
            }
            .launchIn(viewModelScope)

        memoriesFlow = observeMemories(gameId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyMap()
            )
    }

    fun getMemories(): StateFlow<Map<String, String>> {
        return memoriesFlow ?: MutableStateFlow(emptyMap())
    }

    fun currentChapterCode(): String? {
        return (_sessionState.value as? GameSessionState.Ready)
            ?.chapter?.normalizedCode
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
}
