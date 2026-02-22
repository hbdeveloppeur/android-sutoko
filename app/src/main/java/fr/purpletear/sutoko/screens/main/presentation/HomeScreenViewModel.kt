package fr.purpletear.sutoko.screens.main.presentation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.dp
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
import com.purpletear.core.presentation.extensions.executeFlowUseCase
import com.purpletear.framework.services.OpenDiscordOrBrowserService
import com.purpletear.shop.domain.model.Balance
import com.purpletear.shop.domain.usecase.ObserveShopBalanceUseCase
import com.purpletear.sutoko.core.domain.appaction.ActionName
import com.purpletear.sutoko.core.domain.appaction.AppAction
import com.purpletear.sutoko.game.model.Game
import com.purpletear.sutoko.game.model.isPremium
import com.purpletear.sutoko.game.usecase.GetGamesUseCase
import com.purpletear.sutoko.news.model.News
import com.purpletear.sutoko.news.usecase.GetNewsUseCase
import com.purpletear.sutoko.notification.sealed.Screen
import com.purpletear.sutoko.notification.usecase.SetCurrentScreenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.objects.CalendarEvent
import fr.purpletear.sutoko.popup.domain.PopUpIconDrawable
import fr.purpletear.sutoko.popup.domain.SutokoPopUp
import fr.purpletear.sutoko.presentation.util.ImmutableList
import fr.purpletear.sutoko.presentation.util.ImmutableMap
import fr.purpletear.sutoko.screens.main.domain.popup.util.MainMenuCategory
import fr.purpletear.sutoko.shop.coinsLogic.Customer
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
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
    val customer: Customer,
    private val getNewsUseCase: GetNewsUseCase,
    private val getGamesUseCase: GetGamesUseCase,
    private val observeShopBalanceUseCase: ObserveShopBalanceUseCase,
    private val openDiscordOrBrowser: OpenDiscordOrBrowserService,
) : ViewModel(), LifecycleObserver {


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

    private var _squareStories: MutableState<ImmutableList<Game>> =
        mutableStateOf(ImmutableList(listOf()))
    val squareStories: State<ImmutableList<Game>>
        get() = _squareStories

    private var _squareIcons: MutableState<ImmutableMap<String, Int?>> =
        mutableStateOf(ImmutableMap(mapOf()))
    val squareIcons: State<ImmutableMap<String, Int?>>
        get() = _squareIcons

    private var _newsIndex: MutableState<Int> = mutableIntStateOf(0)
    val newsIndex: State<Int>
        get() = _newsIndex

    private var _news: MutableState<List<News>> = mutableStateOf(listOf())
    val news: State<List<News>>
        get() = _news

    // newsState is redundant with news, so it has been removed

    private var _games: MutableState<ImmutableList<Game>> = mutableStateOf(ImmutableList(listOf()))
    val games: State<ImmutableList<Game>>
        get() = _games

    private var _aiConversationMessageCount: MutableState<Int?> = mutableStateOf(null)
    val aiConversationMessageCount: State<Int?>
        get() = _aiConversationMessageCount

    private var _displayAiConversationCard: MutableState<Boolean> = mutableStateOf(true)
    val displayAiConversationCard: State<Boolean>
        get() = _displayAiConversationCard


    private var _fullStories: MutableState<ImmutableList<Game>> =
        mutableStateOf(ImmutableList(listOf()))
    val fullStories: State<ImmutableList<Game>>
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

    val navigate: MutableLiveData<MainEvents> by lazy {
        MutableLiveData<MainEvents>()
    }


    private var _coinsBalance: MutableState<Resource<Balance>> = mutableStateOf(Resource.Loading())
    val coinsBalance: State<Resource<Balance>> = _coinsBalance

    init {
        // Initialize with empty list, will be updated from GetNewsUseCase
        val events =
            savedStateHandle.get<List<CalendarEvent>>(Data.Companion.Extra.CALENDAR_EVENTS.id)
                ?.toList() ?: listOf()

        // Update initial state with saved state handle values
        _state.value = _state.value.copy(
            events = events,
            notificationsOn = symbols.isFirebaseNotificationEnabled
        )

        observeBalance()

        // Fetch news from the repository cache
        viewModelScope.launch {
            getNewsUseCase().collect { result ->
                result.onSuccess { news ->
                    // Update news state variable
                    _news.value = news
                }
            }
        }

        // Fetch games from the repository cache
        viewModelScope.launch {
            getGamesUseCase().collect { result ->
                result.onSuccess { gamesList ->
                    // Update games state variable
                    _games.value = ImmutableList(gamesList)

                    // Update square and full stories
                    _squareStories.value = ImmutableList(
                        getSquareStories(gamesList) ?: listOf()
                    )
                    _fullStories.value = ImmutableList(
                        getFullWidthStories(gamesList)
                    )

                    // Update main state with games
                    _state.value = _state.value.copy(
                        initialStories = gamesList
                    )
                }
            }
        }

        this._squareIcons = mutableStateOf(
            ImmutableMap(
                mapOf(
                    "159" to com.example.sharedelements.R.drawable.logo_card_159,
                    "161" to com.example.sharedelements.R.drawable.logo_card_161,
                    "162" to com.example.sharedelements.R.drawable.logo_card_162,
                    "163" to com.example.sharedelements.R.drawable.logo_card_163,
                )
            )
        )
    }

    private fun observeBalance() {
        _coinsBalance.value = Resource.Loading()
        viewModelScope.launch {
            executeFlowUseCase({
                observeShopBalanceUseCase()
            }, onStream = {
                it?.let { balance ->
                    _coinsBalance.value = Resource.Success(balance)
                }
            }, onFailure = { exception ->
                _coinsBalance.value = Resource.Error(exception)
            })
        }
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

    private fun getSortedCards(cards: Set<Game>): List<Game> {
        val cardsWithIndex = cards.mapIndexed { index, card -> Pair(index, card) }

        return cardsWithIndex.sortedBy { it.second.isPremium() }.map { it.second }
    }


    private fun getFormattedStories(
        category: MainMenuCategory,
        stories: List<Game>
    ): List<Game> {
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
    private fun getSquareStories(stories: List<Game>): List<Game>? {
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
        stories: List<Game>,
    ): List<Game> {
        if (stories.size < 4) {
            return stories
        }
        val storiesToSort = stories.subList(4, stories.size)
        return getSortedCards(storiesToSort.toSet())
    }

    private fun getDiamondsMessage(): Int {
        return when (this._coinsBalance.value.data?.diamonds) {
            0 -> R.string.sutoko_diamonds_context
            else -> R.string.sutoko_diamonds_congrats
        }
    }

    private fun getCoinsMessage(): Int {
        return when (this._coinsBalance.value.data?.coins) {
            0 -> R.string.sutoko_coins_context
            else -> R.string.sutoko_coins_congrats
        }
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

            is MainEvents.AccountButtonPressed, MainEvents.OptionButtonPressed, MainEvents.DiamondButtonPressed, MainEvents.CoinButtonPressed -> {
                navigate.value = event
            }

            is MainEvents.TapMenu -> {
                _categoryState.value = event.category
                viewModelScope.launch {
                    val stories = getFormattedStories(event.category, _state.value.initialStories)
                    _squareStories.value = ImmutableList(
                        getSquareStories(stories) ?: listOf()
                    )
                    _fullStories.value = ImmutableList(
                        getFullWidthStories(stories)
                    )
                }

            }

            is MainEvents.Open -> {

            }

            is MainEvents.TapDiamondsLabel -> {
                this._state.value = this._state.value.copy(
                    popUp = SutokoPopUp(
                        title = UiText.StringResource(
                            R.string.sutoko_count_diamonds,
                            this.customer.getDiamonds()
                        ),
                        description = UiText.StringResource(this.getDiamondsMessage()),
                        icon = PopUpIconDrawable(fr.purpletear.sutoko.shop.presentation.R.drawable.sutoko_diamond_big),
                        buttonText = UiText.StringResource(R.string.sutoko_continue),
                    ),
                )
                this._state.value = this._state.value.copy(isPopUpDisplayed = true)
            }

            is MainEvents.TapCoinsLabel -> {
                this._state.value = this._state.value.copy(
                    popUp = SutokoPopUp(
                        icon = PopUpIconDrawable(fr.purpletear.sutoko.shop.presentation.R.drawable.sutoko_shop_item_mega),
                        iconHeight = 90.dp,
                        title = UiText.StringResource(R.string.sutoko_count_coins),
                        description = UiText.StringResource(this.getCoinsMessage()),
                        buttonText = UiText.StringResource(R.string.sutoko_continue),
                    ),
                )
                this._state.value = this._state.value.copy(isPopUpDisplayed = true)
            }

            is MainEvents.OnPopUpDismissed -> {
                this._state.value = this._state.value.copy(isPopUpDisplayed = false)
            }

            is MainEvents.ToggleNotifications -> {
                this.setNotifications(event.notificationsOn)
                this._state.value =
                    this._state.value.copy(notificationsOn = this.symbols.isFirebaseNotificationEnabled)
            }

            is MainEvents.SwitchNews -> {
                this._newsIndex.value = event.index
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
