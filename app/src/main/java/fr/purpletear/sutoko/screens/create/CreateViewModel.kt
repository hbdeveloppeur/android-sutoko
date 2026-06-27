package fr.purpletear.sutoko.screens.create

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.core.presentation.extensions.Resource
import com.purpletear.game.presentation.model.GameItem
import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.game.model.game.GameCatalog
import com.purpletear.sutoko.game.model.game.GameInstall
import com.purpletear.sutoko.game.repository.game.GameInstallRepository
import com.purpletear.sutoko.game.repository.game.GameRepository
import com.purpletear.sutoko.game.service.MediaUrlResolver
import com.purpletear.sutoko.game.usecase.GetOneUserGamesUseCase
import com.purpletear.sutoko.game.usecase.LoadMoreUserGamesUseCase
import com.purpletear.sutoko.game.usecase.SearchGamesUseCase
import com.purpletear.sutoko.shop.domain.repository.ShopRepository
import com.purpletear.sutoko.shop.domain.repository.model.Balance
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.sutoko.inapppurchase.application.domain.repository.PurchaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CreateViewModel @Inject constructor(
    shopRepository: ShopRepository,
    gameRepository: GameRepository,
    private val gamePurchaseRepository: PurchaseRepository,
    private val gameInstallRepository: GameInstallRepository,
    private val mediaUrlResolver: MediaUrlResolver,
    private val searchGamesUseCase: SearchGamesUseCase,
    private val loadMoreUserGamesUseCase: LoadMoreUserGamesUseCase,
    private val getOneUserGamesUseCase: GetOneUserGamesUseCase,
    private val userRepository: UserRepository,
) : ViewModel() {
    val balance: StateFlow<Resource<Balance>> = shopRepository.observeBalance()
        .map { Resource.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(7000),
            initialValue = Resource.Loading(),
        )

    val isConnected: StateFlow<Boolean> = userRepository.observeIsConnected()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(7000),
            initialValue = false,
        )

    private val _myStories = MutableStateFlow<List<GameItem>>(emptyList())
    val myStories: StateFlow<List<GameItem>> = _myStories

    private val _searchQuery = MutableStateFlow("")
    private val _searchResults = MutableStateFlow<List<GameCatalog>>(emptyList())
    private val _isSearching = MutableStateFlow(false)
    private val _isLoadingMore = MutableStateFlow(false)
    private val _hasMoreUserGames = MutableStateFlow(true)
    private val _searchPage = MutableStateFlow(1)
    private val _hasMoreSearchResults = MutableStateFlow(true)

    val isSearching: StateFlow<Boolean> = _isSearching
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore
    val hasMoreGames: StateFlow<Boolean> = combine(
        _searchQuery,
        _hasMoreUserGames,
        _hasMoreSearchResults,
    ) { query, hasMoreUserGames, hasMoreSearch ->
        if (query.isBlank()) hasMoreUserGames else hasMoreSearch
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(7000),
        initialValue = true,
    )

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

    init {
        viewModelScope.launch {
            isConnected.collect { connected ->
                if (connected) {
                    loadMyStories()
                } else {
                    _myStories.value = emptyList()
                }
            }
        }
    }

    private suspend fun loadMyStories() {
        val user = userRepository.observeUser().firstOrNull() ?: return
        getOneUserGamesUseCase(userId = user.id)
            .onSuccess { catalogs ->
                _myStories.value = catalogs.map { catalog ->
                    buildMyStoryItem(
                        catalog = catalog,
                        mediaUrlResolver = mediaUrlResolver,
                    )
                }
            }
            .onFailure { error ->
                Log.e(TAG, "Failed to load my stories for userId=${user.id}", error)
                _myStories.value = emptyList()
            }
    }

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
        _searchPage.value = 1
        _hasMoreSearchResults.value = true
        if (trimmed.isEmpty()) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            _isSearching.value = true
            searchGamesUseCase(
                query = trimmed,
                languageTag = Locale.getDefault().toLanguageTag(),
                page = 1,
            ).onSuccess { catalogs ->
                _searchResults.value = catalogs
                _hasMoreSearchResults.value = catalogs.size >= SEARCH_PAGE_SIZE
            }.onFailure { error ->
                Log.e(TAG, "Search failed for query=$trimmed", error)
            }
            _isSearching.value = false
        }
    }

    fun loadMore() {
        if (_isLoadingMore.value) return

        viewModelScope.launch {
            _isLoadingMore.value = true
            try {
                val languageTag = Locale.getDefault().toLanguageTag()
                if (_searchQuery.value.isBlank()) {
                    loadMoreUserGamesUseCase(languageTag)
                        .onSuccess { hasMore ->
                            _hasMoreUserGames.value = hasMore
                        }
                        .onFailure { error ->
                            Log.e(TAG, "Load more user games failed", error)
                        }
                } else {
                    val nextPage = _searchPage.value + 1
                    searchGamesUseCase(
                        query = _searchQuery.value,
                        languageTag = languageTag,
                        page = nextPage,
                    ).onSuccess { catalogs ->
                        _searchResults.value = _searchResults.value + catalogs
                        _searchPage.value = nextPage
                        _hasMoreSearchResults.value = catalogs.size >= SEARCH_PAGE_SIZE
                    }.onFailure { error ->
                        Log.e(TAG, "Load more search results failed", error)
                    }
                }
            } finally {
                _isLoadingMore.value = false
            }
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

    private fun buildMyStoryItem(
        catalog: GameCatalog,
        mediaUrlResolver: MediaUrlResolver,
    ): GameItem = buildGameItem(
        catalog = catalog,
        purchasedSkus = catalog.skus.toSet(),
        installs = emptyList(),
        downloads = emptyMap(),
        mediaUrlResolver = mediaUrlResolver,
    )

    companion object {
        private const val TAG = "CreateViewModel"
        private const val SEARCH_PAGE_SIZE = 20
    }
}
