package com.purpletear.game.presentation.preview

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.core.presentation.extensions.awaitFlowResult
import com.purpletear.core.presentation.extensions.executeFlowUseCase
import com.purpletear.core.presentation.services.MakeToastService
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.preview.handlers.DownloadHandler
import com.purpletear.game.presentation.preview.handlers.FirstNameHandler
import com.purpletear.game.presentation.preview.handlers.PopupManager
import com.purpletear.game.presentation.preview.handlers.PurchaseHandler
import com.purpletear.game.presentation.preview.mappers.toButtonsState
import com.purpletear.game.presentation.preview.state.GameButtonsState
import com.purpletear.ntfy.Ntfy
import com.purpletear.sutoko.game.download.GameDownloadState
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.model.Game
import com.purpletear.sutoko.game.model.getThumbnailUrl
import com.purpletear.sutoko.game.model.isPremium
import com.purpletear.sutoko.game.usecase.GetChaptersUseCase
import com.purpletear.sutoko.game.usecase.GetCurrentChapterUseCase
import com.purpletear.sutoko.game.usecase.GetGameUseCase
import com.purpletear.sutoko.game.usecase.HasGameLocalFilesUseCase
import com.purpletear.sutoko.game.usecase.IsFriendZoned1GameUseCase
import com.purpletear.sutoko.game.usecase.IsGameUpdatableUseCase
import com.purpletear.sutoko.game.usecase.ObserveCurrentChapterUseCase
import com.purpletear.sutoko.game.usecase.RemoveGameUseCase
import com.purpletear.sutoko.game.usecase.RestartGameUseCase
import com.purpletear.sutoko.user.usecase.IsUserConnectedUseCase
import com.purpletear.sutoko.user.usecase.OpenSignInPageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.purpletear.sutoko.popup.domain.PopUpUserInteraction
import fr.purpletear.sutoko.popup.domain.usecase.GetPopUpInteractionUseCase
import fr.purpletear.sutoko.shop.coinsLogic.Customer
import fr.sutoko.inapppurchase.domain.data_service.BillingDataService
import fr.sutoko.inapppurchase.domain.model.AppPurchaseDetails
import fr.sutoko.inapppurchase.domain.usecase.ConnectToGooglePlayUseCase
import fr.sutoko.inapppurchase.domain.usecase.GetActiveSubscriptionsSkus
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for game preview functionality.
 * Supports both full-screen and modal contexts.
 * In full-screen mode (gameId from SavedStateHandle), manages billing and premium status.
 * In modal mode (gameId from init()), delegates purchase handling to parent.
 */
@HiltViewModel
class GamePreviewViewModel @Inject constructor(
    // Data use cases
    private val getGameUseCase: GetGameUseCase,
    private val getChaptersUseCase: GetChaptersUseCase,
    private val getCurrentChapterUseCase: GetCurrentChapterUseCase,
    private val observeCurrentChapterUseCase: ObserveCurrentChapterUseCase,
    private val isGameUpdatableUseCase: IsGameUpdatableUseCase,
    private val hasGameLocalFilesUseCase: HasGameLocalFilesUseCase,
    private val isFriendZoned1GameUseCase: IsFriendZoned1GameUseCase,
    private val restartGameUseCase: RestartGameUseCase,
    private val removeGameUseCase: RemoveGameUseCase,
    // User/auth use cases
    private val isUserConnectedUseCase: IsUserConnectedUseCase,
    private val openSignInPageUseCase: OpenSignInPageUseCase,
    // Billing
    private val connectToGooglePlayUseCase: ConnectToGooglePlayUseCase,
    private val getActiveSubscriptionsSkus: GetActiveSubscriptionsSkus,
    billingDataService: BillingDataService,
    // Popup handling
    private val observeInteractionUseCase: GetPopUpInteractionUseCase,
    // Services
    private val makeToastService: MakeToastService,
    private val customer: Customer,
    private val ntfy: Ntfy,
    @ApplicationContext private val appContext: Context,
    // State handle (null in preview/modal context if not navigation destination)
    private val savedStateHandle: SavedStateHandle,
    // Feature handlers
    private val purchaseHandler: PurchaseHandler,
    private val downloadHandler: DownloadHandler,
    private val firstNameHandler: FirstNameHandler,
    private val popupManager: PopupManager,
) : ViewModel() {

    companion object {
        private const val TAG = "GamePreviewViewModel"
        private const val STATE_REFRESH_COOLDOWN_MS = 10_000L
        private const val GAME_BOUGHT_DELAY_MS = 3_200L
        private const val RESTART_DELAY_MS = 1_200L
        private const val PLAY_DELAY_MS = 920L
    }

    // region State

    private var gameId: String? = savedStateHandle.get<String>("gameId")
        private set

    private val _game = mutableStateOf<Game?>(null)
    val game: State<Game?> get() = _game

    private val _gameState = mutableStateOf<GameState>(GameState.Loading)
    val gameState: GameState get() = _gameState.value

    internal val gameButtonsState: GameButtonsState
        get() = _gameState.value.toButtonsState(
            currentChapterNumber = _currentChapter.value?.number ?: 1,
            gamePrice = _game.value?.price,
            onAction = ::onAction
        )

    private val _chapters = MutableStateFlow<List<Chapter>>(emptyList())
    val chapters: StateFlow<List<Chapter>> = _chapters.asStateFlow()

    private val _currentChapter = MutableStateFlow<Chapter?>(null)
    val currentChapter: StateFlow<Chapter?> = _currentChapter.asStateFlow()

    private val _isGameBought = mutableStateOf(false)
    val isGameBought: State<Boolean> get() = _isGameBought

    private val _isUserPremium = mutableStateOf(false)
    val isUserPremium: State<Boolean> get() = _isUserPremium

    private val _isUserConnected = mutableStateOf(customer.isUserConnected())

    private val _gameSquareLogoUrl = mutableStateOf<String?>(null)
    val gameSquareLogoUrl: State<String?> get() = _gameSquareLogoUrl

    private val purchases: StateFlow<List<AppPurchaseDetails>> = billingDataService.getPurchases()

    private var lastStateRefreshTime = 0L

    // endregion

    // region Events

    private val _gameBoughtEvents = MutableSharedFlow<Unit>()
    val gameBoughtEvents: SharedFlow<Unit> = _gameBoughtEvents

    private val _vibrationEvents = MutableSharedFlow<Unit>()
    val vibrationEvents: SharedFlow<Unit> = _vibrationEvents

    private val _playGameEvents = MutableSharedFlow<Unit>()
    val playGameEvents: SharedFlow<Unit> = _playGameEvents

    private val _buyGameEvents = MutableSharedFlow<Game>()
    val buyGameEvents: SharedFlow<Game> = _buyGameEvents

    private val _openChaptersEvents = MutableSharedFlow<Pair<Game, List<Chapter>>>()
    val openChaptersEvents: SharedFlow<Pair<Game, List<Chapter>>> = _openChaptersEvents

    private val _openShopEvents = MutableSharedFlow<Unit>()
    val openShopEvents: SharedFlow<Unit> = _openShopEvents

    private val _gameDeletedEvents = MutableSharedFlow<Unit>()
    val gameDeletedEvents: SharedFlow<Unit> = _gameDeletedEvents

    private val _openStoreEvents = MutableSharedFlow<Unit>()
    val openStoreEvents: SharedFlow<Unit> = _openStoreEvents

    private val _dismissEvents = MutableSharedFlow<Unit>()
    val dismissEvents: SharedFlow<Unit> = _dismissEvents

    // endregion

    // region Initialization

    init {
        initializeFromSavedState()
    }

    private fun initializeFromSavedState() {
        // If gameId comes from SavedStateHandle (full-screen mode), auto-initialize
        gameId?.let { id ->
            viewModelScope.launch {
                connectToGooglePlay()
                loadIsUserPremium()
                observePurchases()
                observeUserConnection()
                observeGameBoughtChanges()
                observeDownloadState()
            }
        }
    }

    /**
     * Initialize the ViewModel for modal context (when not using SavedStateHandle).
     * Call this when using the ViewModel in a modal/dialog context.
     */
    fun init(gameId: String) {
        if (this.gameId == gameId && _game.value != null) {
            // Already initialized, just refresh
            viewModelScope.launch { determineGameState() }
            return
        }

        this.gameId = gameId
        this.savedStateHandle["gameId"] = gameId

        viewModelScope.launch {
            connectToGooglePlay()
            loadIsUserPremium()
            observePurchases()
            observeUserConnection()
            observeGameBoughtChanges()
            observeDownloadState()
            loadGameData()
        }
    }

    fun onResume() {
        loadGameData()
    }

    // endregion

    // region Observers

    private fun observePurchases() {
        viewModelScope.launch {
            purchases.collect(::onNewPurchases)
        }
    }

    private fun observeUserConnection() {
        executeFlowUseCase({ isUserConnectedUseCase() }, onStream = {
            _isUserConnected.value = it
        })
    }

    private fun observeGameBoughtChanges() {
        viewModelScope.launch {
            snapshotFlow { _isGameBought.value }
                .collect { isBought ->
                    determineGameState()
                    if (isBought) _gameBoughtEvents.emit(Unit)
                }
        }
    }

    private fun observeDownloadState() {
        val id = gameId ?: return

        viewModelScope.launch {
            downloadHandler.observeDownloadState(id).collect { state ->
                handleDownloadState(state)
            }
        }
    }

    private fun handleDownloadState(state: GameDownloadState) {
        _gameState.value = when (state) {
            is GameDownloadState.Downloading -> GameState.DownloadingGame(progress = state.progress)
            GameDownloadState.Extracting -> GameState.DownloadingGame(progress = 100)
            GameDownloadState.Completed -> {
                viewModelScope.launch {
                    _game.value?.let { fetchAndCacheChapters(it.id) }
                    determineGameState()
                }
                GameState.ReadyToPlay
            }
            is GameDownloadState.Error -> GameState.LoadingError
            GameDownloadState.Idle, GameDownloadState.Cancelled -> {
                if (_gameState.value is GameState.DownloadingGame) {
                    viewModelScope.launch { determineGameState() }
                }
                _gameState.value
            }
        }
    }

    // endregion

    // region Actions

    fun onAction(action: StoryPreviewAction) {
        when (action) {
            StoryPreviewAction.OnPlay -> handlePlay()
            StoryPreviewAction.OnAbortBuy -> viewModelScope.launch { determineGameState() }
            StoryPreviewAction.OnBuy -> _gameState.value = GameState.ConfirmBuy()
            StoryPreviewAction.OnBuyConfirm -> handleBuyConfirm()
            StoryPreviewAction.OnDownload -> handleDownload()
            StoryPreviewAction.OnReload -> loadGameData()
            StoryPreviewAction.OnRestart -> handleRestart()
            StoryPreviewAction.OnUpdateApp -> viewModelScope.launch { _openStoreEvents.emit(Unit) }
            StoryPreviewAction.OnUpdateGame -> handleDownload()
            StoryPreviewAction.OnDelete -> handleDelete()
        }
    }

    private fun handlePlay() {
        viewModelScope.launch {
            when (firstNameHandler.checkFirstNameNeeded(gameId ?: return@launch, _currentChapter.value)) {
                FirstNameHandler.Result.AlreadySet -> _playGameEvents.emit(Unit)
                FirstNameHandler.Result.ShouldAsk -> askFirstName()
            }
        }
    }

    private fun askFirstName() {
        val tag = firstNameHandler.askFirstName()
        executeFlowUseCase({ observeInteractionUseCase(tag) }, onStream = { interaction ->
            when (val event = interaction.event) {
                is PopUpUserInteraction.ConfirmText -> {
                    if (firstNameHandler.saveFirstName(gameId ?: return@executeFlowUseCase, event.text, appContext)) {
                        viewModelScope.launch {
                            delay(PLAY_DELAY_MS)
                            _playGameEvents.emit(Unit)
                        }
                    }
                }
                PopUpUserInteraction.Dismiss -> { /* User can press play again */ }
                else -> {}
            }
        })
    }

    private fun handleBuyConfirm() {
        val currentGame = _game.value ?: return
        _gameState.value = GameState.ConfirmBuy(isLoading = true)

        if (!_isUserConnected.value) {
            _gameState.value = GameState.ConfirmBuy(isLoading = false)
            openSignInPageUseCase()
            return
        }

        viewModelScope.launch {
            when (val result = purchaseHandler.buyStory(game = currentGame)) {
                is PurchaseHandler.Result.Success -> {
                    _gameState.value = GameState.ConfirmedBuy
                    delay(GAME_BOUGHT_DELAY_MS)
                    _isGameBought.value = true
                    vibrate()
                    _buyGameEvents.emit(currentGame)
                    determineGameState()
                }
                is PurchaseHandler.Result.AlreadyOwned -> {
                    _isGameBought.value = true
                    vibrate()
                    showAlreadyBoughtAlert()
                }
                is PurchaseHandler.Result.InsufficientFunds -> {
                    showInsufficientFundsAlert()
                    _gameState.value = GameState.ConfirmBuy(isLoading = false)
                }
                is PurchaseHandler.Result.Error -> {
                    _gameState.value = GameState.ConfirmBuy(isLoading = false)
                }
            }
        }
    }

    private fun handleDownload() {
        val currentGame = _game.value ?: return
        viewModelScope.launch {
            val result = downloadHandler.startDownload(currentGame)
            if (result is DownloadHandler.Result.Error) {
                _gameState.value = GameState.LoadingError
            }
        }
    }

    private fun handleRestart() {
        val tag = popupManager.showRestartConfirmation()
        executeFlowUseCase({ observeInteractionUseCase(tag) }, onStream = { interaction ->
            when (interaction.event) {
                PopUpUserInteraction.Confirm -> confirmRestart()
                PopUpUserInteraction.Dismiss -> {}
                else -> {}
            }
        })
    }

    private fun confirmRestart() {
        viewModelScope.launch {
            val currentGame = _game.value ?: return@launch
            _gameState.value = GameState.Loading
            delay(RESTART_DELAY_MS)

            restartGameUseCase(currentGame.id)
                .onSuccess {
                    firstNameHandler.clearFirstName(currentGame.id, appContext)
                    loadCurrentChapter(currentGame.id)
                    _gameState.value = GameState.Idle
                    determineGameState()
                }
                .onFailure { e ->
                    ntfy.exception(e)
                    _gameState.value = GameState.Idle
                }
        }
    }

    private fun handleDelete() {
        val tag = popupManager.showDeleteConfirmation()
        executeFlowUseCase({ observeInteractionUseCase(tag) }, onStream = { interaction ->
            when (interaction.event) {
                PopUpUserInteraction.Confirm -> confirmDelete()
                PopUpUserInteraction.Dismiss -> {}
                else -> {}
            }
        })
    }

    private fun confirmDelete() {
        viewModelScope.launch {
            val currentGame = _game.value ?: return@launch
            _gameState.value = GameState.Loading
            delay(800)

            try {
                awaitFlowResult { removeGameUseCase(currentGame) }
                makeToastService(R.string.game_delete_success)
                _gameDeletedEvents.emit(Unit)
            } catch (e: Exception) {
                ntfy.exception(e)
                makeToastService(R.string.game_delete_error)
                _gameState.value = GameState.Idle
                determineGameState()
            }
        }
    }

    // endregion

    // region Data Loading

    private fun loadGameData() {
        val id = gameId ?: return
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastStateRefreshTime < STATE_REFRESH_COOLDOWN_MS) return
        lastStateRefreshTime = currentTime

        viewModelScope.launch {
            _gameState.value = GameState.Loading

            val results = awaitAll(
                async { loadGame(id) },
                async { loadChapters(id) },
                async { loadCurrentChapter(id) }
            )

            if (results[0]) { // Game loaded successfully
                observeCurrentChapter(id)
                if (_isUserConnected.value && _game.value?.isPremium() == true) {
                    verifyGameOwnership()
                }
                determineGameState()
            }
        }
    }

    private suspend fun loadGame(gameId: String): Boolean {
        return try {
            _game.value = awaitFlowResult { getGameUseCase(gameId) }
            _gameSquareLogoUrl.value = _game.value?.logoAsset?.getThumbnailUrl() ?: ""
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load game", e)
            _gameState.value = GameState.LoadingError
            false
        }
    }

    private suspend fun loadChapters(gameId: String): Boolean {
        return try {
            _chapters.value = awaitFlowResult { getChaptersUseCase(gameId) }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load chapters", e)
            false
        }
    }

    private suspend fun loadCurrentChapter(gameId: String): Boolean {
        return try {
            _currentChapter.value = awaitFlowResult { getCurrentChapterUseCase(gameId, true) }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load current chapter", e)
            false
        }
    }

    private fun observeCurrentChapter(gameId: String) {
        viewModelScope.launch {
            observeCurrentChapterUseCase(gameId).collect { chapter ->
                _currentChapter.value = chapter
            }
        }
    }

    private suspend fun fetchAndCacheChapters(storyId: String) {
        try {
            ntfy.startAction("Fetching chapters for $storyId")
            getChaptersUseCase(storyId).collect { result ->
                result.onSuccess { chapters ->
                    Log.d(TAG, "Cached ${chapters.size} chapters for $storyId")
                }.onFailure { error ->
                    ntfy.exception(error)
                }
            }
        } catch (e: Exception) {
            ntfy.exception(e)
        }
    }

    // endregion

    // region State Determination

    private suspend fun determineGameState() {
        val currentGame = _game.value ?: return
        val state = _gameState.value

        if (state == GameState.Loading || state == GameState.LoadingError) return

        // Special case: FriendZone 1 is always ready
        if (isFriendZoned1GameUseCase(currentGame)) {
            _gameState.value = GameState.ReadyToPlay
            return
        }

        // Check payment requirement
        if (currentGame.isPremium() && !_isGameBought.value && !_isUserPremium.value) {
            _gameState.value = GameState.PaymentRequired
            return
        }

        // Check files
        if (!deviceHasGameFiles(currentGame)) {
            _gameState.value = GameState.DownloadRequired
            return
        }

        // Check update
        if (isGameUpdatable(currentGame)) {
            _gameState.value = GameState.UpdateGameRequired
            return
        }

        // Check chapter availability
        val chapter = _currentChapter.value
        if (chapter != null && !chapter.isAvailable) {
            _gameState.value = GameState.ChapterUnavailable(
                number = chapter.number,
                createdAt = chapter.createdAt
            )
            return
        }

        _gameState.value = GameState.ReadyToPlay
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

    // endregion

    // region Billing/Premium

    private suspend fun connectToGooglePlay() {
        try {
            awaitFlowResult { connectToGooglePlayUseCase() }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect to Google Play", e)
        }
    }

    private suspend fun loadIsUserPremium() {
        try {
            val result = awaitFlowResult { getActiveSubscriptionsSkus() }
            _isUserPremium.value = result.any { it.lowercase().contains("premium") }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load premium status", e)
        }
    }

    private fun onNewPurchases(purchaseDetails: List<AppPurchaseDetails>) {
        if (purchaseDetails.isEmpty()) return
        // Additional handling if needed
    }

    private suspend fun verifyGameOwnership() {
        val game = _game.value ?: return
        if (purchaseHandler.verifyOwnership(game)) {
            _isGameBought.value = true
        }
    }

    // endregion

    // region Popup Helpers

    private fun showAlreadyBoughtAlert() {
        val tag = popupManager.showAlreadyBoughtAlert()
        executeFlowUseCase({ observeInteractionUseCase(tag) }, onStream = { interaction ->
            when (interaction.event) {
                PopUpUserInteraction.Dismiss, PopUpUserInteraction.Confirm -> {
                    viewModelScope.launch { determineGameState() }
                }
                else -> {}
            }
        })
    }

    private fun showInsufficientFundsAlert() {
        val tag = popupManager.showInsufficientFundsAlert()
        executeFlowUseCase({ observeInteractionUseCase(tag) }, onStream = { interaction ->
            when (interaction.event) {
                PopUpUserInteraction.Confirm -> {
                    viewModelScope.launch { _openShopEvents.emit(Unit) }
                }
                else -> {}
            }
        })
    }

    // endregion

    // region Utilities

    private fun vibrate(count: Int = 3) {
        viewModelScope.launch {
            repeat(count) {
                _vibrationEvents.emit(Unit)
                delay(160)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        gameId?.let { downloadHandler.cleanup(it) }
    }

    // endregion
}
