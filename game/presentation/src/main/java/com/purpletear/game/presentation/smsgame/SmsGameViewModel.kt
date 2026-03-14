package com.purpletear.game.presentation.smsgame

import android.util.Log
import androidx.lifecycle.ViewModel
import com.purpletear.core.presentation.extensions.executeFlowUseCase
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.model.ErrorType
import com.purpletear.sutoko.game.model.GameSessionState
import com.purpletear.sutoko.game.usecase.GetChaptersUseCase
import com.purpletear.sutoko.game.usecase.StartGameSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * ViewModel for SmsGameActivity.
 * Manages game session state and delegates to use cases.
 */
@HiltViewModel
class SmsGameViewModel @Inject constructor(
    private val startGameSession: StartGameSessionUseCase,
    private val getChapters: GetChaptersUseCase
) : ViewModel() {

    private val _sessionState = MutableStateFlow<GameSessionState>(GameSessionState.Loading)
    val sessionState: StateFlow<GameSessionState> = _sessionState.asStateFlow()

    fun initialize(gameId: String) {
        executeFlowUseCase(
            useCase = { startGameSession(gameId) },
            onStream = { state ->
                _sessionState.value = state
            },
            onFailure = { throwable ->
                _sessionState.value = GameSessionState.Error(
                    type = ErrorType.UNKNOWN,
                    message = throwable.message ?: "An unexpected error occurred"
                )
            }
        )
    }

    /**
     * Gets a specific chapter by its code for the given game.
     */
    fun getChapter(gameId: String, chapterCode: String): Flow<Chapter?> {
        Log.d("TEST", "Loading chapter $chapterCode")
        return getChapters(gameId).map { result ->
            result.getOrNull()?.find { it.code == chapterCode }
        }
    }
}
