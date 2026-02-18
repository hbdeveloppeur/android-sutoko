package com.purpletear.aiconversation.presentation.screens.home.viewModels


import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.purpletear.aiconversation.domain.model.AiCharacter
import com.purpletear.aiconversation.domain.usecase.LoadCharactersUseCase
import com.purpletear.aiconversation.domain.usecase.ObserveCharactersUseCase
import com.purpletear.aiconversation.presentation.R
import com.purpletear.aiconversation.presentation.model.GridItem
import com.purpletear.aiconversation.presentation.screens.home.state.PlayabilityState
import com.purpletear.core.presentation.extensions.executeFlowResultUseCase
import com.purpletear.core.presentation.extensions.executeFlowUseCase
import com.purpletear.core.presentation.services.MakeToastService
import com.purpletear.framework.services.OpenDiscordOrBrowserService
import com.purpletear.shop.domain.usecase.GetMessageCoinsDialogVisibilityUseCase
import com.purpletear.shop.domain.usecase.GetUserAccountStateUseCase
import com.purpletear.shop.domain.usecase.OpenMessagesCoinsDialogUseCase
import com.purpletear.shop.domain.usecase.TryMessagePackUseCase
import com.purpletear.sutoko.notification.sealed.Screen
import com.purpletear.sutoko.notification.usecase.SetCurrentScreenUseCase
import com.purpletear.sutoko.user.usecase.IsUserConnectedUseCase
import com.purpletear.sutoko.user.usecase.OpenSignInPageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.purpletear.sutoko.shop.coinsLogic.Customer
import fr.sutoko.inapppurchase.domain.usecase.GetNonAcknowledgeProductUseCase
import javax.inject.Inject

@HiltViewModel
class AiConversationHomeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val observeCharactersUseCase: ObserveCharactersUseCase,
    private val customer: Customer,
    private val openSignInPageUseCase: OpenSignInPageUseCase,
    private val openMessagesCoinsDialogUseCase: OpenMessagesCoinsDialogUseCase,
    private val getUserAccountStateUseCase: GetUserAccountStateUseCase,
    private val openDiscordOrBrowser: OpenDiscordOrBrowserService,
    private val isUserConnectedUseCase: IsUserConnectedUseCase,
    private val loadCharactersUseCase: LoadCharactersUseCase,
    private val observeMessageCoinsDialogVisibilityUseCase: GetMessageCoinsDialogVisibilityUseCase,
    private val setCurrentScreenUseCase: SetCurrentScreenUseCase,
    private val consumeTryMessagePackUseCase: TryMessagePackUseCase,
    private val getNonAcknowledgeProductUseCase: GetNonAcknowledgeProductUseCase,
    private val makeToastService: MakeToastService,
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

    private var _isConnected: MutableState<Boolean> = mutableStateOf(customer.isUserConnected())
    val isConnected: State<Boolean> get() = _isConnected

    private val _playabilityState: MutableState<PlayabilityState> =
        mutableStateOf(PlayabilityState.Loading)
    internal val playabilityState: State<PlayabilityState> get() = _playabilityState


    init {
        observeCharacters()
        observeUserConnection()
        observeMessageCoinsDialogVisibility()
        reset()
        getNonAcknowledgeProduct()
    }

    fun onResume() {
        setCurrentScreenUseCase(Screen.Home)
        reset()
    }

    private fun reset() {
        loadCharacters()
        loadUserState()
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

    private fun getNonAcknowledgeProduct() {
        executeFlowResultUseCase({
            getNonAcknowledgeProductUseCase()
        }, onSuccess = { products ->
            Log.d("ConversationViewModel", "onSuccess: message sent")
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
            isUserConnectedUseCase()
        }, onStream = { isConnected ->
            if (isConnected) {
                reset()
            }
        })
    }

    private fun loadUserState() {
        _isConnected.value = customer.isUserConnected()
        if (!customer.isUserConnected()) {
            _playabilityState.value =
                PlayabilityState.NotConnected
            return
        }
        executeFlowResultUseCase({
            getUserAccountStateUseCase(userId = customer.user.uid!!)
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

        })
    }

    internal fun openMessagesCoinsDialog() {
        if (!customer.isUserConnected()) {
            openAccountConnection()
            return
        }
        openMessagesCoinsDialogUseCase()
    }

    internal fun openDiscord() {
        openDiscordOrBrowser("https://discord.gg/jJJwBd9cqr")
    }

    private fun loadCharacters() {
        executeFlowResultUseCase({
            loadCharactersUseCase(
                userId = customer.user.uid,
                userToken = customer.user.token,
            )
        })
    }

    fun isConnected(): Boolean {
        return customer.isUserConnected()
    }

    fun onTryPressed(isAd: Boolean) {
        if (!customer.isUserConnected()) {
            openAccountConnection()
            return
        }

        _isLoading.value = true

        // TODO Implement ads
        executeFlowResultUseCase({
            consumeTryMessagePackUseCase(
                userId = customer.getUserId(),
                userToken = customer.getUserToken()
            )
        }, onSuccess = { isTrialActivated ->
            if (isTrialActivated) {
                makeToastService(R.string.ai_conversation_presentation_success_try, 10)
                loadUserState()
            } else {
                makeToastService(R.string.ai_conversation_presentation_error_trial_consumed)
            }
        }, finally = {
            _isLoading.value = false
        })
    }

    fun openAccountConnection() {
        openSignInPageUseCase()
    }

    fun onBuyCoinsPressed() {
        if (!customer.isUserConnected()) {
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
        _selectedCharacter.value = _characters.value.first { it.id == id }
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