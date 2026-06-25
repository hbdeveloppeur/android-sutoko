package fr.purpletear.sutoko.screens.main.presentation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharedelements.Data
import com.example.sharedelements.SutokoAppParams
import com.example.sharedelements.utils.UiText
import com.google.firebase.analytics.FirebaseAnalytics
import com.purpletear.core.presentation.extensions.Resource
import com.purpletear.framework.services.OpenDiscordOrBrowserService
import com.purpletear.sutoko.core.domain.appaction.ActionName
import com.purpletear.sutoko.core.domain.appaction.AppAction
import com.purpletear.sutoko.game.model.game.GameCatalog
import com.purpletear.sutoko.game.model.game.isPremium
import com.purpletear.sutoko.game.usecase.ObserveOfficialGamesUseCase
import com.purpletear.sutoko.news.model.News
import com.purpletear.sutoko.news.usecase.ObserveNewsUseCase
import com.purpletear.sutoko.notification.sealed.Screen
import com.purpletear.sutoko.notification.usecase.SetCurrentScreenUseCase
import com.purpletear.sutoko.shop.domain.repository.ShopRepository
import com.purpletear.sutoko.shop.domain.repository.model.Balance
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.objects.CalendarEvent
import fr.purpletear.sutoko.screens.main.domain.popup.util.MainMenuCategory
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import purpletear.fr.purpleteartools.TableOfSymbols
import javax.inject.Inject

@HiltViewModel
@Stable
class HomeScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private var firebaseAnalytics: FirebaseAnalytics,
    private val screenUseCase: SetCurrentScreenUseCase,
    private val symbols: TableOfSymbols,
    private val observeNewsUseCase: ObserveNewsUseCase,
    private val observeOfficialGamesUseCase: ObserveOfficialGamesUseCase,
    private val openDiscordOrBrowser: OpenDiscordOrBrowserService,
    private val shopRepository: ShopRepository,
) : ViewModel(), LifecycleObserver {

    val balance: StateFlow<Resource<Balance>> = shopRepository.observeBalance()
        .map { Resource.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(7000),
            initialValue = Resource.Loading(),
        )

    // Observe official games from the repository cache; sync is handled by CatalogSyncCoordinator
    private val games: StateFlow<List<GameCatalog>> = observeOfficialGamesUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(7000),
            initialValue = emptyList(),
        )

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

    private val _categoryState = mutableStateOf<MainMenuCategory>(MainMenuCategory.All)
    val categoryState: State<MainMenuCategory>
        get() {
            return _categoryState
        }

    private val _navEvents = Channel<String>(Channel.BUFFERED)
    val navEvents = _navEvents.receiveAsFlow()

    private var _squareStories: MutableState<List<GameCatalog>> =
        mutableStateOf(emptyList())
    val squareStories: State<List<GameCatalog>>
        get() = _squareStories

    private var _squareIcons: MutableState<Map<Int, Int?>> =
        mutableStateOf(emptyMap())
    val squareIcons: State<Map<Int, Int?>>
        get() = _squareIcons

    var news: StateFlow<List<News>> = observeNewsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(7000),
            initialValue = emptyList(),
        )

    private var _aiConversationMessageCount: MutableState<Int?> = mutableStateOf(null)
    val aiConversationMessageCount: State<Int?>
        get() = _aiConversationMessageCount

    private var _displayAiConversationCard: MutableState<Boolean> = mutableStateOf(true)
    val displayAiConversationCard: State<Boolean>
        get() = _displayAiConversationCard


    private var _fullStories: MutableState<List<GameCatalog>> =
        mutableStateOf(emptyList())
    val fullStories: State<List<GameCatalog>>
        get() = _fullStories


    val saveSymbols: MutableLiveData<TableOfSymbols> by lazy {
        MutableLiveData<TableOfSymbols>()
    }
    val toast: MutableLiveData<UiText.StringResource> by lazy {
        MutableLiveData<UiText.StringResource>()
    }

    val navigateToNews: MutableLiveData<News> by lazy {
        MutableLiveData<News>()
    }

    val navigateToShop: MutableLiveData<Unit> by lazy {
        MutableLiveData()
    }

    init {
        // Initialize with empty list, news is observed below
        val events =
            savedStateHandle.get<List<CalendarEvent>>(Data.Companion.Extra.CALENDAR_EVENTS.id)
                ?.toList() ?: listOf()

        // Update initial state with saved state handle values
        _state.value = _state.value.copy(
            events = events,
            notificationsOn = symbols.isFirebaseNotificationEnabled
        )


        // Derive square/full stories and main state from the observed games
        viewModelScope.launch {
            games.collect { gamesList ->
                _squareStories.value = getSquareStories(gamesList) ?: emptyList()
                _fullStories.value = getFullWidthStories(gamesList)
                _state.value = _state.value.copy(initialStories = gamesList)
            }
        }

        this._squareIcons = mutableStateOf(
            mapOf(
                159 to com.example.sharedelements.R.drawable.logo_card_159,
                161 to com.example.sharedelements.R.drawable.logo_card_161,
                162 to com.example.sharedelements.R.drawable.logo_card_162,
                163 to com.example.sharedelements.R.drawable.logo_card_163,
            )
        )
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
        this.symbols.setFirebaseNotification(value)
        firebaseAnalytics.setUserProperty("want_to_get_notified", if (value) "yes" else "no")
        this.saveSymbols.value = this.symbols
    }

    private fun getSortedCards(cards: Set<GameCatalog>): List<GameCatalog> {
        val cardsWithIndex = cards.mapIndexed { index, card -> Pair(index, card) }

        return cardsWithIndex.sortedBy { it.second.isPremium() }.map { it.second }
    }


    private fun getFormattedStories(
        category: MainMenuCategory,
        stories: List<GameCatalog>
    ): List<GameCatalog> {
        return when (category) {
            MainMenuCategory.All -> {
                stories
            }

            MainMenuCategory.Free -> {
                stories.filter { !it.isPremium() }
            }

            MainMenuCategory.New -> {
                stories.filter { it.isPremium() }
            }

        }
    }


    /**
     * Returns a list of the first four elements in the given list of `Card` objects, or `null` if
     * the list has fewer than four elements.
     *
     * @param stories a list of `Card` objects
     * @return List<Card>?
     */
    private fun getSquareStories(stories: List<GameCatalog>): List<GameCatalog>? {
        if (stories.size < 4) {
            return null
        }
        return stories.subList(0, 4)
    }


    /**
     * Returns a list of elements from the given list of `Card` objects starting at the fifth
     * element. If the list has fewer than five elements, the entire list is returned.
     *
     * @param stories a list of `Card` objects
     * @return List<Card>
     */
    private fun getFullWidthStories(
        stories: List<GameCatalog>,
    ): List<GameCatalog> {
        if (stories.size < 4) {
            return stories
        }
        val storiesToSort = stories.subList(4, stories.size)
        return getSortedCards(storiesToSort.toSet())
    }

    fun displayAiConversationCard(appParams: SutokoAppParams) {
        _displayAiConversationCard.value = appParams.aiConversationAvailability
    }


    fun onEvent(event: MainEvents) {
        when (event) {

            is MainEvents.TapAiConversationMenu -> {
                if (!this._displayAiConversationCard.value) {
                    toast.value = UiText.StringResource(R.string.sutoko_functionality_maintenance)
                }
            }

            is MainEvents.OnFlavorModalDismissed -> {
                // this._displayUserFlavorsSettings.value = false
            }

            is MainEvents.OnAppear -> {

            }

            is MainEvents.StartScroll -> {
            }

            is MainEvents.EndScroll -> {
            }

            is MainEvents.TapMenu -> {
                _categoryState.value = event.category
                viewModelScope.launch {
                    val stories = getFormattedStories(event.category, _state.value.initialStories)
                    _squareStories.value = getSquareStories(stories) ?: emptyList()
                    _fullStories.value = getFullWidthStories(stories)
                }

            }

            is MainEvents.Open -> {

            }

            is MainEvents.ToggleNotifications -> {
                this.setNotifications(event.notificationsOn)
                this._state.value =
                    this._state.value.copy(notificationsOn = this.symbols.isFirebaseNotificationEnabled)
            }

            is MainEvents.TapShop -> {
                this.navigateToShop.value = Unit
            }
        }
    }

    fun handleAppAction(action: AppAction) {
        when (action.name) {
            ActionName.OpenLink -> {
                action.value?.let {
                    openDiscordOrBrowser(it)
                }
            }

            ActionName.OpenGame -> {
                action.value?.takeIf { s -> s.isDigitsOnly() }?.let {
                    navigate(MainScreenPages.GamePreview.createRoute(it))
                }
            }

            ActionName.OpenPage -> {
                if (action.value == "page_shop") {
                    this.navigateToShop.value = Unit
                }
                val route = when (action.value) {
                    "page_create" -> MainScreenPages.Create.route
                    "page_home" -> MainScreenPages.Home.route
                    else -> null
                }

                route?.let {
                    navigate(it)
                }
            }
        }
    }

    private fun navigate(route: String) {
        viewModelScope.launch {
            _navEvents.send(route)
        }
    }
}
