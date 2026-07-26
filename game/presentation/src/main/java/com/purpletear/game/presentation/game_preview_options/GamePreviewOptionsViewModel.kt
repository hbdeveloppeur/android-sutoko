package com.purpletear.game.presentation.game_preview_options

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.core.presentation.services.ToastService
import com.purpletear.game.presentation.R
import com.purpletear.sutoko.core.domain.logger.Logger
import com.purpletear.sutoko.core.domain.logger.exception
import com.purpletear.sutoko.game.model.FriendzonedLegacyIds
import com.purpletear.sutoko.game.model.UserRole
import com.purpletear.sutoko.game.repository.ChapterRepository
import com.purpletear.sutoko.game.repository.MemoryRepository
import com.purpletear.sutoko.game.repository.UserRoleRepository
import com.purpletear.sutoko.game.repository.game.GameRepository
import com.purpletear.sutoko.game.usecase.RestartGameUseCase
import com.purpletear.sutoko.game.usecase.SelectChapterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GamePreviewOptionsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chapterRepository: ChapterRepository,
    private val gameRepository: GameRepository,
    private val selectChapterUseCase: SelectChapterUseCase,
    private val restartGameUseCase: RestartGameUseCase,
    private val memoryRepository: MemoryRepository,
    private val userRoleRepository: UserRoleRepository,
    private val toastService: ToastService,
    private val logger: Logger,
) : ViewModel() {

    private val gameId: String =
        checkNotNull(savedStateHandle["gameId"]) { "gameId required in SavedStateHandle" }

    val role: StateFlow<UserRole> = userRoleRepository.observe()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(7000),
            initialValue = UserRole.PLAYER,
        )

    /** Code of the current chapter, used to prefill the input. */
    val currentChapterCode: StateFlow<String> = chapterRepository.observeCurrentChapter(gameId)
        .map { it?.code.orEmpty() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(7000),
            initialValue = "",
        )

    /** Null until the catalog row is loaded. */
    private val legacyId: StateFlow<Int?> = gameRepository.observeGame(gameId)
        .map { it?.legacyId }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(7000),
            initialValue = null,
        )

    /**
     * Friendzoned games manage their own chapter progress: switching chapters
     * from here would write a store they never read, so the section is hidden.
     */
    val isFriendzoned: StateFlow<Boolean> = legacyId
        .map { FriendzonedLegacyIds.isFriendzoned(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(7000),
            initialValue = false,
        )

    private var actionJob: Job? = null

    /**
     * Sets the current chapter to [rawCode] when a locally stored chapter matches it.
     * Unknown codes are rejected with a toast; progress is left untouched.
     */
    fun onChapterCodeSubmitted(rawCode: String) {
        val code = rawCode.trim()
        if (code.isEmpty() || actionJob?.isActive == true) return
        actionJob = viewModelScope.launch {
            val chapter = chapterRepository.observeChapters(gameId).first()
                .firstOrNull { it.normalizedCode == code.lowercase() }
            if (chapter == null) {
                toastService(R.string.game_presentation_options_chapter_unknown)
                return@launch
            }
            selectChapterUseCase(gameId, chapter.code)
                .onSuccess { toastService(R.string.game_presentation_game_chapter_selection_success) }
                .onFailure { error ->
                    logger.exception(error) { "Failed to set chapter ${chapter.code} for gameId=$gameId" }
                    toastService(R.string.game_presentation_game_chapters_select_error)
                }
        }
    }

    fun onRoleSelected(role: UserRole) {
        viewModelScope.launch {
            userRoleRepository.set(role)
        }
    }

    fun onRestartConfirmed() {
        if (actionJob?.isActive == true) return
        actionJob = viewModelScope.launch {
            restartGameUseCase(gameId, legacyId = legacyId.value)
                .onSuccess { toastService(R.string.game_presentation_game_restart_success) }
                .onFailure { error ->
                    logger.exception(error) { "Restart failed for gameId=$gameId" }
                    toastService(R.string.game_presentation_game_restart_error)
                }
        }
    }

    fun onDeleteMemoriesConfirmed() {
        if (actionJob?.isActive == true) return
        actionJob = viewModelScope.launch {
            try {
                memoryRepository.delete(gameId)
                toastService(R.string.game_presentation_options_delete_memories_success)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.exception(e) { "Delete memories failed for gameId=$gameId" }
                toastService(R.string.game_presentation_options_delete_memories_error)
            }
        }
    }
}
