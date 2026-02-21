package fr.purpletear.sutoko.screens.account.screen

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sutokosharedelements.Data
import com.example.sharedelements.OnlineAssetsManager
import com.example.sharedelements.SutokoAppParams
import com.purpletear.core.presentation.extensions.Resource
import com.purpletear.core.presentation.extensions.awaitFlowResult
import com.purpletear.core.presentation.extensions.executeFlowResultUseCase
import com.purpletear.core.presentation.extensions.executeFlowUseCase
import com.purpletear.shop.domain.model.Balance
import com.purpletear.shop.domain.usecase.GetShopBalanceUseCase
import com.purpletear.shop.domain.usecase.ObserveShopBalanceUseCase
import com.purpletear.sutoko.game.model.Game
import com.purpletear.sutoko.game.usecase.GetGamesUseCase
import com.purpletear.sutoko.user.usecase.IsUserConnectedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.purpletear.sutoko.presentation.util.ImmutableList
import fr.purpletear.sutoko.screens.account.screen.model.GameWithOwnership
import fr.purpletear.sutoko.screens.account.screen.transformers.PossessedGamesTransformer
import fr.purpletear.sutoko.shop.coinsLogic.Customer
import kotlinx.coroutines.launch
import purpletear.fr.purpleteartools.TableOfSymbols
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val symbols: TableOfSymbols,
    private val getGamesUseCase: GetGamesUseCase,
    private val observeShopBalanceUseCase: ObserveShopBalanceUseCase,
    private val getShopBalanceUseCase: GetShopBalanceUseCase,
    private val isUserConnectedUseCase: IsUserConnectedUseCase,
    var customer: Customer,
) : ViewModel() {
    private var _possessedGames = ImmutableList<GameWithOwnership>(emptyList())

    private var _allGames: MutableState<List<GameWithOwnership>> = mutableStateOf(listOf())
    val allGames: State<List<GameWithOwnership>>
        get() = _allGames

    private var _isUserConnected: MutableState<Boolean> = mutableStateOf(customer.isUserConnected())
    val isUserConnected: MutableState<Boolean>
        get() = _isUserConnected


    val openAccountConnectionScreen: MutableLiveData<Unit> by lazy {
        MutableLiveData<Unit>()
    }
    val openShopScreen: MutableLiveData<Unit> by lazy {
        MutableLiveData<Unit>()
    }


    val openGameScreen: MutableLiveData<Game> by lazy {
        MutableLiveData<Game>()
    }

    private var _coinsBalance: MutableState<Resource<Balance>> = mutableStateOf(Resource.Loading())
    val coinsBalance: State<Resource<Balance>> = _coinsBalance

    init {
        reloadGames()
        getGames()
        observeBalance()
    }

    private fun observeBalance() {
        _coinsBalance.value = Resource.Loading()
        observeUserConnection()
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

    private fun reloadBalance() {

        viewModelScope.launch {
            if (customer.isUserConnected()) {
                val result = awaitFlowResult {
                    getShopBalanceUseCase(
                        userId = customer.getUserId(),
                        userToken = customer.getUserToken()
                    )
                }
            }
        }
    }


    private fun observeUserConnection() {
        executeFlowUseCase({
            isUserConnectedUseCase()
        }, onStream = { isConnected ->
            _isUserConnected.value = isConnected ?: false
        })
    }


    /**
     * Retrieves the list of games by invoking the associated use case and updates the local state with the result.
     *
     * This method executes a use case to fetch a list of games using a flow. Upon successful retrieval,
     * the fetched games are stored in the `_allGames` state. In case of a failure, no specific error-handling
     * logic is currently implemented.
     *
     * The execution is managed using the `executeFlowResultUseCase` helper function, which handles flow collection and
     * success/failure behavior.
     */
    private fun getGames() {
        executeFlowResultUseCase({
            getGamesUseCase()
        }, onSuccess = { games ->
            _allGames.value = games.map { game ->
                GameWithOwnership(
                    card = game,
                    isPossessed = hasGame(game)
                )
            }.sortedByDescending { it.isPossessed }
        }, onFailure = {

        })
    }

    fun reloadGames() {
        val games = listOf<Game>()
        val possessedGameIds = games.mapNotNull {
            if (hasGame(it)) {
                it.id
            } else {
                null
            }
        }

        val possessedGames = PossessedGamesTransformer.transform(
            games ?: emptyList(),
            possessedGameIds?.toSet() ?: emptySet()
        )
        _possessedGames = ImmutableList(possessedGames)
    }

    fun getAppParams(): SutokoAppParams {
        return savedStateHandle.get<SutokoAppParams>(
            Data.Companion.Extra.APP_PARAMS.id
        ) ?: SutokoAppParams()
    }

    /**
     * Check if the game is available for the user
     * @param card the game to check
     * @return true if the game is available for the user
     */
    private fun hasGame(card: Game): Boolean {
        return card.id == 162 || OnlineAssetsManager.hasStoryFiles(
            card.id,
            card.versionCode,
            symbols
        ) || customer.history.hasStory(card.id)
    }

    fun onEvent(event: AccountEvents) {
        when (event) {
            is AccountEvents.OnShopStateChanged -> {
            }

            is AccountEvents.OnClickCoins -> {
                openShopScreen.value = Unit
            }

            is AccountEvents.OnClickDiamonds -> {
                openShopScreen.value = Unit
            }

            is AccountEvents.OnAccountButtonPressed -> {
                openAccountConnectionScreen.value = Unit
            }

            is AccountEvents.OnAccountStateChanged -> {

            }

            is AccountEvents.OnGamePressed -> {
                openGameScreen.value = event.game
            }
        }
    }

    /**
     * Called when the screen resumes.
     * Updates user connection status, reloads games, and refreshes balance information.
     */
    fun onResume() {
        reloadBalance()
    }
}
