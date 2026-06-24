package fr.purpletear.sutoko.screens.create

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.core.presentation.extensions.Resource
import com.purpletear.game.presentation.model.GameActionState
import com.purpletear.game.presentation.model.GameItem
import com.purpletear.game.presentation.model.toGameActionState
import com.purpletear.sutoko.core.domain.helper.AppVersionProvider
import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.game.model.game.GameCatalog
import com.purpletear.sutoko.game.model.game.GameInstall
import com.purpletear.sutoko.game.repository.game.GameInstallRepository
import com.purpletear.sutoko.game.repository.game.GameRepository
import com.purpletear.sutoko.game.service.MediaUrlResolver
import com.purpletear.sutoko.game.usecase.DownloadGameUseCase
import com.purpletear.sutoko.game.usecase.SearchGamesUseCase
import com.purpletear.sutoko.shop.domain.repository.ShopRepository
import com.purpletear.sutoko.shop.domain.repository.model.Balance
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.sutoko.inapppurchase.application.domain.repository.PurchaseRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

sealed class CreatePageEvent {
    data object OpenAppStore : CreatePageEvent()
}

@HiltViewModel
class CreateViewModel @Inject constructor(
    shopRepository: ShopRepository,
    gameRepository: GameRepository,
    private val gamePurchaseRepository: PurchaseRepository,
    private val gameInstallRepository: GameInstallRepository,
    mediaUrlResolver: MediaUrlResolver,
    appVersionProvider: AppVersionProvider,
    private val downloadGameUseCase: DownloadGameUseCase,
    private val searchGamesUseCase: SearchGamesUseCase,
    private val userRepository: UserRepository,
) : ViewModel() {
    val appBuildNumber: Int = appVersionProvider.getVersionCode()

    private val _events = Channel<CreatePageEvent>(Channel.CONFLATED)
    val events: Flow<CreatePageEvent> = _events.receiveAsFlow()

    val balance: StateFlow<Resource<Balance>> = shopRepository.observeBalance()
        .map { Resource.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(7000),
            initialValue = Resource.Loading(),
        )


    private val _searchQuery = MutableStateFlow("")
    private val _searchResults = MutableStateFlow<List<GameCatalog>>(emptyList())
    private val _isSearching = MutableStateFlow(false)

    val isSearching: StateFlow<Boolean> = _isSearching

    private data class GameOwnershipState(
        val purchasedSkus: Set<String>,
        val installs: List<GameInstall>,
        val downloads: Map<String, Float>,
    )

    private val gameOwnershipState: StateFlow<GameOwnershipState> = combine(
        gamePurchaseRepository.observePurchasedSkus(),
        gameInstallRepository.observeInstalls(),
        gameInstallRepository.observeDownloadProgresses(),
    ) { purchasedSkus, installs, downloads ->
        GameOwnershipState(purchasedSkus, installs, downloads)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(7000),
        initialValue = GameOwnershipState(emptySet(), emptyList(), emptyMap()),
    )

    val games: StateFlow<List<GameItem>> = combine(
        gameRepository.observeUserGames(),
        _searchQuery,
        _searchResults,
        gameOwnershipState,
    ) { userCatalogs, query, searchCatalogs, ownership ->
        val catalogs = if (query.isBlank()) userCatalogs else searchCatalogs
        catalogs.map { catalog ->
            buildGameItem(
                catalog = catalog,
                purchasedSkus = ownership.purchasedSkus,
                installs = ownership.installs,
                downloads = ownership.downloads,
                mediaUrlResolver = mediaUrlResolver,
            )
        }
    }.catch { e ->
        emit(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(7000),
        initialValue = emptyList(),
    )

    fun onSearchQueryChange(query: String) {
        val trimmed = query.trimStart()
        _searchQuery.value = trimmed
        if (trimmed.isEmpty()) {
            _searchResults.value = emptyList()
        }
    }

    fun onSearchSubmit(query: String) {
        val trimmed = query.trim()
        _searchQuery.value = trimmed
        if (trimmed.isEmpty()) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            _isSearching.value = true
            searchGamesUseCase(
                query = trimmed,
                languageTag = Locale.getDefault().toLanguageTag(),
            ).onSuccess { catalogs ->
                _searchResults.value = catalogs
            }.onFailure { error ->
                Log.e(TAG, "Search failed for query=$trimmed", error)
            }
            _isSearching.value = false
        }
    }

    fun onGameGetClick(game: GameItem) {
        val state = game.toGameActionState(
            currentChapter = null,
            appBuildNumber = appBuildNumber,
        )

        when (state) {
            is GameActionState.Download,
            is GameActionState.UpdateGame -> startDownload(game.id)

            is GameActionState.Purchase -> purchaseGame(game)
            is GameActionState.UpdateApp -> sendEvent(CreatePageEvent.OpenAppStore)
            else -> Unit
        }
    }

    fun onGameCancelClick(gameId: String) {
        gameInstallRepository.cancelDownload(gameId)
    }

    private fun purchaseGame(game: GameItem) {
        val sku = game.skuIdentifiers.firstOrNull()
        if (sku == null) {
            Log.w(TAG, "No SKU available for purchase for gameId=${game.id}")
            return
        }

        viewModelScope.launch {
            gamePurchaseRepository.purchase(sku)
                .onFailure { error ->
                    Log.e(TAG, "Purchase failed for sku=$sku", error)
                }
        }
    }

    private fun startDownload(gameId: String) {
        viewModelScope.launch {
            val user = userRepository.observeUser().firstOrNull()
            downloadGameUseCase(
                gameId = gameId,
                userId = user?.id,
                userToken = user?.token,
            )
                .catch { error ->
                    if (error !is IllegalStateException) {
                        Log.e(TAG, "Download failed for gameId=$gameId", error)
                    }
                }
                .collect { progress ->
                    Log.d(TAG, "Download progress for gameId=$gameId: $progress")
                }
        }
    }

    private fun sendEvent(event: CreatePageEvent) {
        viewModelScope.launch {
            _events.send(event)
        }
    }

    private fun buildGameItem(
        catalog: GameCatalog,
        purchasedSkus: Set<String>,
        installs: List<GameInstall>,
        downloads: Map<String, Float>,
        mediaUrlResolver: MediaUrlResolver,
    ): GameItem = GameItem(
        catalog = catalog,
        install = installs.find { it.gameId == catalog.id },
        isPurchased = catalog.skus.any { it in purchasedSkus },
        bannerUrl = mediaUrlResolver.resolveBannerUrl(catalog.banner?.storagePath),
        logoUrl = mediaUrlResolver.resolveBannerUrl(catalog.logo?.storagePath),
        menuBackgroundUrl = mediaUrlResolver.resolveBannerUrl(catalog.menuBackground?.storagePath),
        downloadProgress = downloads[catalog.id],
    )

    companion object {
        private const val TAG = "CreateViewModel"
    }
}
