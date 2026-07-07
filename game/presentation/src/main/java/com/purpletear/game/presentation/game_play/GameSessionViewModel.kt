package com.purpletear.game.presentation.game_play

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.core.presentation.services.MakeToastService
import com.purpletear.game.presentation.R
import com.purpletear.sutoko.game.model.GameSessionState
import com.purpletear.sutoko.game.repository.UserGameProgressRepository
import com.purpletear.sutoko.game.repository.game.GameRepository
import com.purpletear.sutoko.game.usecase.ObserveGameSessionUseCase
import com.purpletear.sutoko.game.usecase.ObserveMemoriesUseCase
import com.purpletear.sutoko.game.usecase.RestartGameUseCase
import com.purpletear.sutoko.game.usecase.SaveUserNickNameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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
    private val restartGameUseCase: RestartGameUseCase,
    private val saveUserNickNameUseCase: SaveUserNickNameUseCase,
    private val gameRepository: GameRepository,
    private val userGameProgressRepository: UserGameProgressRepository,
    private val makeToastService: MakeToastService,
) : ViewModel() {

    private val _sessionState = MutableStateFlow<GameSessionState>(GameSessionState.Loading)
    val sessionState: StateFlow<GameSessionState> = _sessionState.asStateFlow()

    private val _showRestartDialog = MutableStateFlow(false)
    val showRestartDialog: StateFlow<Boolean> = _showRestartDialog.asStateFlow()

    private val _memories = MutableStateFlow<Map<String, String>>(emptyMap())
    val memories: StateFlow<Map<String, String>> = _memories.asStateFlow()

    private val _userNickNameRequired = MutableStateFlow(false)
    val userNickNameRequired: StateFlow<Boolean> = _userNickNameRequired.asStateFlow()

    private val _heroName = MutableStateFlow("")
    val heroName: StateFlow<String> = _heroName.asStateFlow()

    private var currentGameId: String? = null
    private var sessionJob: Job? = null
    private var memoriesJob: Job? = null
    private var catalogJob: Job? = null
    private var heroNameJob: Job? = null

    fun initialize(gameId: String) {
        if (currentGameId == gameId) return
        currentGameId = gameId

        sessionJob?.cancel()
        memoriesJob?.cancel()
        catalogJob?.cancel()
        heroNameJob?.cancel()

        sessionJob = observeGameSession(gameId)
            .onEach { _sessionState.value = it }
            .launchIn(viewModelScope)

        memoriesJob = observeMemories(gameId)
            .onEach { _memories.value = it }
            .launchIn(viewModelScope)

        catalogJob = gameRepository.observeGame(gameId)
            .onEach { catalog ->
                _userNickNameRequired.value = catalog?.userNickNameRequired ?: false
            }
            .launchIn(viewModelScope)

        heroNameJob = userGameProgressRepository.observe(gameId)
            .onEach { _heroName.value = it.heroName }
            .launchIn(viewModelScope)
    }

    fun saveNickName(name: String?) {
        val gameId = currentGameId ?: return
        viewModelScope.launch {
            saveUserNickNameUseCase(gameId, name)
        }
    }

    fun onRestartPressed() {
        _showRestartDialog.value = true
    }

    fun onRestartDialogConfirm() {
        _showRestartDialog.value = false

        val gameId = currentGameId ?: return
        viewModelScope.launch {
            restartGameUseCase(gameId)
                .onSuccess {
                    makeToastService(R.string.game_restart_success)
                }
                .onFailure { error ->
                    Log.e(TAG, "Restart failed for gameId=$gameId", error)
                }
        }
    }

    fun onRestartDialogDismiss() {
        _showRestartDialog.value = false
    }

    companion object {
        private const val TAG = "GameSessionViewModel"
    }

    override fun onCleared() {
        sessionJob?.cancel()
        sessionJob = null
        memoriesJob?.cancel()
        memoriesJob = null
        catalogJob?.cancel()
        catalogJob = null
        heroNameJob?.cancel()
        heroNameJob = null
        super.onCleared()
    }
}
