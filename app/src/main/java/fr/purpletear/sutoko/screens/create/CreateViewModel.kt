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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import com.purpletear.shop.domain.model.Balance
import com.purpletear.shop.domain.usecase.ObserveShopBalanceUseCase
import com.purpletear.sutoko.game.model.Game
import com.purpletear.sutoko.game.usecase.GetUserGamesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.purpletear.sutoko.shop.coinsLogic.Customer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateViewModel @Inject constructor(
    private val observeShopBalanceUseCase: ObserveShopBalanceUseCase,
    private val getUserGamesUseCase: GetUserGamesUseCase,
    private val customer: Customer,
    private val ntfy: Ntfy,
) : ViewModel() {

    private val _balance = mutableStateOf<Resource<Balance>>(Resource.Loading())
    val balance: State<Resource<Balance>> = _balance

    private val _userGames = mutableStateOf<Resource<List<Game>>>(Resource.Loading())
    val userGames: State<Resource<List<Game>>> = _userGames

    private val _currentPage = mutableIntStateOf(1)
    val currentPage: State<Int> = _currentPage

    private val _hasMorePages = mutableStateOf(true)
    val hasMorePages: State<Boolean> = _hasMorePages

    private val _isLoadingMore = mutableStateOf(false)
    val isLoadingMore: State<Boolean> = _isLoadingMore

    private val _isRefreshing = mutableStateOf(false)
    val isRefreshing: State<Boolean> = _isRefreshing

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
                    _currentPage.intValue = page
                    _hasMorePages.value = games.size >= PAGE_LIMIT
                    _isLoadingMore.value = false
                },
                onFailure = { exception ->
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
}
