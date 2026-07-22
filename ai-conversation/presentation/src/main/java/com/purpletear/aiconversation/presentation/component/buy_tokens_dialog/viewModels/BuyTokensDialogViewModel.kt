package com.purpletear.aiconversation.presentation.component.buy_tokens_dialog.viewModels


import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharedelements.utils.UiText
import com.purpletear.aiconversation.domain.model.AiMessagePack
import com.purpletear.aiconversation.domain.usecase.GetAiMessagesPacksUseCase
import com.purpletear.aiconversation.domain.usecase.ObserveAiTokenStateUseCase
import com.purpletear.aiconversation.presentation.R
import com.purpletear.aiconversation.presentation.component.buy_tokens_dialog.UiMessagePack
import com.purpletear.aiconversation.presentation.component.buy_tokens_dialog.states.BuyTokensCoinsDialogState
import com.purpletear.aiconversation.presentation.component.buy_tokens_dialog.states.BuyTokensDialogState
import com.purpletear.aiconversation.presentation.usecase.CloseMessagesCoinsDialogUseCase
import com.purpletear.aiconversation.presentation.usecase.ObserveMessageCoinsDialogVisibilityUseCase
import com.purpletear.core.presentation.extensions.executeFlowUseCase
import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.domain.usecase.OpenSignInPageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.sutoko.inapppurchase.application.domain.model.Product
import fr.sutoko.inapppurchase.application.domain.model.PurchaseErrorType
import fr.sutoko.inapppurchase.application.domain.model.toPurchaseErrorType
import fr.sutoko.inapppurchase.application.domain.repository.PurchaseRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BuyTokensDialogViewModel @Inject constructor(
    private val observeMessageCoinsDialogVisibilityUseCase: ObserveMessageCoinsDialogVisibilityUseCase,
    private val closeMessagesCoinsDialogUseCase: CloseMessagesCoinsDialogUseCase,
    private val openSignInPageUseCase: OpenSignInPageUseCase,
    private val getAiMessagesPacksUseCase: GetAiMessagesPacksUseCase,
    private val observeAiTokenStateUseCase: ObserveAiTokenStateUseCase,
    private val purchaseRepository: PurchaseRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private var _state: MutableState<BuyTokensDialogState> =
        mutableStateOf(BuyTokensDialogState.Loading)
    val state: State<BuyTokensDialogState> get() = _state

    private var _coinsState: MutableState<BuyTokensCoinsDialogState> =
        mutableStateOf(BuyTokensCoinsDialogState.Loading)
    val coinsState: State<BuyTokensCoinsDialogState> get() = _coinsState

    private var _isVisible: MutableState<Boolean> = mutableStateOf(false)
    val isVisible: State<Boolean> get() = _isVisible

    private var loadedPacks: List<UiMessagePack> = emptyList()
    private var loadJob: Job? = null
    private var balanceJob: Job? = null

    init {
        executeFlowUseCase({
            observeMessageCoinsDialogVisibilityUseCase()
        }, onStream = { isVisible ->
            _isVisible.value = isVisible
            if (isVisible) {
                load()
            } else {
                reset()
            }
        }, onFailure = {
            Log.e("BuyTokensDialogViewModel", it.toString())
        })
    }

    fun close() {
        closeMessagesCoinsDialogUseCase()
    }

    fun onClickLogin() {
        close()
        openSignInPageUseCase()
    }

    fun onClickPack(pack: UiMessagePack) {
        _state.value = BuyTokensDialogState.Confirm.Buy(pack)
    }

    fun onCancelConfirm() {
        _state.value = BuyTokensDialogState.Packs(loadedPacks)
    }

    fun onConfirmBuy(pack: UiMessagePack) {
        viewModelScope.launch {
            _state.value = BuyTokensDialogState.Loading
            purchaseRepository.purchase(pack.identifier)
                .onSuccess {
                    // The backend credit is delivered asynchronously by
                    // AiMessagePackPurchaseBackendRegistrar; the coin balance
                    // updates itself through observeAiTokenStateUseCase.
                    _state.value = BuyTokensDialogState.Success(
                        UiText.StringResource(
                            R.string.ai_conversation_presentation_success_buy,
                            pack.tokensCount,
                        )
                    )
                }
                .onFailure { error ->
                    _state.value = when (error.toPurchaseErrorType()) {
                        PurchaseErrorType.CANCELLED,
                        PurchaseErrorType.PENDING,
                            -> BuyTokensDialogState.Packs(loadedPacks)

                        PurchaseErrorType.ALREADY_OWNED,
                        PurchaseErrorType.FAILED,
                        PurchaseErrorType.UNKNOWN,
                            -> BuyTokensDialogState.Error.Generic(
                            UiText.StringResource(R.string.ai_conversation_presentation_error_unknown)
                        )
                    }
                }
        }
    }

    private fun load() {
        loadJob?.cancel()
        balanceJob?.cancel()
        loadJob = viewModelScope.launch {
            val user = userRepository.observeUser().first()
            if (user == null) {
                _coinsState.value = BuyTokensCoinsDialogState.NotLoggedIn
                _state.value = BuyTokensDialogState.Login
                return@launch
            }
            observeBalance()
            loadPacks()
        }
    }

    private fun observeBalance() {
        balanceJob = viewModelScope.launch {
            observeAiTokenStateUseCase().collect { tokensState ->
                _coinsState.value = BuyTokensCoinsDialogState.Loaded(tokensState.messagesCount)
            }
        }
    }

    private suspend fun loadPacks() {
        _state.value = BuyTokensDialogState.Loading
        getAiMessagesPacksUseCase()
            .onSuccess { packs ->
                loadedPacks = withPrices(packs)
                _state.value = BuyTokensDialogState.Packs(loadedPacks)
            }
            .onFailure { error ->
                Log.e("BuyTokensDialogViewModel", "loadPacks failed", error)
                _state.value = BuyTokensDialogState.Error.Generic(
                    UiText.StringResource(R.string.ai_conversation_presentation_error_unknown)
                )
            }
    }

    private suspend fun withPrices(packs: List<AiMessagePack>): List<UiMessagePack> {
        val productsBySku: Map<String, Product> = try {
            purchaseRepository.queryProductDetails(packs.map { it.identifier })
                .getOrDefault(emptyList())
                .associateBy { it.sku }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emptyMap()
        }
        return packs.map { pack -> UiMessagePack(pack, productsBySku[pack.identifier]) }
    }

    private fun reset() {
        loadJob?.cancel()
        balanceJob?.cancel()
        loadedPacks = emptyList()
        _state.value = BuyTokensDialogState.Loading
        _coinsState.value = BuyTokensCoinsDialogState.Loading
    }
}
