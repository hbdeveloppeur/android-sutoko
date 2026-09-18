package com.purpletear.game.presentation.game_chapters

import androidx.annotation.Keep
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.core.presentation.services.ToastService
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.model.GameUiError
import com.purpletear.sutoko.core.domain.logger.Logger
import com.purpletear.sutoko.core.domain.logger.exception
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.model.UserRole
import com.purpletear.sutoko.game.repository.ChapterRepository
import com.purpletear.sutoko.game.repository.UserRoleRepository
import com.purpletear.sutoko.game.repository.game.GameRepository
import com.purpletear.sutoko.game.service.MediaUrlResolver
import com.purpletear.sutoko.game.usecase.GetChaptersUseCase
import com.purpletear.sutoko.game.usecase.PrepareGameLaunchUseCase
import com.purpletear.sutoko.game.usecase.SaveUserNickNameUseCase
import com.purpletear.sutoko.game.usecase.SelectChapterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ChaptersUiState {
    data object Loading : ChaptersUiState

    @Keep
    data class Data(
        val chapters: List<Chapter>,
        /** Normalized code of the chapter the user is currently playing, null when never started. */
        val currentChapterCode: String?,
        val backgroundUrl: String?,
        /** Administrators can see and select chapters that are not released yet. */
        val isAdmin: Boolean = false,
    ) : ChaptersUiState

    data object Error : ChaptersUiState
}

sealed interface ChaptersEvent {
    /** The chapter was selected and persisted: the game can be launched at this chapter. */
    data class OpenChapter(val chapterCode: String) : ChaptersEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChaptersViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getChaptersUseCase: GetChaptersUseCase,
    private val gameRepository: GameRepository,
    mediaUrlResolver: MediaUrlResolver,
    chapterRepository: ChapterRepository,
    private val selectChapterUseCase: SelectChapterUseCase,
    private val prepareGameLaunchUseCase: PrepareGameLaunchUseCase,
    private val saveUserNickNameUseCase: SaveUserNickNameUseCase,
    private val userRoleRepository: UserRoleRepository,
    private val toastService: ToastService,
    private val logger: Logger,
) : ViewModel() {

    private val gameId: String =
        checkNotNull(savedStateHandle["gameId"]) { "gameId required in SavedStateHandle" }

    /** Incremented by [refresh] to restart the chapters fetch. */
    private val chaptersRefresh = MutableStateFlow(0)

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val uiState: StateFlow<ChaptersUiState> = combine(
        chaptersRefresh.flatMapLatest { tick ->
            getChaptersUseCase(gameId).onCompletion { cause ->
                // Only a normal completion of the latest fetch ends the refresh;
                // a cancelled (superseded) fetch must not clear the flag.
                if (cause == null && tick == chaptersRefresh.value) {
                    _isRefreshing.value = false
                }
            }
        },
        gameRepository.observeGame(gameId),
        chapterRepository.observeCurrentChapter(gameId),
        userRoleRepository.observe(),
    ) { chaptersResult, catalog, currentChapter, role ->
        chaptersResult.fold(
            onSuccess = { chapters ->
                ChaptersUiState.Data(
                    chapters = chapters.sortedBy { it.number },
                    currentChapterCode = currentChapter?.normalizedCode,
                    backgroundUrl = mediaUrlResolver.resolveBannerUrl(catalog?.menuBackground?.storagePath),
                    isAdmin = role == UserRole.ADMINISTRATOR,
                )
            },
            onFailure = { error ->
                logger.exception(error) { "Failed to load chapters for gameId=$gameId" }
                ChaptersUiState.Error
            },
        )
    }.catch { error ->
        logger.exception(error) { "Chapters observation failed for gameId=$gameId" }
        emit(ChaptersUiState.Error)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(7000),
        initialValue = ChaptersUiState.Loading,
    )

    private val _events = Channel<ChaptersEvent>(Channel.BUFFERED)
    val events: Flow<ChaptersEvent> = _events.receiveAsFlow()

    private val _isSelecting = MutableStateFlow(false)
    val isSelecting: StateFlow<Boolean> = _isSelecting.asStateFlow()

    private val _nickNameChapter = MutableStateFlow<Chapter?>(null)
    val nickNameChapter: StateFlow<Chapter?> = _nickNameChapter.asStateFlow()

    private val _isSavingNickName = MutableStateFlow(false)
    val isSavingNickName: StateFlow<Boolean> = _isSavingNickName.asStateFlow()

    private var navigationPending = false

    fun onResume() {
        navigationPending = false
    }

    /**
     * Re-fetches this story's chapters from the network; the fresh result flows
     * into [uiState] automatically. Concurrent refreshes are ignored.
     */
    fun refresh() {
        if (_isRefreshing.value) return
        _isRefreshing.value = true
        chaptersRefresh.value += 1
    }

    /**
     * Moves the user's progress to [chapter] then asks the screen to open the game there.
     * Locked chapters (unless administrator) and concurrent selections are ignored.
     */
    fun onChapterSelected(chapter: Chapter) {
        val isAdmin = (uiState.value as? ChaptersUiState.Data)?.isAdmin == true
        if (!chapter.available && !isAdmin) return
        if (navigationPending || _nickNameChapter.value != null ||
            !_isSelecting.compareAndSet(false, true)
        ) return
        viewModelScope.launch {
            try {
                prepareAndOpenChapter(chapter, requestNickName = true)
            } finally {
                _isSelecting.value = false
            }
        }
    }

    fun onNickNameDismissed() {
        if (!_isSavingNickName.value) _nickNameChapter.value = null
    }

    fun onNickNameConfirmed(name: String?) {
        val chapter = _nickNameChapter.value ?: return
        if (navigationPending || !_isSavingNickName.compareAndSet(false, true)) return
        viewModelScope.launch {
            try {
                saveUserNickNameUseCase(gameId, name).getOrElse { error ->
                    showNickNameError(error)
                    return@launch
                }
                prepareAndOpenChapter(chapter, requestNickName = false)
            } finally {
                _isSavingNickName.value = false
            }
        }
    }

    private suspend fun prepareAndOpenChapter(chapter: Chapter, requestNickName: Boolean) {
        try {
            if (!chapter.available && userRoleRepository.get() != UserRole.ADMINISTRATOR) return
            val catalog = gameRepository.observeGame(gameId).firstOrNull()
                ?: error("Game not found: $gameId")
            val needsNickName = prepareGameLaunchUseCase(catalog, requestNickName)
                .getOrElse { error ->
                    showNickNameError(error)
                    return
                }
            if (needsNickName) {
                _nickNameChapter.value = chapter
                return
            }
            selectChapterUseCase(gameId, chapter.code).getOrThrow()
            _nickNameChapter.value = null
            navigationPending = true
            _events.send(ChaptersEvent.OpenChapter(chapter.normalizedCode))
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            logger.exception(error) { "Failed to select chapter ${chapter.code} for gameId=$gameId" }
            toastService(R.string.game_presentation_game_chapters_select_error)
        }
    }

    private fun showNickNameError(error: Throwable) {
        logger.exception(error) { "Could not prepare nickname for gameId=$gameId" }
        toastService(GameUiError.NickName.stringRes)
    }
}
