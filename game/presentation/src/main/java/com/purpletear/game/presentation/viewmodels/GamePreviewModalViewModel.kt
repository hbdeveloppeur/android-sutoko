package com.purpletear.game.presentation.viewmodels

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharedelements.utils.UiText
import com.purpletear.core.presentation.extensions.awaitFlowResult
import com.purpletear.core.presentation.extensions.executeFlowUseCase
import com.purpletear.core.presentation.services.MakeToastService
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.states.GameButtonsState
import com.purpletear.game.presentation.states.GameState
import com.purpletear.game.presentation.states.StoryPreviewAction
import com.purpletear.game.presentation.states.toButtonsState
import com.purpletear.ntfy.Ntfy
import com.purpletear.sutoko.game.download.GameDownloadManager
import com.purpletear.sutoko.game.download.GameDownloadState
import com.purpletear.sutoko.game.exception.GameDownloadForbiddenException
import com.purpletear.sutoko.game.exception.UserNotConnectedException
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.model.Game
import com.purpletear.sutoko.game.model.isPremium
import com.purpletear.sutoko.game.usecase.GetChaptersUseCase
import com.purpletear.sutoko.game.usecase.GetCurrentChapterUseCase
import com.purpletear.sutoko.game.usecase.GetGameUseCase
import com.purpletear.sutoko.game.usecase.HasGameLocalFilesUseCase
import com.purpletear.sutoko.game.usecase.IsGameUpdatableUseCase
import com.purpletear.sutoko.game.usecase.RemoveGameUseCase
import com.purpletear.sutoko.game.usecase.SetGameVersionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.purpletear.sutoko.popup.domain.PopUpIconAnimation
import fr.purpletear.sutoko.popup.domain.PopUpUserInteraction
import fr.purpletear.sutoko.popup.domain.SutokoPopUp
import fr.purpletear.sutoko.popup.domain.usecase.GetPopUpInteractionUseCase
import fr.purpletear.sutoko.popup.domain.usecase.ShowPopUpUseCase
import fr.purpletear.sutoko.shop.coinsLogic.Customer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject

/**
 * ViewModel for the GamePreviewModal component.
 * Manages download state and game actions for a specific game in a modal context.
 * This ViewModel is scoped to the modal's lifecycle.
 */
@HiltViewModel
class GamePreviewModalViewModel @Inject constructor(
    private val getGameUseCase: GetGameUseCase,
    private val getChaptersUseCase: GetChaptersUseCase,
    private val gameDownloadManager: GameDownloadManager,
    private val hasGameLocalFilesUseCase: HasGameLocalFilesUseCase,
    private val isGameUpdatableUseCase: IsGameUpdatableUseCase,
    private val getCurrentChapterUseCase: GetCurrentChapterUseCase,
    private val setGameVersionUseCase: SetGameVersionUseCase,
    private val removeGameUseCase: RemoveGameUseCase,
    private val showPopUpUseCase: ShowPopUpUseCase,
    private val observeInteractionUseCase: GetPopUpInteractionUseCase,
    private val makeToastService: MakeToastService,
    private val customer: Customer,
    private val ntfy: Ntfy,
) : ViewModel() {

    private var gameId: String? = null

    // Mutex to prevent concurrent chapter fetching during download (race condition protection)
    private val chaptersDownloadMutex = Mutex()

    private val _game = mutableStateOf<Game?>(null)
    val game: State<Game?> = _game

    private val _gameState = mutableStateOf<GameState>(GameState.Loading)
    val gameState: State<GameState> = _gameState

    private val _currentChapter = mutableStateOf<Chapter?>(null)
    val currentChapter: State<Chapter?> = _currentChapter

    private val _currentChapterNumber = mutableIntStateOf(1)
    val currentChapterNumber: State<Int> = _currentChapterNumber

    // Events for navigation/actions
    private val _playGameEvents = MutableSharedFlow<String>() // emits gameId
    val playGameEvents: SharedFlow<String> = _playGameEvents

    private val _dismissEvents = MutableSharedFlow<Unit>()
    val dismissEvents: SharedFlow<Unit> = _dismissEvents

    private val _gameDeletedEvents = MutableSharedFlow<Unit>()
    val gameDeletedEvents: SharedFlow<Unit> = _gameDeletedEvents

    private val _openStoreEvents = MutableSharedFlow<Unit>()
    val openStoreEvents: SharedFlow<Unit> = _openStoreEvents

    // UI-specific state for buttons
    internal val gameButtonsState: GameButtonsState
        get() = _gameState.value.toButtonsState(
            currentChapterNumber = _currentChapterNumber.intValue,
            gamePrice = _game.value?.price,
            onAction = ::onAction
        )

    /**
     * Initialize the ViewModel with a game ID.
     * Fetches the game from repository and determines initial state.
     *
     * @param gameId The ID of the game to load
     */
    fun init(gameId: String) {
        if (this.gameId == gameId) {
            // Already initialized with same game, just refresh state
            _game.value?.let { game ->
                viewModelScope.launch { determineGameState(game) }
            }
            return
        }

        this.gameId = gameId
        loadGame(gameId)
    }

    /**
     * Handles user actions from the UI.
     */
    fun onAction(action: StoryPreviewAction) {
        when (action) {
            StoryPreviewAction.OnPlay -> {
                gameId?.let { id ->
                    viewModelScope.launch { _playGameEvents.emit(id) }
                }
            }

            StoryPreviewAction.OnDownload -> {
                startDownload()
            }

            StoryPreviewAction.OnReload -> {
                gameId?.let { loadGame(it) }
            }

            StoryPreviewAction.OnUpdateGame -> {
                startDownload()
            }

            StoryPreviewAction.OnAbortBuy,
            StoryPreviewAction.OnBuy,
            StoryPreviewAction.OnBuyConfirm -> {
                Log.d(TAG, "Buy action triggered - emitting dismissEvents")
                viewModelScope.launch { _dismissEvents.emit(Unit) }
            }

            StoryPreviewAction.OnRestart -> {
                // TODO:
            }
            
            StoryPreviewAction.OnUpdateApp -> {
                viewModelScope.launch { _openStoreEvents.emit(Unit) }
            }

            StoryPreviewAction.OnDelete -> {
                confirmAndDeleteGame()
            }
        }
    }

    /**
     * Shows a confirmation popup and deletes the game if confirmed.
     */
    private fun confirmAndDeleteGame() {
        Log.d(TAG, "confirmAndDeleteGame() called")
        val popUp = SutokoPopUp(
            title = UiText.StringResource(R.string.game_delete_confirm_title),
            description = UiText.StringResource(R.string.game_delete_confirm_description_short),
            icon = PopUpIconAnimation(id = R.raw.lottie_animation_validation_green),
            buttonText = UiText.StringResource(R.string.game_delete_confirm_button)
        )
        val tag = showPopUpUseCase(popUp)

        executeFlowUseCase({
            observeInteractionUseCase(tag)
        }, onStream = { interaction ->
            when (interaction.event) {
                PopUpUserInteraction.Confirm -> {
                    viewModelScope.launch {
                        _game.value?.let { game ->
                            ntfy.startAction("Deleting game ${game.id}")
                            try {
                                awaitFlowResult { removeGameUseCase(game) }
                                makeToastService(R.string.game_delete_success)
                                Log.d(TAG, "Emitting gameDeletedEvents")
                                _gameDeletedEvents.emit(Unit)
                                Log.d(TAG, "Setting gameState to Idle after delete")
                                _gameState.value = GameState.DownloadRequired
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to delete game", e)
                                ntfy.exception(e)
                                makeToastService(R.string.game_delete_error)
                                _gameState.value = GameState.Idle
                                _game.value?.let { determineGameState(it) }
                            }
                        }
                    }
                }

                PopUpUserInteraction.Dismiss -> {
                    Log.d(TAG, "Popup dismissed - doing nothing")
                }

                else -> {}
            }
        })
    }

    /**
     * Loads the game data from repository.
     */
    private fun loadGame(gameId: String) {
        Log.d(TAG, "loadGame called with gameId=$gameId, current gameId=${this.gameId}")
        _gameState.value = GameState.Loading

        viewModelScope.launch {
            try {
                val game = awaitFlowResult { getGameUseCase(gameId) }
                _game.value = game

                // Load chapter and observe download state in parallel
                launch { loadCurrentChapter(gameId) }
                launch { observeDownloadState(gameId) }

                // Determine initial game state
                determineGameState(game)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load game", e)
                _gameState.value = GameState.LoadingError
            }
        }
    }

    /**
     * Observes the download state from GameDownloadManager and maps it to GameState.
     */
    private fun observeDownloadState(gameId: String) {
        viewModelScope.launch {
            gameDownloadManager.getDownloadState(gameId).collect { downloadState ->
                when (downloadState) {
                    is GameDownloadState.Downloading -> {
                        _gameState.value = GameState.DownloadingGame(progress = downloadState.progress)
                    }

                    GameDownloadState.Extracting -> {
                        _gameState.value = GameState.DownloadingGame(progress = 100)
                    }

                    GameDownloadState.Completed -> {
                        // Set game version after successful download
                        _game.value?.let { game ->
                            try {
                                awaitFlowResult { setGameVersionUseCase(game) }
                                
                                // Fire-and-forget: Fetch and cache chapters in background
                                // This ensures chapters are available offline after download
                                fetchAndCacheChapters(game.id)
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to set game version", e)
                                ntfy.exception(e)
                            }
                        }
                        _gameState.value = GameState.ReadyToPlay
                    }

                    is GameDownloadState.Error -> {
                        _gameState.value = GameState.LoadingError
                    }

                    GameDownloadState.Idle,
                    GameDownloadState.Cancelled -> {
                        // Don't change state on idle/cancelled
                        if (_gameState.value is GameState.DownloadingGame) {
                            _game.value?.let { determineGameState(it) }
                        }
                    }
                }
            }
        }
    }

    /**
     * Determines the initial game state based on local files and update status.
     */
    private suspend fun determineGameState(game: Game) {
        try {
            val hasFiles = deviceHasGameFiles(game)
            if (!hasFiles) {
                _gameState.value = GameState.DownloadRequired
                return
            }

            val isUpdatable = isGameUpdatable(game)
            if (isUpdatable) {
                _gameState.value = GameState.UpdateGameRequired
                return
            }

            val chapter = _currentChapter.value
            if (chapter != null && !chapter.isAvailable) {
                _gameState.value = GameState.ChapterUnavailable(
                    number = chapter.number,
                    createdAt = chapter.createdAt
                )
                return
            }

            _gameState.value = GameState.ReadyToPlay
        } catch (e: Exception) {
            Log.e(TAG, "Error determining game state", e)
            _gameState.value = GameState.LoadingError
        }
    }

    /**
     * Starts downloading the game.
     */
    private fun startDownload() {
        ntfy.startAction("Start downloading game")
        val game = _game.value ?: return

        if (!customer.isUserConnected() && game.isPremium()) {
            val exception = UserNotConnectedException()
            ntfy.urgent(exception)
            throw exception
        }

        viewModelScope.launch {
            try {
                gameDownloadManager.downloadGame(
                    game = game,
                    userId = if (game.isPremium()) customer.getUserId() else null,
                    userToken = if (game.isPremium()) customer.getUserToken() else null
                )
            } catch (e: GameDownloadForbiddenException) {
                Log.e(TAG, "Forbidden access to download", e)
                ntfy.urgent(e)
                _gameState.value = GameState.LoadingError
            } catch (e: Exception) {
                Log.e(TAG, "Download failed", e)
                ntfy.exception(e)
                _gameState.value = GameState.LoadingError
            }
        }
    }

    private suspend fun loadCurrentChapter(gameId: String) {
        try {
            getCurrentChapterUseCase(gameId, false).collect { result ->
                result.getOrNull()?.let { chapter ->
                    _currentChapter.value = chapter
                    _currentChapterNumber.intValue = chapter.number
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load current chapter", e)
        }
    }

    /**
     * Fetches chapters from API and caches them in the local database.
     * This is a fire-and-forget background operation that runs during/after download.
     * Uses Mutex to prevent concurrent fetching (race condition protection).
     *
     * @param storyId The ID of the story to fetch chapters for.
     */
    private fun fetchAndCacheChapters(storyId: String) {
        viewModelScope.launch {
            // Try to acquire mutex without blocking - if another fetch is in progress, skip
            if (!chaptersDownloadMutex.tryLock()) {
                Log.d(TAG, "Chapters fetch already in progress for $storyId, skipping")
                return@launch
            }

            try {
                ntfy.startAction("Fetching chapters for $storyId")
                getChaptersUseCase(storyId).collect { result ->
                    result.onSuccess { chapters ->
                        Log.d(TAG, "Successfully cached ${chapters.size} chapters for $storyId")
                    }.onFailure { error ->
                        ntfy.exception(error)
                    }
                }
            } catch (e: Exception) {
                ntfy.exception(e)
            } finally {
                chaptersDownloadMutex.unlock()
            }
        }
    }

    private suspend fun deviceHasGameFiles(game: Game): Boolean {
        return try {
            awaitFlowResult { hasGameLocalFilesUseCase(game) }
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun isGameUpdatable(game: Game): Boolean {
        return try {
            awaitFlowResult { isGameUpdatableUseCase(game) }
        } catch (e: Exception) {
            false
        }
    }

    override fun onCleared() {
        super.onCleared()
        gameId?.let { id ->
            // Only cleanup if not downloading (preserve download in background)
            val downloadState = gameDownloadManager.getDownloadState(id).value
            if (downloadState !is GameDownloadState.Downloading &&
                downloadState !is GameDownloadState.Extracting
            ) {
                gameDownloadManager.cleanup(id)
            }
        }
    }

    companion object {
        private const val TAG = "GamePreviewModalVM"
    }
}
