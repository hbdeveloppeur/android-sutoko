package fr.purpletear.sutoko.screens.account.screen

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.core.presentation.extensions.Resource
import com.purpletear.core.presentation.extensions.executeFlowUseCase
import com.purpletear.game.presentation.model.GameItem
import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.game.repository.game.GameRepository
import com.purpletear.sutoko.game.service.MediaUrlResolver
import com.purpletear.sutoko.shop.domain.repository.ShopRepository
import com.purpletear.sutoko.shop.domain.repository.model.Balance
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.sutoko.inapppurchase.application.domain.repository.PurchaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(

    private val gameRepository: GameRepository,
    private val gamePurchaseRepository: PurchaseRepository,
    private val userRepository: UserRepository,
    private val shopRepository: ShopRepository,
    mediaUrlResolver: MediaUrlResolver,
) : ViewModel() {
    val isUserConnected: StateFlow<Boolean> = userRepository
        .observeIsConnected()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false,
        )

    val games: StateFlow<List<GameItem>> = combine(
        gameRepository.observeOfficialGames(),
        gamePurchaseRepository.observePurchasedSkus(),
    ) { catalogs, purchasedSkus ->
        catalogs.map { catalog ->
            GameItem(
                catalog = catalog,
                install = null,
                isPurchased = catalog.skus.any { it in purchasedSkus },
                bannerUrl = mediaUrlResolver.resolveBannerUrl(catalog.banner?.storagePath),
                logoUrl = mediaUrlResolver.resolveBannerUrl(catalog.logo?.storagePath),
                downloadProgress = null
            )
        }
    }.catch { e ->
        emit(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(7000),
        initialValue = emptyList(),
    )


    val openAccountConnectionScreen: MutableLiveData<Unit> by lazy {
        MutableLiveData<Unit>()
    }
    val openShopScreen: MutableLiveData<Unit> by lazy {
        MutableLiveData<Unit>()
    }


    val openGameCatalogEntityScreen: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    private val _coinsBalance = MutableStateFlow<Resource<Balance>>(Resource.Loading())
    val coinsBalance: StateFlow<Resource<Balance>> = _coinsBalance

    val allGames: StateFlow<List<GameItem>> = games

    init {

        viewModelScope.launch {
            observeBalance()
            getBalance()
        }
    }

    private suspend fun observeBalance() {
        shopRepository.observeBalance()
            .catch { exception ->
                _coinsBalance.value = Resource.Error(exception)
            }
            .collect { balance ->
                _coinsBalance.value = Resource.Success(balance)
            }
    }

    private suspend fun getBalance() {
        _coinsBalance.value = Resource.Loading()

        val user = userRepository.observeUser().first() ?: return

        executeFlowUseCase({
            shopRepository.loadBalance(userId = user.id, userToken = user.token)
        }, onStream = { result ->
            result.fold(
                onSuccess = {
                    // Updated balance is emitted via observeBalance()
                },
                onFailure = { exception ->
                    _coinsBalance.value = Resource.Error(exception)
                }
            )
        }, onFailure = { exception ->
            _coinsBalance.value = Resource.Error(exception)
        })
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
                openGameCatalogEntityScreen.value = event.game.id
            }
        }
    }

    /**
     * Called when the screen resumes.
     * Updates user connection status, reloads games, and refreshes balance information.
     */
    fun onResume() {
        viewModelScope.launch { getBalance() }
    }
}
