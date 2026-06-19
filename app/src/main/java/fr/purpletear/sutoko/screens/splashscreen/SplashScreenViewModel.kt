package fr.purpletear.sutoko.screens.splashscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel for the splash screen.
 * Injects the GetNewsUseCase and GetGamesUseCase to fetch news and games.
 * Tracks the completion status of splash screen animations and data loading.
 */
@HiltViewModel
class SplashScreenViewModel @Inject constructor(
) : ViewModel() {

    // Flag to track if animations are finished (they take 5 seconds to complete)
    private val _areAnimationsFinished = MutableStateFlow(false)
    val areAnimationsFinished: StateFlow<Boolean> = _areAnimationsFinished.asStateFlow()

    // Combined state to check if both animations are finished and data is loaded
    val isReadyToNavigate: StateFlow<Boolean> = combine(
        areAnimationsFinished,
    ) { animationsFinished ->
        animationsFinished.all { it }
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
