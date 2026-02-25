package com.purpletear.game.presentation.viewmodels

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharedelements.utils.UiText
import com.purpletear.core.presentation.extensions.awaitFlowResult
import com.purpletear.core.presentation.extensions.executeFlowResultUseCase
import com.purpletear.core.presentation.extensions.executeFlowUseCase
import com.purpletear.core.presentation.services.MakeToastService
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.audio.GameMenuSoundPlayer
import com.purpletear.game.presentation.states.GameButtonsState
import com.purpletear.game.presentation.states.GameState
import com.purpletear.game.presentation.states.toButtonsState
import com.purpletear.game.presentation.states.StoryPreviewAction
import com.purpletear.shop.data.exception.InsufficientFundsException
import com.purpletear.shop.data.exception.InternetConnectivityException
import com.purpletear.shop.data.exception.ItemAlreadyOwnedErrorException
import com.purpletear.shop.data.exception.ProductNotFoundException
import com.purpletear.shop.data.exception.SkuIdentifierNotFoundException
import com.purpletear.shop.domain.usecase.BuyCatalogProductUseCase
import com.purpletear.shop.domain.usecase.GetShopBalanceUseCase
import com.purpletear.shop.domain.usecase.RegisterOrderUseCaseIfNecessary
import com.purpletear.shop.domain.usecase.UserHasProductUseCase
import com.purpletear.sutoko.core.domain.helper.provider.HostProvider
import com.purpletear.sutoko.game.exception.GameDownloadForbiddenException
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.model.Game
import com.purpletear.sutoko.game.model.isPaying
import com.purpletear.sutoko.game.model.isPremium
import com.purpletear.sutoko.game.download.GameDownloadManager
import com.purpletear.sutoko.game.download.GameDownloadState
import com.purpletear.sutoko.game.usecase.GetChaptersUseCase
import com.purpletear.sutoko.game.usecase.GetCurrentChapterUseCase
import com.purpletear.sutoko.game.usecase.GetGameUseCase
import com.purpletear.sutoko.game.usecase.HasGameLocalFilesUseCase
import com.purpletear.sutoko.game.usecase.IsFriendZoned1GameUseCase
import com.purpletear.sutoko.game.usecase.IsFriendZonedGameUseCase
import com.purpletear.sutoko.game.usecase.IsGameUpdatableUseCase
import com.purpletear.sutoko.game.usecase.ObserveCurrentChapterUseCase
import com.purpletear.sutoko.game.usecase.RestartGameUseCase
import com.purpletear.sutoko.game.usecase.SetGameVersionUseCase
import com.purpletear.sutoko.user.usecase.IsUserConnectedUseCase
import com.purpletear.sutoko.user.usecase.OpenSignInPageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.purpletear.sutoko.popup.domain.EditTextPopUp
import fr.purpletear.sutoko.popup.domain.PopUpIconAnimation
import fr.purpletear.sutoko.popup.domain.PopUpUserInteraction
import fr.purpletear.sutoko.popup.domain.SutokoPopUp
import fr.purpletear.sutoko.popup.domain.usecase.GetPopUpInteractionUseCase
import fr.purpletear.sutoko.popup.domain.usecase.ShowPopUpUseCase
import fr.purpletear.sutoko.shop.coinsLogic.Customer
import fr.sutoko.inapppurchase.domain.data_service.BillingDataService
import fr.sutoko.inapppurchase.domain.enums.AppPurchaseType
import fr.sutoko.inapppurchase.domain.model.AppPurchaseDetails
import fr.sutoko.inapppurchase.domain.usecase.ConnectToGooglePlayUseCase
import fr.sutoko.inapppurchase.domain.usecase.GetActiveSubscriptionsSkus
import fr.sutoko.inapppurchase.domain.usecase.GooglePlayClientOwnsProductUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the GamePreview component.
 * Provides game data and manages chapter loading.
 */
@HiltViewModel
class GamePreviewViewModel @Inject constructor(
    private val gameDownloadManager: GameDownloadManager,
    private val customer: Customer,
    private val getGameUseCase: GetGameUseCase,
    private val getChaptersUseCase: GetChaptersUseCase,
    private val getCurrentChapterUseCase: GetCurrentChapterUseCase,
    private val observeCurrentChapterUseCase: ObserveCurrentChapterUseCase,
    private val googlePlayClientOwnsProductUseCase: GooglePlayClientOwnsProductUseCase,
    private val userHasProductUseCase: UserHasProductUseCase,
    private val isGameUpdatableUseCase: IsGameUpdatableUseCase,
    private val getActiveSubscriptionsSkus: GetActiveSubscriptionsSkus,
    private val connectToGooglePlayUseCase: ConnectToGooglePlayUseCase,
    private val isFriendZonedGameUseCase: IsFriendZonedGameUseCase,
    private val isFriendZoned1GameUseCase: IsFriendZoned1GameUseCase,
    private val hasGameLocalFilesUseCase: HasGameLocalFilesUseCase,
    private val isUserConnectedUseCase: IsUserConnectedUseCase,
    private val registerOrderUseCaseIfNecessary: RegisterOrderUseCaseIfNecessary,
    private val buyCatalogProductUseCase: BuyCatalogProductUseCase,
    private val observeInteractionUseCase: GetPopUpInteractionUseCase,
    private val showPopUpUseCase: ShowPopUpUseCase,
    private val setGameVersionUseCase: SetGameVersionUseCase,
    private val openSignInPageUseCase: OpenSignInPageUseCase,
    billingDataService: BillingDataService,
    private val savedStateHandle: SavedStateHandle,
    private val makeToastService: MakeToastService,
    private val hostProvider: HostProvider,
    private val getShopBalanceUseCase: GetShopBalanceUseCase,
    private val restartGameUseCase: RestartGameUseCase,
    private val tableOfSymbols: purpletear.fr.purpleteartools.TableOfSymbols,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val gameId: String = savedStateHandle.get<String>("gameId")!!

    private var _game: MutableState<Game?> = mutableStateOf(null)
    internal val game: State<Game?> get() = _game

    // State to hold the current game state
    private val _gameState: MutableState<GameState> = mutableStateOf(GameState.Loading)
    internal val gameState: GameState get() = _gameState.value

    // UI-specific state for buttons
    internal val gameButtonsState: GameButtonsState
        get() = _gameState.value.toButtonsState(
            currentChapterNumber = currentChapter.value?.number ?: 1,
            gamePrice = game.value?.price,
            onAction = ::onAction
        )

    // StateFlow for chapters to be used with collectAsState
    private val _chaptersFlow = MutableStateFlow<List<Chapter>>(emptyList())
    val chaptersFlow: StateFlow<List<Chapter>> = _chaptersFlow.asStateFlow()

    // State to hold the current chapter
    private val _currentChapter = MutableStateFlow<Chapter?>(null)
    val currentChapter: StateFlow<Chapter?> = _currentChapter.asStateFlow()

    private var _gameBought: MutableState<Boolean> = mutableStateOf(false)
    val isGameBought: State<Boolean> get() = _gameBought

    private var _isUserPremium: MutableState<Boolean> = mutableStateOf(false)
    val isUserPremium: State<Boolean> get() = _isUserPremium

    private val purchases: StateFlow<List<AppPurchaseDetails>> = billingDataService.getPurchases()

    private val _gameBoughtEvents = MutableSharedFlow<Unit>()
    val gameBoughtEvents: SharedFlow<Unit> = _gameBoughtEvents

    private val _vibrationEvents = MutableSharedFlow<Unit>()
    val vibrationEvents: SharedFlow<Unit> = _vibrationEvents

    private val _playGameEvents = MutableSharedFlow<Unit>()
    val playGameEvents: SharedFlow<Unit> = _playGameEvents

    // Sound player for menu background music
    private val menuSoundPlayer = GameMenuSoundPlayer()

    private val _buyGameEvents = MutableSharedFlow<Game>()
    val buyGameEvents: SharedFlow<Game> = _buyGameEvents

    private val _openChaptersEvents = MutableSharedFlow<Pair<Game, List<Chapter>>>()
    val openChaptersEvents: SharedFlow<Pair<Game, List<Chapter>>> = _openChaptersEvents

    private val _openShopEvents = MutableSharedFlow<Unit>()
    val openShopEvents: SharedFlow<Unit> = _openShopEvents

    // Timestamp of the last loadGameData call to prevent spamming
    private var lastLoadGameDataTimestamp = 0L


    private var _isUserConnected: MutableState<Boolean> = mutableStateOf(customer.isUserConnected())

    private var _gameSquareLogoURL: MutableState<String?> = mutableStateOf(null)
    val gameSquareLogoURL: State<String?> get() = _gameSquareLogoURL


    private var _isFriendzonedGame: MutableState<Boolean> = mutableStateOf(false)
    val isFriendzonedGame: State<Boolean> get() = _isFriendzonedGame

    init {

        viewModelScope.launch {
            purchases.collect(::onNewPurchases)
        }

        observeSignIn()

        viewModelScope.launch {
            connectToGooglePlay()
            loadIsUserPremium()
        }

        viewModelScope.launch {
            snapshotFlow { _gameBought.value }
                .distinctUntilChanged()
                .collect { isGameBought ->
                    refreshGameState()
                    if (isGameBought) {

                        _gameBoughtEvents.emit(Unit)
                    }
                }
        }

        observeDownloadState()
    }

    /**
     * Observes the download state from GameDownloadManager and maps it to GameState.
     */
    private fun observeDownloadState() {
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
                        game.value?.let { game ->
                            executeFlowResultUseCase({ setGameVersionUseCase(game) })
                        }
                        _gameState.value = GameState.ReadyToPlay
                        refreshGameState()
                    }
                    is GameDownloadState.Error -> {
                        _gameState.value = GameState.LoadingError
                    }
                    GameDownloadState.Idle,
                    GameDownloadState.Cancelled -> {
                        // Don't change state on idle/cancelled, let refreshGameState handle it
                        if (_gameState.value is GameState.DownloadingGame) {
                            refreshGameState()
                        }
                    }
                }
            }
        }
    }

    /**
     * Loads and updates the logo URL for the specified game's square media logo.
     *
     * @param game The game object containing the media logo information. If the 'logoAsset'
     *             property is not null, its storagePath is used to generate the URL.
     */
    private fun loadGameLogoURL(game: Game) {
        game.logoAsset?.storagePath?.let { path ->
            _gameSquareLogoURL.value = "https://sutoko.com/media/$path"
        }
    }

    internal fun onAction(action: StoryPreviewAction?) {
        when (action) {
            StoryPreviewAction.OnPlay -> {
                viewModelScope.launch {
                    if (shouldAskFirstName()) {
                        askFirstNameAndPlay()
                    } else {
                        _playGameEvents.emit(Unit)
                    }
                }
            }

            StoryPreviewAction.OnAbortBuy -> {
                viewModelScope.launch { refreshGameState() }
            }

            StoryPreviewAction.OnBuy -> {
                _gameState.value = GameState.ConfirmBuy()
            }

            StoryPreviewAction.OnBuyConfirm -> {
                onBuyStoryPressed()
            }

            StoryPreviewAction.OnDownload -> {
                onClickDownloadGame()
            }

            StoryPreviewAction.OnReload -> {
                loadGameData()
            }

            StoryPreviewAction.OnRestart -> {
                confirmAndRestartGame()
            }

            StoryPreviewAction.OnUpdateApp -> {
                callToActionUpdateApp()
            }

            StoryPreviewAction.OnUpdateGame -> {
                updateGame()
            }

            null -> {}
        }
    }

    private fun callToActionUpdateApp() {
        // Shows a popUp to encourages the user to update the app
    }

    private fun shouldAskFirstName(): Boolean {
        val chapter = currentChapter.value ?: return false
        if (chapter.number != 1) return false
        val existing = try {
            tableOfSymbols.get(gameId.hashCode(), "prenom")
        } catch (_: Exception) {
            null
        }
        return existing.isNullOrBlank()
    }

    private fun askFirstNameAndPlay() {
        val popUp = EditTextPopUp(
            title = UiText.StringResource(R.string.first_name_prompt_title),
            placeholder = UiText.StringResource(R.string.first_name_prompt_placeholder),
        )
        val tag = showPopUpUseCase(popUp)

        executeFlowUseCase({
            observeInteractionUseCase(tag)
        }, onStream = { interaction ->
            when (val e = interaction.event) {
                is PopUpUserInteraction.ConfirmText -> {
                    val sanitized = sanitizeFirstName(e.text)
                    if (sanitized.isNotEmpty()) {
                        viewModelScope.launch {
                            try {
                                tableOfSymbols.addOrSet(gameId.hashCode(), "prenom", sanitized)
                                tableOfSymbols.save(appContext)
                            } catch (_: Exception) {
                            }
                            delay(920)
                            _playGameEvents.emit(Unit)
                        }
                    }
                }

                PopUpUserInteraction.Dismiss -> {
                    // Do nothing on dismiss; user can press play again
                }

                else -> {}
            }
        })
    }

    private fun sanitizeFirstName(input: String): String {
        // Keep only letters (all locales), spaces, hyphens, and apostrophes. Remove emojis and symbols like /*+ etc.
        val cleaned = input
            .trim()
            .replace(Regex("[^\\p{L}\\s'\\-]+"), "")
            .replace(Regex("\\s{2,}"), " ")
        return cleaned.trim()
    }

    // The api also sync orders when requesting balance
    private suspend fun loadBalance() {
        if (customer.isUserConnected()) {
            val balanceResult =
                awaitFlowResult {
                    getShopBalanceUseCase(
                        customer.getUserId(),
                        customer.getUserToken()
                    )
                }
        }
    }

    private fun loadGameData() {
        // Check if 5 seconds have passed since the last call
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastLoadGameDataTimestamp < 10000) {
            // Less than 5 seconds have passed, prevent execution
            return
        }


        // Update the timestamp before executing
        lastLoadGameDataTimestamp = currentTime

        viewModelScope.launch {
            val balanceResult = awaitAll(async { loadBalance() })

            val (gameResult, _) = awaitAll(
                async { loadGame(gameId) },
                async { loadChapters(gameId) },
            )

            // Play menu sound if available
            playMenuSoundIfAvailable()

            awaitAll(
                async { loadCurrentChapter(gameId) }
            )

            observedCurrentChapter(gameId)
            _isFriendzonedGame.value = isFriendZonedGameUseCase(gameId = gameId)

            _gameState.value = GameState.Idle

            if (gameResult) {
                viewModelScope.launch {
                    loadGameLogoURL(game = game.value!!)
                }
            }

            if (gameResult && _isUserConnected.value && _game.value!!.isPaying()) {
                awaitAll(
                    async {
                        loadHasBoughtProduct(
                            game = game.value!!,
                            userId = customer.user.uid!!,
                            userToken = customer.user.token!!
                        )
                    }
                )
            }

            refreshGameState()
        }
    }

    private suspend fun onConfirmBuyStoryPressed() {
        require(customer.user.uid != null)
        require(customer.user.token != null)
        require(game.value != null)
        require(game.value!!.skuIdentifiers.isNotEmpty())

        _gameState.value = GameState.ConfirmBuy(isLoading = true)
        val skuIdentifier = game.value!!.skuIdentifiers.first()

        try {
            delay(1200)
            awaitFlowResult {
                buyCatalogProductUseCase(
                    userId = customer.getUserId(),
                    skuIdentifier = skuIdentifier,
                    "story"
                )
            }

            _gameState.value = GameState.ConfirmedBuy
            delay(3200)
            _gameBought.value = true
            vibrate()
            game.value?.let { _buyGameEvents.emit(it) } // Emit event to MainActivity's onBuyGame function
            refreshGameState()
        } catch (e: InsufficientFundsException) {
            showInsufficientFundsAlert()
            e.printStackTrace()
        } catch (e: ItemAlreadyOwnedErrorException) {
            e.printStackTrace()
            _gameBought.value = true
            vibrate()
            showAlreadyBoughtAlert()
            // Handle
        } catch (e: SkuIdentifierNotFoundException) {
            makeToastService(R.string.game_story_preview_buy_story_story_not_buyable_anymore)
            e.printStackTrace()
            // Handle
        } catch (e: ProductNotFoundException) {
            e.printStackTrace()
            // Handle
        } catch (e: InternetConnectivityException) {
            makeToastService(R.string.game_buy_story_error_check_internet)
        } catch (e: Exception) {
            e.printStackTrace()
            makeToastService(R.string.game_story_preview_buy_story_unknown_error_occured_you_have_been_refunded)
            _gameState.value = GameState.ConfirmBuy(isLoading = false)
        }
    }

    private fun showAlreadyBoughtAlert() {
        val popUp = SutokoPopUp(
            title = UiText.StringResource(R.string.already_bought_alert_title),
            description = UiText.StringResource(R.string.already_bought_alert_description),
            icon = PopUpIconAnimation(id = R.raw.lottie_animation_validation_green),
            buttonText = UiText.StringResource(R.string.already_bought_alert_button)
        )
        val tag = showPopUpUseCase(popUp)

        executeFlowUseCase({
            observeInteractionUseCase(tag)
        }, onStream = { interaction ->
            when (interaction.event) {
                PopUpUserInteraction.Dismiss, PopUpUserInteraction.Confirm -> {
                    viewModelScope.launch {
                        refreshGameState()
                    }
                }

                else -> {}
            }
        })
    }

    private fun showInsufficientFundsAlert() {
        val popUp = SutokoPopUp(
            title = UiText.StringResource(R.string.insufficient_funds_alert_title),
            description = UiText.StringResource(R.string.insufficient_funds_alert_description),
            icon = PopUpIconAnimation(id = R.raw.lottie_animation_treasure),
            buttonText = UiText.StringResource(R.string.insufficient_funds_alert_button)
        )
        val tag = showPopUpUseCase(popUp)

        executeFlowUseCase({
            observeInteractionUseCase(tag)
        }, onStream = { interaction ->
            when (interaction.event) {
                PopUpUserInteraction.Dismiss -> {

                }

                PopUpUserInteraction.Confirm -> {
                    viewModelScope.launch {
                        _openShopEvents.emit(Unit)
                    }
                }

                else -> {}
            }
        })
    }

    /**
     * Public function to download the game.
     * Called from the UI components.
     * Delegates to GameDownloadManager for the actual download process.
     */
    private fun onClickDownloadGame() {
        if (game.value?.isPremium() == true && !customer.isUserConnected()) {
            openSignInPageUseCase()
            return
        }

        viewModelScope.launch {
            try {
                val isPremium = game.value?.isPremium() == true
                gameDownloadManager.downloadGame(
                    gameId = gameId,
                    isPremium = isPremium,
                    userId = if (isPremium) customer.getUserId() else null,
                    userToken = if (isPremium) customer.getUserToken() else null
                )
            } catch (e: GameDownloadForbiddenException) {
                e.printStackTrace()
                _gameState.value = GameState.LoadingError
                android.util.Log.e("GamePreviewViewModel", "Forbidden access: ${e.message}")
            } catch (e: Exception) {
                e.printStackTrace()
                _gameState.value = GameState.LoadingError
            }
        }
    }

    private fun onNewPurchases(purchaseDetails: List<AppPurchaseDetails>) {
        if (purchaseDetails.isEmpty()) {
            return
        }

        val purchases = purchaseDetails.filter {
            it.isPurchased() && it.products.any { product -> product.second == AppPurchaseType.STORY }
        }
    }

    private fun observeSignIn() {
        executeFlowUseCase({
            isUserConnectedUseCase()
        }, onStream = {
            _isUserConnected.value = it
        })
    }


    /**
     * Determines the game menu state based on various conditions.
     * This function should be called when the screen is resumed or when chapter loading state changes.
     */
    private suspend fun refreshGameState() {
        val state = _gameState.value

        if (game.value == null || arrayOf(
                GameState.Loading,
                GameState.LoadingError
            ).contains(state)
        )
            return

        if (isFriendZoned1GameUseCase(game.value!!)) {
            _gameState.value = GameState.ReadyToPlay
            return
        }

        if (requirePayment(game.value!!) && isFriendZonedGameUseCase(gameId = gameId)) {
            _gameState.value = GameState.PaymentRequired
            return
        }

        if (requirePayment(game.value!!)) {
            _gameState.value = GameState.PaymentRequired
            return
        }

        if (!deviceHasGameFiles(game.value!!)) {
            _gameState.value = GameState.DownloadRequired
            return
        }

        if (isGameUpdatable(game.value!!)) {
            _gameState.value = GameState.UpdateGameRequired
            return
        }

        if (!isCurrentChapterAvailable()) {
            _gameState.value = GameState.ChapterUnavailable(
                number = currentChapter.value!!.number,
                createdAt = currentChapter.value!!.createdAt
            )
            return
        }

        _gameState.value = GameState.ReadyToPlay
    }

    private fun isCurrentChapterAvailable(): Boolean {
        require(currentChapter.value != null)

        return currentChapter.value!!.isAvailable
    }

    private suspend fun deviceHasGameFiles(game: Game): Boolean {
        try {
            val result = awaitFlowResult { hasGameLocalFilesUseCase(game = game) }
            return result
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private suspend fun isGameUpdatable(game: Game): Boolean {
        try {
            val result = awaitFlowResult { isGameUpdatableUseCase(game = game) }
            return result
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private fun requirePayment(game: Game): Boolean {
        return game.isPremium() && !isGameBought.value && !isUserPremium.value
    }

    private suspend fun connectToGooglePlay() {
        try {
            awaitFlowResult { connectToGooglePlayUseCase() }
        } catch (e: Exception) {
            Log.e("BuyTokensDialogViewModel", e.toString())
        }
    }

    private suspend fun loadIsUserPremium() {
        try {
            val result = awaitFlowResult { getActiveSubscriptionsSkus() }
            _isUserPremium.value = result.any {
                it.lowercase().contains("premium")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onBuyStoryPressed() {
        _gameState.value = GameState.ConfirmBuy(isLoading = true)
        require(game.value != null)
        require(game.value!!.price != null)

        if (_isUserConnected.value) {
            viewModelScope.launch {
                try {
                    onConfirmBuyStoryPressed()
                } catch (e: Exception) {
                    e.printStackTrace()
                    _gameState.value = GameState.LoadingError
                    return@launch
                }
            }
        } else {
            _gameState.value = GameState.ConfirmBuy(isLoading = false)
            // Fire event to MainActivity to open sign-in page
            openSignInPageUseCase()
        }
    }

    private suspend fun loadHasBoughtProduct(game: Game, userId: String, userToken: String) {
        require(game.isPremium())
        require(game.skuIdentifiers.isNotEmpty())
        require(_isUserConnected.value)

        val foundAtLeastOneSkuInGooglePlayWallet =
            checkGooglePlayItems(game = game, userId = userId, userToken = userToken)

        if (foundAtLeastOneSkuInGooglePlayWallet) {
            _gameBought.value = true
            return
        }

        try {
            val result = awaitFlowResult {
                userHasProductUseCase(
                    userId = userId,
                    skuIdentifiers = game.skuIdentifiers
                )
            }
            _gameBought.value = result
        } catch (e: Exception) {
            e.printStackTrace()
            // TODO
        }
    }

    private suspend fun checkGooglePlayItems(
        game: Game,
        userId: String,
        userToken: String
    ): Boolean {
        require(_isUserConnected.value)

        try {
            val googlePlayResult = awaitFlowResult {
                googlePlayClientOwnsProductUseCase(sku = _game.value!!.skuIdentifiers)
            }

            val foundAtLeastOneSkuInGooglePlayWallet =
                googlePlayResult.keys.any { it in game.skuIdentifiers }

            googlePlayResult.forEach { (skuIdentifier, purchaseToken) ->
                registerOrderUseCaseIfNecessary(
                    purchaseToken,
                    skuIdentifier,
                    userId,
                    userToken,
                ).last()
            }

            return foundAtLeastOneSkuInGooglePlayWallet
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private fun updateGame() {
        onClickDownloadGame()
    }

    /**
     * Loads a game by its ID.
     *
     * @param gameId The ID of the game to load.
     */
    private suspend fun loadGame(gameId: String): Boolean {
        _gameState.value = GameState.Loading
        try {
            _game.value = awaitFlowResult {
                getGameUseCase(gameId)
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            _gameState.value = GameState.LoadingError
        }
        return false
    }

    /**
     * Loads chapters for a specific game ID.
     *
     * @param gameId The ID of the game to load chapters for.
     */
    private suspend fun loadChapters(gameId: String): Boolean {
        _gameState.value = GameState.Loading

        try {
            _chaptersFlow.value = awaitFlowResult { getChaptersUseCase(gameId) }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            _gameState.value = GameState.LoadingError
        }
        return false
    }

    /**
     * Loads chapters for a specific game ID.
     *
     * @param gameId The ID of the game to load chapters for.
     */
    private suspend fun loadCurrentChapter(gameId: String): Boolean {
        _gameState.value = GameState.Loading

        try {
            _currentChapter.value = awaitFlowResult { getCurrentChapterUseCase(gameId, true) }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            _gameState.value = GameState.LoadingError
        }
        return false
    }

    private fun observedCurrentChapter(gameId: String) {
        viewModelScope.launch {
            observeCurrentChapterUseCase(gameId).collect { chapter ->
                _currentChapter.value = chapter
            }
        }
    }


    /**
     * Updates the game menu state when the screen is resumed.
     * This function should be called from the UI when the lifecycle state changes to RESUMED.
     * Also plays the menu sound if available.
     */
    fun onResume() {
        // Play menu sound if available
        playMenuSoundIfAvailable()

        loadGameData()
    }

    /**
     * Plays the menu sound with fade in effect if the game has a menuSoundUrl.
     * Note: menuSoundUrl field has been removed from Game model.
     */
    private fun playMenuSoundIfAvailable() {
        // game.value?.menuSoundUrl?.let { soundUrl ->
        //     if (soundUrl.isNotEmpty()) {
        //         menuSoundPlayer.playWithFadeIn(soundUrl, 2000)
        //     }
        // }
    }

    private fun confirmAndRestartGame() {
        val popUp = SutokoPopUp(
            title = UiText.StringResource(R.string.game_restart_confirm_title),
            description = UiText.StringResource(R.string.game_restart_confirm_description),
            icon = PopUpIconAnimation(id = R.raw.lottie_animation_validation_green),
            buttonText = UiText.StringResource(R.string.game_restart_confirm_button)
        )
        val tag = showPopUpUseCase(popUp)

        executeFlowUseCase({
            observeInteractionUseCase(tag)
        }, onStream = { interaction ->
            when (interaction.event) {
                PopUpUserInteraction.Confirm -> {
                    viewModelScope.launch {
                        game.value?.let { game ->
                            try {
                                _gameState.value = GameState.Loading
                                delay(1200)
                                restartGameUseCase(game.id)
                                // Remove per-game first name so it will be asked again on chapter 1
                                try {
                                    tableOfSymbols.removeVar(game.id.hashCode(), "prenom")
                                    tableOfSymbols.save(appContext)
                                } catch (_: Exception) {
                                }
                                loadCurrentChapter(game.id)
                                _gameState.value = GameState.Idle
                                refreshGameState()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                _gameState.value = GameState.Idle
                            }
                        }
                    }
                }

                PopUpUserInteraction.Dismiss -> {
                    // Do nothing on cancel
                }

                else -> {}
            }
        })
    }

    /**
     * Stops the menu sound with fade out effect.
     * This should be called when the view disappears.
     */
    fun stopMenuSound() {
        menuSoundPlayer.stopWithFadeOut(560)
    }

    /**
     * Called when the ViewModel is cleared.
     * Releases resources used by the menu sound player.
     */
    override fun onCleared() {
        super.onCleared()
        menuSoundPlayer.release()
        gameDownloadManager.cleanup(gameId)
    }

    /**
     * Triggers vibration n times with a delay of 280ms between each vibration.
     * The composable collecting the vibrationEvents flow should use the performVibration function
     * to actually vibrate the device.
     *
     * @param n The number of times to vibrate
     */
    private fun vibrate(n: Int = 3) {
        viewModelScope.launch {
            repeat(n) {
                _vibrationEvents.emit(Unit)
                delay(160)
            }
        }
    }
}
