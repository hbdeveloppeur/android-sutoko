package com.purpletear.aiconversation.presentation.screens.conversation.viewmodels


import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.sharedelements.utils.UiText
import com.purpletear.aiconversation.domain.model.AiCharacter
import com.purpletear.aiconversation.domain.model.AiCharacterWithStatus
import com.purpletear.aiconversation.domain.model.messages.Conversation
import com.purpletear.aiconversation.domain.model.messages.entities.MessageInviteCharacters
import com.purpletear.aiconversation.domain.usecase.AddMessageToConversationUseCase
import com.purpletear.aiconversation.domain.usecase.GetAllCharactersWithStatusUseCase
import com.purpletear.aiconversation.domain.usecase.GetConversationSettingsUseCase
import com.purpletear.aiconversation.domain.usecase.InviteCharactersUseCase
import com.purpletear.aiconversation.presentation.R
import com.purpletear.core.presentation.extensions.executeFlowResultUseCase
import com.purpletear.core.presentation.extensions.executeFlowUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.purpletear.sutoko.popup.domain.PopUpIconDrawable
import fr.purpletear.sutoko.popup.domain.PopUpUserInteraction
import fr.purpletear.sutoko.popup.domain.SutokoPopUp
import fr.purpletear.sutoko.popup.domain.usecase.GetPopUpInteractionUseCase
import fr.purpletear.sutoko.popup.domain.usecase.ShowPopUpUseCase
import fr.purpletear.sutoko.shop.coinsLogic.Customer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class InviteCharacterViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getAllCharactersWithStatusUseCase: GetAllCharactersWithStatusUseCase,
    private val inviteCharactersUseCase: InviteCharactersUseCase,
    private val addMessageToConversationUseCase: AddMessageToConversationUseCase,
    private val getConversationSettingsUseCase: GetConversationSettingsUseCase,
    private val showPopUpUseCase: ShowPopUpUseCase,
    private val interactionUseCase: GetPopUpInteractionUseCase,
    private val customer: Customer,
) : ViewModel() {

    private var _isLoading: MutableState<Boolean> = mutableStateOf(true)
    val isLoading: State<Boolean> get() = _isLoading

    private var _characters: MutableState<List<AiCharacterWithStatus>> =
        mutableStateOf(listOf())
    val characters: State<List<AiCharacterWithStatus>> get() = _characters

    private var _selectedIds = mutableStateOf(listOf<Int>())
    val selectedIds: State<List<Int>> = _selectedIds

    private var _isInviteCharacterLoading: MutableState<Boolean> = mutableStateOf(false)
    val isInviteCharacterLoading: State<Boolean> get() = _isInviteCharacterLoading
    private var _conversationSettings: MutableState<Conversation?> = mutableStateOf(
        null
    )
    private var aiCharacterId: Int = 1

    val conversationSettings: State<Conversation?>
        get() = _conversationSettings


    init {
        aiCharacterId = savedStateHandle.get<Int>("character_id") ?: 1
        getCharacters()
    }

    internal fun characterAlreadyInConversation(character: AiCharacter): Boolean {
        return conversationSettings.value?.characters?.any { it.id == character.id } == true
    }

    internal fun loadCharacters() {
        getConversationSettings()
    }


    private fun onUserNotConnected() {
        // TODO
    }

    internal fun confirmPopUp() {
        if (!customer.isUserConnected()) {
            onUserNotConnected()
            return
        }

        _isInviteCharacterLoading.value = true
        executeFlowResultUseCase({
            inviteCharactersUseCase(
                userId = customer.user.uid!!,
                conversationCharacterId = aiCharacterId,
                characters = _characters.value.map {
                    it.character
                }.filter {
                    _selectedIds.value.contains(it.id)
                }
            )
        }, onSuccess = {
            insertInviteMessage()
            _isInviteCharacterLoading.value = false
            closeComposable()
        }, onFailure = {
            _isInviteCharacterLoading.value = false
            it.printStackTrace()
            Log.e("InviteCharacterViewModel", "onFailure: ${it.message}")
        })
    }

    private fun getConversationSettings() {
        if (!customer.isUserConnected()) {
            onUserNotConnected()
            return
        }

        _isLoading.value = true
        executeFlowUseCase(

            useCase = {
                getConversationSettingsUseCase(customer.user.uid!!, aiCharacterId)
            },

            onStream = { conversation ->
                _conversationSettings.value = conversation
                _isLoading.value = false
            },

            onFailure = {
                _isLoading.value = false
                Log.d("ConversationViewModel", "onFailure: ${it.message}")
            }

        )
    }

    fun onCharacterSelected(characterId: Int) {
        if (_selectedIds.value.contains(characterId)) {
            _selectedIds.value = _selectedIds.value.minus(characterId)
        } else {
            _selectedIds.value = _selectedIds.value.plus(characterId)
        }
    }

    fun names(): String {
        return _characters.value.filter {
            _selectedIds.value.contains(it.character.id)
        }.joinToString(", ") { it.character.firstName }
    }

    private val _closeComposable = MutableStateFlow(false)
    val closeComposable: StateFlow<Boolean> = _closeComposable

    internal fun closeComposable() {
        _closeComposable.value = true
    }

    fun resetCloseComposable() {
        _closeComposable.value = false
    }

    private fun getCharacters() {
        if (!customer.isUserConnected()) {
            return
        }
        _isLoading.value = true
        executeFlowResultUseCase({
            getAllCharactersWithStatusUseCase(
                userId = customer.user.uid!!
            )
        }, onSuccess = {
            _characters.value = it
            _isLoading.value = false
        }, onFailure = {
            _isLoading.value = false
            it.printStackTrace()
            Log.e("InviteCharacterViewModel", "onFailure: ${it.message}")
        })
    }

    internal fun inviteSelectedCharacters() {
        if (_characters.value.isEmpty() || _selectedIds.value.isEmpty() || _isInviteCharacterLoading.value) {
            return
        }

        val tag = showPopUpUseCase(
            SutokoPopUp(
                title = UiText.StringResource(R.string.ai_conversation_confirm_invite_title),
                icon = PopUpIconDrawable(fr.purpletear.sutoko.shop.presentation.R.drawable.account_creation_character),
                iconHeight = null,
                buttonText = UiText.StringResource(R.string.ai_conversation_invite),
                description = UiText.StringResource(
                    R.string.ai_conversation_confirm_invite_description,
                    names()
                ),
            )
        )

        executeFlowUseCase({
            interactionUseCase(tag)
        }, onStream = { interaction ->
            if (interaction.event is PopUpUserInteraction.Confirm) {
                confirmPopUp()
            }
        })
    }

    private fun insertInviteMessage() {
        val characters = _characters.value.filter {
            _selectedIds.value.contains(it.character.id)
        }
        addMessageToConversationUseCase(
            message = MessageInviteCharacters(characters = characters.map { v -> v.character })
        )
    }
}