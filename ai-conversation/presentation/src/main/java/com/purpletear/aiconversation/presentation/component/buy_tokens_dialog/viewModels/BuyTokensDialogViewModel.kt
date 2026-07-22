package com.purpletear.aiconversation.presentation.component.buy_tokens_dialog.viewModels


import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.purpletear.aiconversation.domain.model.AiMessagePack
import com.purpletear.aiconversation.presentation.component.buy_tokens_dialog.states.BuyTokensCoinsDialogState
import com.purpletear.aiconversation.presentation.component.buy_tokens_dialog.states.BuyTokensDialogState
import com.purpletear.aiconversation.presentation.component.buy_tokens_dialog.states.BuyTokensDialogTitleState
import com.purpletear.aiconversation.presentation.usecase.CloseMessagesCoinsDialogUseCase
import com.purpletear.aiconversation.presentation.usecase.ObserveMessageCoinsDialogVisibilityUseCase
import com.purpletear.core.presentation.extensions.executeFlowUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BuyTokensDialogViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getMessageCoinsDialogVisibilityUseCase: ObserveMessageCoinsDialogVisibilityUseCase,
    private val closeMessagesCoinsDialogUseCase: CloseMessagesCoinsDialogUseCase,
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


    init {

        executeFlowUseCase({
            getMessageCoinsDialogVisibilityUseCase()
        }, onStream = {
            _isVisible.value = it
        }, onFailure = {
            Log.e("BuyTokensDialogViewModel", it.toString())
        })
    }


    fun close() {
        closeMessagesCoinsDialogUseCase()
    }

    fun onResume() {

    }
}
