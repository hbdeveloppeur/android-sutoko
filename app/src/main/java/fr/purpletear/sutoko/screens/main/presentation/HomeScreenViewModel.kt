package fr.purpletear.sutoko.screens.main.presentation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharedelements.Data
import com.example.sharedelements.utils.UiText
import com.purpletear.core.presentation.extensions.Resource
import com.purpletear.sutoko.core.domain.analytics.AnalyticsTracker
import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.game.model.game.GameCatalog
import com.purpletear.sutoko.game.repository.ChapterRepository
import com.purpletear.sutoko.game.repository.game.FavoriteGamesRepository
import com.purpletear.sutoko.game.usecase.GetChaptersUseCase
import com.purpletear.sutoko.game.usecase.ObserveOfficialGamesUseCase
import com.purpletear.sutoko.notification.sealed.Screen
import com.purpletear.sutoko.notification.usecase.SetCurrentScreenUseCase
import com.purpletear.sutoko.shop.domain.repository.ShopRepository
import com.purpletear.sutoko.shop.domain.repository.model.Balance
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.purpletear.sutoko.friendzoned.FriendzonedGameRouter
import fr.purpletear.sutoko.objects.CalendarEvent
import fr.purpletear.sutoko.symbols.SymbolsRepository
import fr.purpletear.sutoko.sync.catalog.CatalogSyncCoordinator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import purpletear.fr.purpleteartools.TableOfSymbols
import javax.inject.Inject

@HiltViewModel
@Stable
class HomeScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private var analyticsTracker: AnalyticsTracker,
    private val screenUseCase: SetCurrentScreenUseCase,
    private val symbolsRepository: SymbolsRepository,
    private val observeOfficialGamesUseCase: ObserveOfficialGamesUseCase,
    private val shopRepository: ShopRepository,
    private val userRepository: UserRepository,
    private val favoriteGamesRepository: FavoriteGamesRepository,
    private val chapterRepository: ChapterRepository,
    private val getChaptersUseCase: GetChaptersUseCase,
    private val catalogSyncCoordinator: CatalogSyncCoordinator,
) : ViewModel(), LifecycleObserver {

    val balance: StateFlow<Resource<Balance>> = shopRepository.observeBalance()
        .map { Resource.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(7000),
            initialValue = Resource.Loading(),
        )

    val isConnected: StateFlow<Boolean?> = userRepository.observeIsConnected()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(7000),
            initialValue = null,
        )

    // Observe official games from the repository cache; sync is handled by CatalogSyncCoordinator
    private val games = observeOfficialGamesUseCase()
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(7000),
            replay = 1,
        )

    val favoriteIds: StateFlow<Set<String>> = favoriteGamesRepository.observeFavoriteIds()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(7000),
            initialValue = emptySet(),
        )

    /** Ids of games with at least one cached chapter releasing in the future. */
    val newChaptersSoonGameIds: StateFlow<Set<String>> =
        chapterRepository.observeStoryIdsWithUpcomingChapters()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(7000),
                initialValue = emptySet(),
            )

    /** Games whose chapters were already fetched once for this ViewModel lifetime. */
    private val warmedUpChapterGameIds = mutableSetOf<String>()

    private val _state: MutableState<MainState> = mutableStateOf(
        MainState(
            initialStories = listOf(),
            events = listOf(),
        )
    )
    val state: State<MainState>
        get() {
            return _state
        }

    private val _catalog = mutableStateOf(HomeCatalogState())
    val catalog: State<HomeCatalogState> = _catalog
    val catalogSyncStatus = catalogSyncCoordinator.status

    val squareIcons: Map<Int, Int?> = mapOf(
        159 to com.example.sharedelements.R.drawable.shared_elements_logo_card_159,
        161 to com.example.sharedelements.R.drawable.shared_elements_logo_card_161,
        162 to com.example.sharedelements.R.drawable.shared_elements_logo_card_162,
        163 to com.example.sharedelements.R.drawable.shared_elements_logo_card_163,
    )

    val saveSymbols: MutableLiveData<TableOfSymbols> by lazy {
        MutableLiveData<TableOfSymbols>()
    }
    val toast: MutableLiveData<UiText.StringResource> by lazy {
        MutableLiveData<UiText.StringResource>()
    }

    val navigateToShop: MutableLiveData<Unit> by lazy {
        MutableLiveData()
    }

    init {
        // Initialize with saved state handle values
        val events =
            savedStateHandle.get<List<CalendarEvent>>(Data.Companion.Extra.CALENDAR_EVENTS.id)
                ?.toList() ?: listOf()

        // Update initial state with saved state handle values
        _state.value = _state.value.copy(
            events = events,
            notificationsOn = true
        )

        viewModelScope.launch {
            val symbols = symbolsRepository.load()
            _state.value = _state.value.copy(
                notificationsOn = symbols.isFirebaseNotificationEnabled
            )
        }

        // Derive square/full stories and main state from the observed games.
        // The backend endpoint order is authoritative: no client-side re-sorting
        // beyond the contractual friendzoned pinning (see sortForHome).
        viewModelScope.launch {
            games.collect { gamesList ->
                _catalog.value = HomeCatalogState.fromGames(sortForHome(gamesList))
                _state.value = _state.value.copy(initialStories = gamesList)
            }
        }

        // Warm the chapters cache once per game so the "new chapters soon" badge
        // reflects server data, not only previously visited stories. Failures are
        // delivered as Result values by the repository and simply leave the badge off.
        viewModelScope.launch {
            games.collect { gamesList ->
                gamesList.forEach { game ->
                    if (warmedUpChapterGameIds.add(game.id)) {
                        launch { getChaptersUseCase(game.id).collect { } }
                    }
                }
            }
        }

    }

    fun retryCatalog() {
        viewModelScope.launch { catalogSyncCoordinator.sync() }
    }

    fun onResume() {
        screenUseCase(Screen.Main)
    }


    /**
     * This function sets a user property for Firebase Analytics, indicating whether the user wants
     * to receive notifications or not.
     *
     * @param value a Boolean representing if the user wants to receive notifications or not.
     */
    private fun setNotifications(value: Boolean) {
        viewModelScope.launch {
            val symbols = symbolsRepository.load()
            // Refresh first: saving below rewrites the whole snapshot, so it must
            // not be based on stale data predating the user's latest game session.
            withContext(Dispatchers.IO) { symbols.read() }
            symbols.setFirebaseNotification(value)
            analyticsTracker.setUserProperty("want_to_get_notified", if (value) "yes" else "no")
            saveSymbols.value = symbols
            _state.value =
                _state.value.copy(notificationsOn = symbols.isFirebaseNotificationEnabled)
        }
    }

    /**
     * Friendzoned games are contractually pinned to the first positions: the home screen
     * always lists them as the 4 first games. Every other game keeps its backend
     * endpoint order (the partition is stable).
     */
    private fun sortForHome(games: List<GameCatalog>): List<GameCatalog> {
        val (friendzoned, others) = games.partition { it.isFriendzoned() }
        return friendzoned + others
    }

    private fun GameCatalog.isFriendzoned(): Boolean =
        legacyId?.let { FriendzonedGameRouter.loaderClassFor(it) != null } == true


    fun onEvent(event: MainEvents) {
        when (event) {

            is MainEvents.OnFlavorModalDismissed -> {
                // this._displayUserFlavorsSettings.value = false
            }

            is MainEvents.ToggleNotifications -> {
                this.setNotifications(event.notificationsOn)
            }

            is MainEvents.TapShop -> {
                this.navigateToShop.value = Unit
            }
        }
    }
}
