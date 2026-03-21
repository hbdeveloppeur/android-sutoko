package com.purpletear.game.presentation.smsgame

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.model.GameSessionState
import com.purpletear.sutoko.game.usecase.GetChaptersUseCase
import com.purpletear.sutoko.game.usecase.ObserveGameSessionUseCase
import com.purpletear.sutoko.game.usecase.ObserveMemoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel for SmsGameActivity.
 * Manages game session state reactively using ObserveGameSessionUseCase.
 * Automatically updates when user progress changes (e.g., chapter advancement).
 */
@HiltViewModel
class SmsGameViewModel @Inject constructor(
    private val observeGameSession: ObserveGameSessionUseCase,
    private val getChapters: GetChaptersUseCase,
    private val observeMemories: ObserveMemoriesUseCase,
) : ViewModel() {

    private val _sessionState = MutableStateFlow<GameSessionState>(GameSessionState.Loading)
    val sessionState: StateFlow<GameSessionState> = _sessionState.asStateFlow()

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
                started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
                initialValue = emptyMap()
            )
    }

    fun getMemories(): StateFlow<Map<String, String>> {
        return memoriesFlow ?: MutableStateFlow(emptyMap())
    }

    /**
     * Gets a specific chapter by its code for the given game.
     */
    fun getChapter(gameId: String, chapterCode: String): Flow<Chapter?> {
        Log.d("TEST", "Loading chapter $chapterCode")
        return getChapters(gameId).map { result ->
            result.getOrNull()?.find { it.normalizedCode == chapterCode.lowercase() }
        }
    }
}
