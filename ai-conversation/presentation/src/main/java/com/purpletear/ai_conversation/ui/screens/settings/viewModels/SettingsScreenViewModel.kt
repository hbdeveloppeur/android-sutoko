package com.purpletear.ai_conversation.ui.screens.settings.viewModels


import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.sharedelements.utils.UiText
import com.purpletear.ai_conversation.domain.model.Version
import com.purpletear.ai_conversation.domain.repository.MessageQueue
import com.purpletear.ai_conversation.domain.usecase.GetVersionUseCase
import com.purpletear.ai_conversation.domain.usecase.RestartConversationUseCase
import com.purpletear.ai_conversation.presentation.R
import com.purpletear.core.presentation.extensions.executeFlowResultUseCase
import com.purpletear.core.presentation.extensions.executeFlowUseCase
import com.purpletear.core.presentation.services.MakeToastService
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.purpletear.sutoko.popup.domain.AlertPopUp
import fr.purpletear.sutoko.popup.domain.PopUpUserInteraction
import fr.purpletear.sutoko.popup.domain.usecase.GetPopUpInteractionUseCase
import fr.purpletear.sutoko.popup.domain.usecase.ShowPopUpUseCase
import fr.purpletear.sutoko.shop.coinsLogic.Customer
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
internal class SettingsScreenViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getVersionUseCase: GetVersionUseCase,
    private val restartConversationUseCase: RestartConversationUseCase,
    private val showPopUpUseCase: ShowPopUpUseCase,
    private val popUpInteractionUseCase: GetPopUpInteractionUseCase,
    private val messageQueue: MessageQueue,
    private val customer: Customer,
    private val toastService: MakeToastService,

    ) : ViewModel() {

    private var _isLoading: MutableState<Boolean> = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private var _isDeleted: MutableState<Boolean> = mutableStateOf(false)
    val isDeleted: State<Boolean> = _isDeleted

    private var _isRefreshing: MutableState<Boolean> = mutableStateOf(false)
    val isRefreshing: State<Boolean> = _isRefreshing

    private var _currentVersion: MutableState<Version?> = mutableStateOf(null)
    val currentVersion: State<Version?> = _currentVersion

    private var _nextVersion: MutableState<Version?> = mutableStateOf(null)
    val nextVersion: State<Version?> = _nextVersion

    private var aiCharacterId: Int = 1

    init {
        aiCharacterId = savedStateHandle.get<Int>("character_id") ?: 1
        getVersion()
    }

    internal fun onRestartConversationPressed() {
        val tag = showPopUpUseCase(
            AlertPopUp(
                title = UiText.StringResource(R.string.ai_conversation_restart_title),
            )
        )

        executeFlowUseCase({
            popUpInteractionUseCase(tag)
        }, onStream = { interaction ->
            if (interaction.event is PopUpUserInteraction.Confirm) {
                restartConversation()
            }
        }, onFailure = {
            // Failure
            Log.d("ConversationViewModel", "on Failure: ${it.message}")
        })
    }

    private fun onUserNotConnected() {
        toastService(R.string.ai_conversation_you_are_not_connected)
    }

    internal fun onResume() {
        _isDeleted.value = false
    }


    private fun restartConversation() {
        if (!customer.isUserConnected()) {
            onUserNotConnected()
            return
        }
        messageQueue.clear()
        _isLoading.value = true
        executeFlowResultUseCase(
            useCase = {
                delay(1000L)
                // TODO
                restartConversationUseCase(
                    userId = customer.user.uid!!,
                    // TODO
                    aiCharacterId = aiCharacterId,
                )
            },
            onSuccess = {
                _isLoading.value = false
                _isDeleted.value = true
            },
            onFailure = {
                Log.d("ConversationViewModel", "onFailure: ${it.message}")
                _isLoading.value = false
            }
        )
    }

    internal fun refreshVersion() {
        _isRefreshing.value = true
        getVersion()
    }

    private fun getVersion() {
        executeFlowResultUseCase(
            useCase = {
                delay(2000L)
                getVersionUseCase()
            },
            onSuccess = {
                _isRefreshing.value = false
                _currentVersion.value = it.current
                _nextVersion.value = it.next
            },
            onFailure = {
                Log.d("ConversationViewModel", "onFailure: ${it.message}")
            }
        )
    }
}