package com.purpletear.aiconversation.presentation.screens.media.image_generator.viewmodels

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.aiconversation.domain.enums.ProcessStatus
import com.purpletear.aiconversation.domain.model.ImageGenerationRequest
import com.purpletear.aiconversation.domain.usecase.GenerateImageFromPromptUseCase
import com.purpletear.aiconversation.domain.usecase.GetAiTokensStateUseCase
import com.purpletear.aiconversation.domain.usecase.GetCurrentImageGenerationRequestUseCase
import com.purpletear.aiconversation.presentation.R
import com.purpletear.aiconversation.presentation.usecase.OpenMessagesCoinsDialogUseCase
import com.purpletear.core.presentation.extensions.executeFlowResultUseCase
import com.purpletear.core.presentation.extensions.executeFlowUseCase
import com.purpletear.core.presentation.services.MakeToastService
import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.domain.usecase.OpenSignInPageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImageGeneratorViewModel @Inject constructor(
    private val makeToastService: MakeToastService,
    private val openSignInPageUseCase: OpenSignInPageUseCase,
    private val userRepository: UserRepository,
    private val getAiTokensStateUseCase: GetAiTokensStateUseCase,
    private val openMessagesCoinsDialogUseCase: OpenMessagesCoinsDialogUseCase,
    private val generateImageFromPromptUseCase: GenerateImageFromPromptUseCase,
    private val getCurrentImageGenerationRequestUseCase: GetCurrentImageGenerationRequestUseCase,
) : ViewModel() {
    val isUserConnected: StateFlow<Boolean> = userRepository
        .observeIsConnected()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false,
        )

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


    private var _currentImageRequest: MutableState<ImageGenerationRequest?> = mutableStateOf(null)
    val currentImageRequest: State<ImageGenerationRequest?> get() = _currentImageRequest
    val canUseImage: Boolean get() = _currentImageRequest.value != null

    init {
        observeCurrentImageGenerationRequest()
    }

    fun onResume() {
        reloadIsEnabled()
        viewModelScope.launch {
            getUserAccountState(1280L)
        }
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
            isUserConnected.value && _prompt.value.isNotBlank() && _prompt.value.trim().length > 3
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

    private suspend fun getUserAccountState(wait: Long = 1280L) {
        val user = userRepository.observeUser().first() ?: return

        _isCoinsLoading.value = true
        executeFlowResultUseCase({
            delay(wait)
            getAiTokensStateUseCase(
                userId = user.id,
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
        if (!isUserConnected.value) {
            onUserNotConnected()
            return
        }

        if (prompt.value.isBlank()) {
            makeToastService.invoke(R.string.ai_conversation_generate_image_prompt_required)
            return
        }

        viewModelScope.launch {
            val user = userRepository.observeUser().first()
            if (user == null) {
                onUserNotConnected()
                return@launch
            }
            executeFlowResultUseCase({
                generateImageFromPromptUseCase(
                    userId = user.id,
                    userToken = user.token,
                    prompt = prompt.value,
                )
            }, onFailure = {
                makeToastService.invoke(R.string.ai_conversation_presentation_error_unknown)
            }, finally = {
                viewModelScope.launch {
                    getUserAccountState(0L)
                }
            })
        }
    }
}