package com.purpletear.game.presentation.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.core.presentation.services.MakeToastService
import com.purpletear.game.presentation.R
import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.game.usecase.RemoveGameUseCase
import com.purpletear.sutoko.game.usecase.RestartGameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for debug/dev components in SMS game.
 * Handles developer commands and tools.
 */
@HiltViewModel
class SmsGameDevViewModel @Inject constructor(
    private val restartGame: RestartGameUseCase,
    private val removeGame: RemoveGameUseCase,
    private val makeToastService: MakeToastService,
    private val userRepository: UserRepository,
) : ViewModel() {

    /**
     * Restarts the game by deleting all user progress.
     * Shows a toast on success or failure.
     */
    fun restart(gameId: String) {
        viewModelScope.launch {
            restartGame(gameId)
                .onSuccess { toast(R.string.game_restart_success) }
                .onFailure { toast(R.string.game_restart_error) }
        }
    }

    /**
     * Deletes the game by removing its installed files.
     * Calls [onComplete] on success and shows a toast on failure.
     */
    fun delete(gameId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            removeGame(gameId)
                .onSuccess {
                    onComplete()
                    toast(R.string.game_delete_success)
                }
                .onFailure { toast(R.string.game_delete_error) }
        }
    }

    /**
     * Redownloads the game files.
     * Deletes existing files and metadata, then re-downloads from scratch.
     * Shows toasts for: start, completion, and error states.
     */
    fun redownload(gameId: String) {
        viewModelScope.launch {
            observeDownloadProgress(gameId)
            startRedownload(gameId)
        }
    }

    /**
     * Observes download progress and shows toast on completion or error.
     * Runs in parallel with the download operation.
     */
    private fun observeDownloadProgress(gameId: String) = viewModelScope.launch {
        // TODO
    }

    /**
     * Triggers the redownload use case with appropriate user credentials.
     */
    private suspend fun startRedownload(gameId: String) {
        val credentials = getUserCredentials()
    }

    /**
     * Returns user credentials if connected, nulls otherwise.
     * Premium games require authentication; free games don't.
     */
    private suspend fun getUserCredentials(): UserCredentials {
        val user = userRepository.observeUser().first()
        return UserCredentials(
            userId = user?.id,
            userToken = user?.token,
            isConnected = user != null,
        )
    }

    private fun toast(messageResId: Int) {
        makeToastService(messageResId)
    }
}

private data class UserCredentials(
    val userId: String?,
    val userToken: String?,
    val isConnected: Boolean,
)
