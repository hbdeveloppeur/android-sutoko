package fr.purpletear.sutoko.screens.create

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.core.presentation.extensions.Resource
import com.purpletear.core.presentation.extensions.executeFlowResultUseCase
import com.purpletear.core.presentation.extensions.executeFlowUseCase
import com.purpletear.ntfy.Ntfy
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import com.purpletear.shop.domain.model.Balance
import com.purpletear.shop.domain.usecase.ObserveShopBalanceUseCase
import com.purpletear.sutoko.game.model.Game
import com.purpletear.sutoko.game.model.isPremium
import com.purpletear.sutoko.game.repository.GameRepository
import com.purpletear.sutoko.game.usecase.GetUserGamesUseCase
import com.purpletear.sutoko.game.usecase.SearchStoriesUseCase
import com.purpletear.sutoko.game.download.GameDownloadManager
import com.purpletear.game.presentation.states.GameState
import com.purpletear.sutoko.game.download.GameDownloadState
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.purpletear.sutoko.shop.coinsLogic.Customer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateViewModel @Inject constructor(
    private val observeShopBalanceUseCase: ObserveShopBalanceUseCase,
    private val getUserGamesUseCase: GetUserGamesUseCase,
    private val searchStoriesUseCase: SearchStoriesUseCase,
    private val gameRepository: GameRepository,
    private val gameDownloadManager: GameDownloadManager,
    private val customer: Customer,
    private val ntfy: Ntfy,
) : ViewModel() {

    private val _balance = mutableStateOf<Resource<Balance>>(Resource.Loading())
    val balance: State<Resource<Balance>> = _balance

    private val _userGames = mutableStateOf<Resource<List<Game>>>(Resource.Loading())
    val userGames: State<Resource<List<Game>>> = _userGames

    // Featured cover games - always from the original list, never from search
    private val _featuredGames = mutableStateOf<List<Game>>(emptyList())
    val featuredGames: State<List<Game>> = _featuredGames

    private val _currentPage = mutableIntStateOf(1)
    val currentPage: State<Int> = _currentPage

    private val _hasMorePages = mutableStateOf(true)
    val hasMorePages: State<Boolean> = _hasMorePages

    private val _isLoadingMore = mutableStateOf(false)
    val isLoadingMore: State<Boolean> = _isLoadingMore

    private val _isRefreshing = mutableStateOf(false)
    val isRefreshing: State<Boolean> = _isRefreshing

    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery

    private val _isSearching = mutableStateOf(false)
    val isSearching: State<Boolean> = _isSearching

    companion object {
        private const val PAGE_LIMIT = 20
        private const val MIN_REFRESH_DURATION_MS = 1000L
    }

    init {
        observeBalance()
        loadUserGames()
    }

    private fun observeBalance() {
        viewModelScope.launch {
            executeFlowUseCase(
                { observeShopBalanceUseCase() },
                onStream = { balance ->
                    _balance.value = Resource.Success(
                        balance ?: Balance(coins = getCoins(), diamonds = getDiamonds())
                    )
                },
                onFailure = { exception ->
                    ntfy.exception(exception)
                    _balance.value = Resource.Error(exception)
                }
            )
        }
    }

    /**
     * Get the download state for a specific game.
     * Returns a StateFlow that emits updates whenever the download state changes.
     */
    fun getDownloadState(gameId: String) = gameDownloadManager.getDownloadState(gameId)

    /**
     * Get the installation status for a specific game.
     * Returns a StateFlow that emits updates whenever the installation status changes.
     * This is the single source of truth from GameRepository.
     */
    fun getGameInstallationStatus(gameId: String) = gameRepository.observeGameInstallationStatus(gameId)

    /**
     * Get the combined game state for a specific game.
     * Returns a StateFlow that combines download state and installation status into a single GameState.
     */
    fun getGameState(gameId: String) = gameDownloadManager.getDownloadState(gameId)
        .combine(gameRepository.observeGameInstallationStatus(gameId)) { downloadState, isInstalled ->
            downloadState.toGameState(isInstalled)
        }

    /**
     * Convert GameDownloadState to GameState based on installation status.
     */
    private fun GameDownloadState.toGameState(isInstalled: Boolean): GameState = when (this) {
        is GameDownloadState.Downloading -> GameState.DownloadingGame(progress)
        GameDownloadState.Extracting -> GameState.DownloadingGame(100)
        GameDownloadState.Completed -> GameState.ReadyToPlay
        is GameDownloadState.Error -> GameState.LoadingError
        GameDownloadState.Idle -> if (isInstalled) GameState.ReadyToPlay else GameState.DownloadRequired
        GameDownloadState.Cancelled -> if (isInstalled) GameState.ReadyToPlay else GameState.DownloadRequired
    }

    /**
     * Start downloading a game.
     * The download state will be emitted via getDownloadState(gameId).
     */
    fun downloadGame(game: Game) {
        viewModelScope.launch {
            try {
                gameDownloadManager.downloadGame(
                    game = game,
                    userId = if (game.isPremium()) customer.getUserId() else null,
                    userToken = if (game.isPremium()) customer.getUserToken() else null
                )
            } catch (e: Exception) {
                ntfy.exception(e)
            }
        }
    }

    /**
     * Cancel an ongoing download.
     */
    fun cancelDownload(gameId: String) {
        gameDownloadManager.cancelDownload(gameId)
    }

    fun loadUserGames(
        languageCode: String = "fr-FR",
        page: Int = 1,
        isLoadMore: Boolean = false,
    ) {
        if (isLoadMore) {
            _isLoadingMore.value = true
        } else {
            _userGames.value = Resource.Loading()
        }

        viewModelScope.launch {
            executeFlowResultUseCase(
                useCase = {
                    getUserGamesUseCase(
                        languageCode = languageCode,
                        page = page,
                        limit = PAGE_LIMIT
                    )
                },
                onSuccess = { games ->
                    val currentList = if (isLoadMore && _userGames.value is Resource.Success) {
                        (_userGames.value as Resource.Success).data.orEmpty()
                    } else {
                        emptyList()
                    }
                    val updatedList = currentList + games
                    _userGames.value = Resource.Success(updatedList)
                    
                    // Update featured games only on initial load (not pagination, not search)
                    if (!isLoadMore && !_isSearching.value) {
                        _featuredGames.value = updatedList
                    }
                    
                    _currentPage.intValue = page
                    _hasMorePages.value = games.size >= PAGE_LIMIT
                    _isLoadingMore.value = false
                },
                onFailure = { exception ->
                    ntfy.urgent(exception)
                    _userGames.value = Resource.Error(exception)
                    _isLoadingMore.value = false
                }
            )
        }
    }

    fun refreshUserGames(languageCode: String = "fr-FR") {
        android.util.Log.d("CreateViewModel", "refreshUserGames called")
        _isRefreshing.value = true
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            getUserGamesUseCase(
                languageCode = languageCode,
                page = 1,
                limit = PAGE_LIMIT
            )
                .flowOn(Dispatchers.IO)
                .catch { exception ->
                    val elapsedTime = System.currentTimeMillis() - startTime
                    val remainingDelay = (MIN_REFRESH_DURATION_MS - elapsedTime).coerceAtLeast(0)
                    if (remainingDelay > 0) {
                        delay(remainingDelay)
                    }
                    _userGames.value = Resource.Error(exception)
                    _isRefreshing.value = false
                }
                .collectLatest { result ->
                    val elapsedTime = System.currentTimeMillis() - startTime
                    val remainingDelay = (MIN_REFRESH_DURATION_MS - elapsedTime).coerceAtLeast(0)
                    if (remainingDelay > 0) {
                        delay(remainingDelay)
                    }
                    result.fold(
                        onSuccess = { games ->
                            android.util.Log.d("CreateViewModel", "refreshUserGames success, games count=${games.size}")
                            _userGames.value = Resource.Success(games)
                            
                            // Update featured games only when not searching
                            if (!_isSearching.value) {
                                _featuredGames.value = games
                            }
                            
                            _currentPage.intValue = 1
                            _hasMorePages.value = games.size >= PAGE_LIMIT
                        },
                        onFailure = { exception ->
                            _userGames.value = Resource.Error(exception)
                        }
                    )
                    _isRefreshing.value = false
                }
        }
    }

    fun loadMoreUserGames(languageCode: String = "fr-FR") {
        if (_isLoadingMore.value || !_hasMorePages.value) return
        loadUserGames(
            languageCode = languageCode,
            page = _currentPage.intValue + 1,
            isLoadMore = true
        )
    }

    fun getCoins(): Int = customer.getCoins()

    fun getDiamonds(): Int = customer.getDiamonds()

    fun isUserConnected(): Boolean = customer.isUserConnected()

    /**
     * Update search query. Search is only performed when user submits via keyboard.
     * When query is empty, reset to show all games.
     *
     * @param query The search query string
     */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        
        // Reset to normal list when search is cleared
        if (query.isBlank() && _isSearching.value) {
            _isSearching.value = false
            refreshUserGames()
        }
    }

    /**
     * Perform search when user presses search button on keyboard.
     *
     * @param query The search query string
     */
    fun onSearchSubmit(query: String) {
        _searchQuery.value = query
        
        if (query.isBlank()) {
            _isSearching.value = false
            refreshUserGames()
            return
        }
        
        performSearch(query)
    }

    private fun performSearch(query: String) {
        _isSearching.value = true
        _userGames.value = Resource.Loading()
        
        viewModelScope.launch {
            executeFlowResultUseCase(
                useCase = {
                    searchStoriesUseCase(
                        query = query,
                        languageCode = "fr-FR",
                        page = 1,
                        limit = PAGE_LIMIT
                    )
                },
                onSuccess = { games ->
                    _userGames.value = Resource.Success(games)
                    
                    _currentPage.intValue = 1
                    _hasMorePages.value = false // Search doesn't support pagination yet
                },
                onFailure = { exception ->
                    ntfy.exception(exception)
                    _userGames.value = Resource.Error(exception)
                }
            )
        }
    }
}
