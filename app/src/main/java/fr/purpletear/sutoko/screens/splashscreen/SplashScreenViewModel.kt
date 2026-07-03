package fr.purpletear.sutoko.screens.splashscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.purpletear.sutoko.symbols.SymbolsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel for the splash screen.
 * Tracks the completion status of splash screen animations and the asynchronous load of the
 * symbols table. Navigation to the main screen is gated on both being ready.
 */
@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    symbolsRepository: SymbolsRepository
) : ViewModel() {

    // Flag to track if animations are finished (they take 5 seconds to complete)
    private val _areAnimationsFinished = MutableStateFlow(false)
    val areAnimationsFinished: StateFlow<Boolean> = _areAnimationsFinished.asStateFlow()

    // Combined state to check if both animations are finished and data is loaded
    val isReadyToNavigate: StateFlow<Boolean> = combine(
        areAnimationsFinished,
        symbolsRepository.symbols,
    ) { animationsFinished, symbols ->
        animationsFinished && symbols != null
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(4500),
        initialValue = false
    )

    /**
     * Sets the animation completion flag to true.
     * Should be called when animations are finished.
     */
    fun setAnimationsFinished() {
        _areAnimationsFinished.value = true
    }
}
