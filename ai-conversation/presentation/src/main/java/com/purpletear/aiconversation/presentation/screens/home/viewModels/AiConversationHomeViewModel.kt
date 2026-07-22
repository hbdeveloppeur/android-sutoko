package com.purpletear.aiconversation.presentation.screens.home.viewModels


import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.aiconversation.domain.model.AiCharacter
import com.purpletear.aiconversation.domain.usecase.GetAiTokensStateUseCase
import com.purpletear.aiconversation.domain.usecase.LoadCharactersUseCase
import com.purpletear.aiconversation.domain.usecase.ObserveCharactersUseCase
import com.purpletear.aiconversation.domain.usecase.TryMessagePackUseCase
import com.purpletear.aiconversation.presentation.R
import com.purpletear.aiconversation.presentation.model.GridItem
import com.purpletear.aiconversation.presentation.screens.home.state.PlayabilityState
import com.purpletear.aiconversation.presentation.usecase.ObserveMessageCoinsDialogVisibilityUseCase
import com.purpletear.aiconversation.presentation.usecase.OpenMessagesCoinsDialogUseCase
import com.purpletear.core.presentation.extensions.executeFlowResultUseCase
import com.purpletear.core.presentation.extensions.executeFlowUseCase
import com.purpletear.core.presentation.services.MakeToastService
import com.purpletear.framework.services.OpenDiscordOrBrowserService
import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.domain.usecase.OpenSignInPageUseCase
import com.purpletear.sutoko.notification.sealed.Screen
import com.purpletear.sutoko.notification.usecase.SetCurrentScreenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiConversationHomeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val observeCharactersUseCase: ObserveCharactersUseCase,
    private val openSignInPageUseCase: OpenSignInPageUseCase,
    private val openMessagesCoinsDialogUseCase: OpenMessagesCoinsDialogUseCase,
    private val getAiTokensStateUseCase: GetAiTokensStateUseCase,
    private val openDiscordOrBrowser: OpenDiscordOrBrowserService,
    private val loadCharactersUseCase: LoadCharactersUseCase,
    private val observeMessageCoinsDialogVisibilityUseCase: ObserveMessageCoinsDialogVisibilityUseCase,
    private val setCurrentScreenUseCase: SetCurrentScreenUseCase,
    private val consumeTryMessagePackUseCase: TryMessagePackUseCase,
    private val makeToastService: MakeToastService,
    private val userRepository: UserRepository,
) : ViewModel() {
    private var _characters: MutableState<List<AiCharacter>> = mutableStateOf(listOf())
    val characters: State<List<AiCharacter>> get() = _characters

    private var _gridItems: MutableState<List<GridItem>> = mutableStateOf(listOf())
    val gridItems: State<List<GridItem>> get() = _gridItems

    private val _selectedCharacter: MutableState<AiCharacter?> = mutableStateOf(null)
    val selectedCharacter: State<AiCharacter?> get() = _selectedCharacter

    private val _isLoading: MutableState<Boolean> = mutableStateOf(false)
    val isLoading: State<Boolean> get() = _isLoading

    private var _customerCoins: MutableState<Int?> = mutableStateOf(null)
    val customerCoins: State<Int?> get() = _customerCoins

    val isUserConnected: StateFlow<Boolean> = userRepository
        .observeIsConnected()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false,
        )

    private val _playabilityState: MutableState<PlayabilityState> =
        mutableStateOf(PlayabilityState.Loading)
    internal val playabilityState: State<PlayabilityState> get() = _playabilityState


    init {
        observeCharacters()
        observeUserConnection()
        observeMessageCoinsDialogVisibility()
        reset()
    }

    fun onResume() {
        setCurrentScreenUseCase(Screen.Home)
        reset()
    }

    private fun reset() {
        viewModelScope.launch {
            loadCharacters()
            loadUserState()
        }
    }

    private fun observeMessageCoinsDialogVisibility() {
        executeFlowUseCase({
            observeMessageCoinsDialogVisibilityUseCase()
        }, onStream = { isVisible ->
            if (!isVisible) {
                onResume()
            }
        })
    }

    private fun observeCharacters() {
        executeFlowUseCase({
            observeCharactersUseCase()
        }, onStream = { characters ->
            _characters.value = characters
            if (_selectedCharacter.value == null) {
                _selectedCharacter.value = characters.firstOrNull()
            }
            _gridItems.value = characters.map { character ->
                GridItem(
                    url = character.avatarUrl ?: "",
                    isSelected = _selectedCharacter.value?.id == character.id,
                    notificationCount = 0,
                    code = character.id.toString()
                )
            }
        })
    }

    private fun observeUserConnection() {
        executeFlowUseCase({
            userRepository.observeIsConnected()
        }, onStream = { isConnected ->
            if (isConnected) {
                reset()
            }
        })
    }

    private suspend fun loadUserState() {
        val user = userRepository.observeUser().first()

        if (null == user) {
            _playabilityState.value =
                PlayabilityState.NotConnected
            return
        }
        executeFlowResultUseCase({
            getAiTokensStateUseCase(userId = user.id)
        }, onSuccess = onSuccess@{ accountState ->
            _customerCoins.value = accountState.messagesCount
            when (true) {
                (accountState.freeTrialAvailable && accountState.messagesCount == 0) -> {
                    _playabilityState.value =
                        PlayabilityState.Triable(isAd = false)
                }

                else -> {
                    _playabilityState.value = PlayabilityState.Playable
                }
            }
        }, onFailure = {
            Log.e("AiConversationHomeViewModel", "loadUserState failed", it)
            // Fallback: allow play, the trial/coins gate is enforced server-side anyway.
            _playabilityState.value = PlayabilityState.Playable
        })
    }

    internal fun openMessagesCoinsDialog() {
        if (!isUserConnected.value) {
            openAccountConnection()
            return
        }
        openMessagesCoinsDialogUseCase()
    }

    internal fun openDiscord() {
        openDiscordOrBrowser("https://discord.gg/jJJwBd9cqr")
    }

    private suspend fun loadCharacters() {
        val user = userRepository.observeUser().first() ?: return
        executeFlowResultUseCase({
            loadCharactersUseCase(
                userId = user.id,
                userToken = user.token,
            )
        })
    }

    fun onTryPressed() {
        viewModelScope.launch { consumeTryPack() }
    }

    private suspend fun consumeTryPack() {
        val user = userRepository.observeUser().first()
        if (null == user) {
            openAccountConnection()
            return
        }

        _isLoading.value = true

        try {
            consumeTryMessagePackUseCase(
                userId = user.id,
                userToken = user.token,
            ).getOrThrow()
            makeToastService(R.string.ai_conversation_presentation_success_try, 10)
            viewModelScope.launch { loadUserState() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            makeToastService(R.string.ai_conversation_presentation_error_trial_consumed)
        } finally {
            _isLoading.value = false
        }
    }

    fun openAccountConnection() {
        openSignInPageUseCase()
    }

    fun onBuyCoinsPressed() {
        if (!isUserConnected.value) {
            openSignInPageUseCase()
            return
        }

        openMessagesCoinsDialogUseCase()
    }

    private fun updateSelectedCharacter(id: Int) {
        try {
            _selectedCharacter.value = _characters.value.first { it.id == id }
        } catch (e: NoSuchElementException) {
            Log.e("AiConversationHomeViewModel", e.toString())
        }
    }

    fun onClickElement(code: String) {
        _gridItems.value = _gridItems.value.map { item ->
            if (item.code == code) {
                item.copy(isSelected = true)
            } else {
                item.copy(isSelected = false)
            }
        }
        val characterId = code.toInt()
        updateSelectedCharacter(id = characterId)
    }
}