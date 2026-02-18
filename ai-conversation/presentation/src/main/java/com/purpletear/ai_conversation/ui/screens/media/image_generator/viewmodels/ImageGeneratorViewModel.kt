package com.purpletear.ai_conversation.ui.screens.media.image_generator.viewmodels

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.purpletear.ai_conversation.domain.enums.ProcessStatus
import com.purpletear.ai_conversation.domain.model.ImageGenerationRequest
import com.purpletear.ai_conversation.domain.usecase.GenerateImageFromPromptUseCase
import com.purpletear.ai_conversation.domain.usecase.GetCurrentImageGenerationRequestUseCase
import com.purpletear.ai_conversation.presentation.R
import com.purpletear.core.presentation.extensions.executeFlowResultUseCase
import com.purpletear.core.presentation.extensions.executeFlowUseCase
import com.purpletear.core.presentation.services.MakeToastService
import com.purpletear.shop.domain.usecase.GetUserAccountStateUseCase
import com.purpletear.shop.domain.usecase.OpenMessagesCoinsDialogUseCase
import com.purpletear.sutoko.user.usecase.IsUserConnectedUseCase
import com.purpletear.sutoko.user.usecase.OpenSignInPageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.purpletear.sutoko.shop.coinsLogic.Customer
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class ImageGeneratorViewModel @Inject constructor(
    private val customer: Customer,
    private val makeToastService: MakeToastService,
    private val openSignInPageUseCase: OpenSignInPageUseCase,
    private val isUserConnectedUseCase: IsUserConnectedUseCase,
    private val getUserAccountStateUseCase: GetUserAccountStateUseCase,
    private val openMessagesCoinsDialogUseCase: OpenMessagesCoinsDialogUseCase,
    private val generateImageFromPromptUseCase: GenerateImageFromPromptUseCase,
    private val getCurrentImageGenerationRequestUseCase: GetCurrentImageGenerationRequestUseCase,
) : ViewModel() {
    private val _prompt: MutableState<String> = mutableStateOf("")
    val prompt: State<String> get() = _prompt

    private var _isLoading: MutableState<Boolean> = mutableStateOf(false)
    val isLoading: State<Boolean> get() = _isLoading
    private var _isEnabled: MutableState<Boolean> = mutableStateOf(false)
    val isEnabled: State<Boolean> get() = _isEnabled

    private var _price: MutableState<Int> = mutableIntStateOf(5)
    val price: State<Int> get() = _price

    private var _coins: MutableState<Int?> = mutableStateOf(null)
    val coins: State<Int?> get() = _coins

    private var _isCoinsLoading: MutableState<Boolean> = mutableStateOf(false)
    val isCoinsLoading: State<Boolean> get() = _isCoinsLoading

    private var _isUserConnected: MutableState<Boolean> = mutableStateOf(customer.isUserConnected())
    val isUserConnected: State<Boolean> get() = _isUserConnected

    private var _currentImageRequest: MutableState<ImageGenerationRequest?> = mutableStateOf(null)
    val currentImageRequest: State<ImageGenerationRequest?> get() = _currentImageRequest
    val canUseImage: Boolean get() = _currentImageRequest.value != null

    init {
        observeSignIn()
        observeCurrentImageGenerationRequest()
    }

    fun onResume() {
        reloadIsEnabled()
        getUserAccountState(1280L)
    }

    private fun observeSignIn() {
        executeFlowUseCase({
            isUserConnectedUseCase()
        }, onStream = {
            _isUserConnected.value = it
        })
    }

    private fun onUserNotConnected() {
        openSignInPageUseCase()
    }

    fun onPromptTextChanged(text: String) {
        _prompt.value = text.trimStart().replace("  ", " ")
        reloadIsEnabled()
    }

    private fun reloadIsEnabled() {
        _isEnabled.value =
            customer.isUserConnected() && _prompt.value.isNotBlank() && _prompt.value.trim().length > 3
    }

    private fun observeCurrentImageGenerationRequest() {
        executeFlowUseCase({
            getCurrentImageGenerationRequestUseCase()
        }, onStream = { request ->
            _currentImageRequest.value = request
            if (request != null) {
                _prompt.value = request.prompt
                reloadIsEnabled()
            }
            _isLoading.value = request?.status == ProcessStatus.PROCESSING.code
        })
    }

    private fun getUserAccountState(wait: Long = 1280L) {
        _isCoinsLoading.value = true
        executeFlowResultUseCase({
            delay(wait)
            getUserAccountStateUseCase(
                userId = customer.getUserId(),
            )
        }, onSuccess = {
            _coins.value = it.messagesCount
        }, finally = {
            _isCoinsLoading.value = false
        })
    }

    fun openBuyTokensDialog() {
        openMessagesCoinsDialogUseCase()
    }

    fun onClickGenerateImage() {
        if (!customer.isUserConnected()) {
            onUserNotConnected()
            return
        }

        if (prompt.value.isBlank()) {
            makeToastService.invoke(R.string.ai_conversation_generate_image_prompt_required)
            return
        }

        executeFlowResultUseCase({
            generateImageFromPromptUseCase(
                userId = customer.getUserId(),
                userToken = customer.getUserToken(),
                prompt = prompt.value,
            )
        }, onFailure = {
            makeToastService.invoke(R.string.ai_conversation_presentation_error_unknown)
        }, finally = {
            getUserAccountState(0L)
        })
    }
}