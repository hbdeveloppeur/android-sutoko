package fr.purpletear.sutoko.screens.splashscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.core.presentation.extensions.awaitFlowResult
import com.purpletear.shop.domain.usecase.GetShopBalanceUseCase
import com.purpletear.sutoko.game.usecase.GetGamesUseCase
import com.purpletear.sutoko.news.usecase.GetNewsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.purpletear.sutoko.shop.coinsLogic.Customer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

/**
 * ViewModel for the splash screen.
 * Injects the GetNewsUseCase and GetGamesUseCase to fetch news and games.
 * Tracks the completion status of splash screen animations and data loading.
 */
@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    private val getNewsUseCase: GetNewsUseCase,
    private val getGamesUseCase: GetGamesUseCase,
    private val getShopBalanceUseCase: GetShopBalanceUseCase,
    private val customer: Customer,
) : ViewModel() {

    // Flag to track if animations are finished (they take 5 seconds to complete)
    private val _areAnimationsFinished = MutableStateFlow(false)
    val areAnimationsFinished: StateFlow<Boolean> = _areAnimationsFinished.asStateFlow()

    // Flags to track data loading status
    private val _isNewsLoaded = MutableStateFlow(false)
    val isNewsLoaded: StateFlow<Boolean> = _isNewsLoaded.asStateFlow()

    private val _isGamesLoaded = MutableStateFlow(false)
    val isGamesLoaded: StateFlow<Boolean> = _isGamesLoaded.asStateFlow()

    // Combined state to check if both animations are finished and data is loaded
    val isReadyToNavigate: StateFlow<Boolean> = combine(
        areAnimationsFinished,
        isNewsLoaded,
        isGamesLoaded
    ) { animationsFinished, newsLoaded, gamesLoaded ->
        animationsFinished && newsLoaded && gamesLoaded
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(4500),
        initialValue = false
    )

    init {
        loadData()

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

    /**
     * Sets the animation completion flag to true.
     * Should be called when animations are finished.
     */
    fun setAnimationsFinished() {
        _areAnimationsFinished.value = true
    }

    /**
     * Loads news and games data in parallel without blocking the UI.
     * Uses a timeout to prevent indefinite waiting.
     * Includes intentional delays to simulate slow network conditions for testing purposes.
     */
    private fun loadData() {
        // Load news
        viewModelScope.launch {
            try {
                withTimeoutOrNull(10.seconds) {
                    getNewsUseCase().first()
                }
            } catch (e: Exception) {
                //
            } finally {
                _isNewsLoaded.value = true
            }
        }

        // Load games
        viewModelScope.launch {
            try {
                withTimeoutOrNull(10.seconds) {
                    getGamesUseCase().first()
                }
            } catch (e: Exception) {
                //
            } finally {
                _isGamesLoaded.value = true
            }
        }
    }
}
