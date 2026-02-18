package com.purpletear.ai_conversation.ui.component.buy_tokens_dialog.viewModels


import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sutokosharedelements.utils.UiText
import com.purpletear.ai_conversation.presentation.R
import com.purpletear.ai_conversation.ui.component.buy_tokens_dialog.events.BuyTokensDialogAbort
import com.purpletear.ai_conversation.ui.component.buy_tokens_dialog.states.BuyTokensCoinsDialogState
import com.purpletear.ai_conversation.ui.component.buy_tokens_dialog.states.BuyTokensDialogState
import com.purpletear.ai_conversation.ui.component.buy_tokens_dialog.states.BuyTokensDialogTitleState
import com.purpletear.core.presentation.extensions.executeFlowResultUseCase
import com.purpletear.core.presentation.extensions.executeFlowUseCase
import com.purpletear.shop.domain.model.AiMessagePack
import com.purpletear.shop.domain.usecase.CloseMessagesCoinsDialogUseCase
import com.purpletear.shop.domain.usecase.GetAiMessagesPacksUseCase
import com.purpletear.shop.domain.usecase.GetMessageCoinsDialogVisibilityUseCase
import com.purpletear.shop.domain.usecase.GetUserAccountStateUseCase
import com.purpletear.shop.domain.usecase.PreProcessPurchaseUseCase
import com.purpletear.shop.domain.usecase.ProcessPurchaseUseCase
import com.purpletear.shop.domain.usecase.TryMessagePackUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.purpletear.sutoko.shop.coinsLogic.Customer
import fr.sutoko.in_app_purchase_domain.data_service.BillingDataService
import fr.sutoko.in_app_purchase_domain.enums.AppPurchaseType
import fr.sutoko.in_app_purchase_domain.model.AppPurchaseDetails
import fr.sutoko.in_app_purchase_domain.usecase.AcknowledgeProductUseCase
import fr.sutoko.in_app_purchase_domain.usecase.ConnectToGooglePlayUseCase
import fr.sutoko.in_app_purchase_domain.usecase.GetInAppProductsUseCase
import fr.sutoko.in_app_purchase_domain.usecase.LaunchBillingFlowUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BuyTokensDialogViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val processPurchaseUseCase: ProcessPurchaseUseCase,
    private val preProcessPurchaseUseCase: PreProcessPurchaseUseCase,
    private val getAiMessagesPacksUseCase: GetAiMessagesPacksUseCase,
    private val getInAppProductsUseCase: GetInAppProductsUseCase,
    private val getUserAccountStateUseCase: GetUserAccountStateUseCase,
    private val launchBillingFlowUseCase: LaunchBillingFlowUseCase,
    private val acknowledgeProductUseCase: AcknowledgeProductUseCase,
    private val tryMessagePackUseCase: TryMessagePackUseCase,
    private val getMessageCoinsDialogVisibilityUseCase: GetMessageCoinsDialogVisibilityUseCase,
    private val closeMessagesCoinsDialogUseCase: CloseMessagesCoinsDialogUseCase,
    private val customer: Customer,
    private val connectToGooglePlayUseCase: ConnectToGooglePlayUseCase,
    billingDataService: BillingDataService,
) : ViewModel() {

    private var _messagesPacks: MutableState<List<AiMessagePack>> = mutableStateOf(listOf())
    val messagesPacks: MutableState<List<AiMessagePack>> get() = _messagesPacks

    private var _state: MutableState<BuyTokensDialogState> =
        mutableStateOf(BuyTokensDialogState.Loading)
    val state: MutableState<BuyTokensDialogState> get() = _state

    private var _titleState: MutableState<BuyTokensDialogTitleState> = mutableStateOf(
        BuyTokensDialogTitleState.Buy
    )
    val titleState: MutableState<BuyTokensDialogTitleState> get() = _titleState

    private var _coinsState: MutableState<BuyTokensCoinsDialogState> = mutableStateOf(
        BuyTokensCoinsDialogState.Loading(-1)
    )
    val coinsState: MutableState<BuyTokensCoinsDialogState> get() = _coinsState

    private var _isVisible: MutableState<Boolean> = mutableStateOf(false)
    val isVisible: MutableState<Boolean> get() = _isVisible


    private val purchases = billingDataService.getPurchases()

    init {
        loadPacks()
        updateMessageCount()
        viewModelScope.launch {
            purchases.collect(::onNewPurchases)
        }

        executeFlowUseCase({
            getMessageCoinsDialogVisibilityUseCase()
        }, onStream = {
            _isVisible.value = it
        }, onFailure = {
            Log.e("BuyTokensDialogViewModel", it.toString())
        })
    }


    fun close() {
        loadPacks()
        closeMessagesCoinsDialogUseCase()
    }

    fun onResume() {
        loadPacks()
    }


    private fun onNewPurchases(purchaseDetails: List<AppPurchaseDetails>) {
        if (purchaseDetails.isEmpty()) {
            return
        }

        val purchases = purchaseDetails.filter {
            it.isPurchased() && it.products.any { product -> product.second == AppPurchaseType.MESSAGE_COINS }
        }

        processPurchases(purchases = purchases)
    }


    private fun getMessagesToSyncCount(purchases: List<AppPurchaseDetails>): Int {
        return purchases.sumOf {
            if (it.isAcknowledged) {
                0
            } else {
                it.products.sumOf { pair ->
                    val identifier = pair.first
                    _messagesPacks.value.sumOf { pack ->
                        if (pack.identifier == identifier) {
                            pack.tokensCount
                        } else {
                            0
                        }
                    }
                }
            }
        }
    }

    private fun tokensFromPackId(identifier: String): Int {
        _messagesPacks.value.forEach {
            if (it.identifier == identifier) {
                return it.tokensCount
            }
        }
        return -1
    }

    private fun acknowledgeProduct(identifier: String, purchase: AppPurchaseDetails) {
        executeFlowResultUseCase({
            acknowledgeProductUseCase(
                purchaseDetails = purchase,
                consume = true
            )
        }, onSuccess = {
            Log.d("PURCHASED", "SUCCESS")
            updateMessageCount()
            _state.value = BuyTokensDialogState.Success(
                UiText.StringResource(
                    R.string.ai_conversation_presentation_success_buy,
                    tokensFromPackId(identifier)
                )
            )
        }, onFailure = { e ->
            Log.d("PURCHASED", "FAILURE")
            Log.e("PURCHASED", e.toString())
            updateMessageCount()
            _state.value = BuyTokensDialogState.Success(
                UiText.StringResource(
                    R.string.ai_conversation_presentation_success_buy,
                    tokensFromPackId(identifier)
                )
            )
            //          _isLoading.value = false
        })
    }

    private fun preprocessPurchase(identifier: String, purchase: AppPurchaseDetails) {

        executeFlowResultUseCase({
            preProcessPurchaseUseCase.invoke(
                orderId = purchase.orderId ?: "",
                purchaseToken = purchase.purchaseToken,
                productId = identifier,
            )
        }, onSuccess = {
            _state.value = BuyTokensDialogState.Success(
                UiText.StringResource(
                    R.string.ai_conversation_presentation_success_buy,
                    tokensFromPackId(identifier)
                )
            )

            acknowledgeProduct(identifier, purchase)
        }, onFailure = {
            _state.value = BuyTokensDialogState.Error(
                UiText.StringResource(R.string.ai_conversation_presentation_error_unknown)
            )
        })
    }

    private fun processPurchase(identifier: String, purchase: AppPurchaseDetails) {
        executeFlowResultUseCase({
            processPurchaseUseCase(
                userId = customer.user.uid!!,
                userToken = customer.user.token!!,
                orderId = purchase.orderId ?: "",
                purchaseToken = purchase.purchaseToken,
                productId = identifier,
            )
        }, onSuccess = {
            acknowledgeProduct(identifier, purchase)
        }, onFailure = {
            _state.value = BuyTokensDialogState.Error(
                UiText.StringResource(R.string.ai_conversation_presentation_error_unknown)
            )
            //   _isLoading.value = false
        })
    }

    private fun processPurchases(purchases: List<AppPurchaseDetails>) {
        if (!customer.isUserConnected()) {
            val count = getMessagesToSyncCount(purchases = purchases)
            if (count > 0) {
                customer.addPendingCoins(count)
            }
            return
        }

        _state.value = BuyTokensDialogState.Loading
        // _isLoading.value = purchases.isNotEmpty()

        purchases.forEach { purchase ->
            purchase.products.forEach { pair ->
                val identifier = pair.first
                viewModelScope.launch {
                    if (customer.isUserConnected()) {
                        processPurchase(identifier, purchase)
                    } else {
                        preprocessPurchase(identifier, purchase)
                    }
                }
            }
        }
    }

    private fun updateMessageCount() {
        if (customer.isUserConnected().not()) {
            return
        }

        executeFlowResultUseCase(
            useCase = {
                getUserAccountStateUseCase(
                    userId = customer.user.uid!!
                )
            },
            onSuccess = {
                _coinsState.value = BuyTokensCoinsDialogState.Loaded(it.messagesCount)
            },
            onFailure = {
                Log.d(
                    "BuyTokensDialogViewModel",
                    "Unable to fetch user messages coins. ${it.message}"
                )
            }
        )
    }


    private fun loadPacks() {
        _state.value = BuyTokensDialogState.Loading
        executeFlowResultUseCase(
            useCase = { getAiMessagesPacksUseCase() },
            onSuccess = { packs ->
                viewModelScope.launch {
                    try {
                        connectToGooglePlayUseCase().collect {
                            loadPrices(packs)
                        }
                    } catch (e: Exception) {
                        Log.e("BuyTokensDialogViewModel", "loadPacks: ${e.message}")
                    }
                }
            },
            onFailure = {
                _state.value =
                    BuyTokensDialogState.Error(message = UiText.StringResource(R.string.ai_conversation_error_network))
                Log.d("ConversationViewModel", "on Failure: ${it.message}")
            }
        )
    }


    private fun loadPrices(packs: List<AiMessagePack>) {
        executeFlowResultUseCase(
            useCase = {
                getInAppProductsUseCase(identifiers = packs.map { it.identifier })
            },
            onSuccess = { products ->
                _state.value = BuyTokensDialogState.Packs(packs.toMutableList())
                _messagesPacks.value = packs.map { pack ->
                    pack.copy(
                        productDetails = products.firstOrNull { product ->
                            product.productId == pack.identifier
                        }
                    )
                }.toMutableList()
            },
            onFailure = {
                Log.d("ConversationViewModel", "on Failure: ${it.message}")
            }
        )
    }


    internal fun onClickMessagePack(pack: AiMessagePack) {
        executeFlowResultUseCase(useCase = {
            launchBillingFlowUseCase(identifiers = listOf(pack.identifier))
        }, onSuccess = {

        }, onFailure = {
            Log.d("ConversationViewModel", "on Failure: ${it.message}")
        })
    }

    internal fun onTryClicked() {
        _state.value = BuyTokensDialogState.Loading
        if (!customer.isUserConnected()) {
            _state.value = BuyTokensDialogState.Login
            return
        }

        executeFlowResultUseCase({
            tryMessagePackUseCase(customer.user.uid!!, customer.user.token!!)
        }, onSuccess = {
            // -> update dialog state
            _state.value = BuyTokensDialogState.Success(
                UiText.StringResource(
                    R.string.ai_conversation_presentation_success_try,
                    7
                )
            )

            // -> update coins
            this.updateMessageCount()
        }, onFailure = {
            // -> not triable
            _state.value = BuyTokensDialogState.Error(
                UiText.StringResource(
                    R.string.ai_conversation_presentation_error_trial_consumed,
                    7
                )
            )
        })
    }


    internal fun displayListOfPacks() {
        _state.value = BuyTokensDialogState.Packs(this.messagesPacks.value)
    }

    internal fun cancelAction(action: BuyTokensDialogAbort) {
        when (action) {
            BuyTokensDialogAbort.Buy -> _state.value =
                BuyTokensDialogState.Packs(this.messagesPacks.value)

            else -> {}
        }
    }
}
