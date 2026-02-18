package com.purpletear.ai_conversation.ui.screens.home.components.characters_table


import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.sharedelements.utils.UiText
import com.purpletear.ai_conversation.domain.model.AiCharacter
import com.purpletear.ai_conversation.domain.usecase.DeleteCharacterUseCase
import com.purpletear.ai_conversation.presentation.R
import com.purpletear.ai_conversation.ui.common.utils.getRemoteAssetsUrl
import com.purpletear.core.presentation.extensions.executeFlowResultUseCase
import com.purpletear.core.presentation.extensions.executeFlowUseCase
import com.purpletear.core.presentation.services.MakeToastService
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.purpletear.sutoko.popup.domain.PopUpIconUrl
import fr.purpletear.sutoko.popup.domain.PopUpUserInteraction
import fr.purpletear.sutoko.popup.domain.SutokoPopUp
import fr.purpletear.sutoko.popup.domain.usecase.GetPopUpInteractionUseCase
import fr.purpletear.sutoko.popup.domain.usecase.ShowPopUpUseCase
import fr.purpletear.sutoko.shop.coinsLogic.Customer
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class CharactersTableViewModel @Inject constructor(
    private val customer: Customer,
    private val showPopUpUseCase: ShowPopUpUseCase,
    private val makeToastService: MakeToastService,
    private val deleteCharacterUseCase: DeleteCharacterUseCase,
    private val getPopUpInteractionUseCase: GetPopUpInteractionUseCase,
) : ViewModel() {

    private var _loadingUsers: MutableState<List<AiCharacter>> = mutableStateOf(emptyList())
    val loadingUsers: State<List<AiCharacter>> = _loadingUsers

    private var _tmpCharacterToRemove: MutableState<AiCharacter?> = mutableStateOf(null)

    init {
    }

    private fun onDismissDialog() {

    }

    private fun onConfirmDialog() {
        val character = _tmpCharacterToRemove.value ?: return

        addCharacterToLoadingList(character)

        executeFlowResultUseCase({
            delay(2128)
            deleteCharacterUseCase(
                userId = customer.user.uid!!,
                token = customer.user.token!!,
                aiCharacter = _tmpCharacterToRemove.value!!
            )
        }, onSuccess = {
            removeCharacterFromLoadingList(character)
        }, onFailure = {
            removeCharacterFromLoadingList(character)
        }, finally = {
            _tmpCharacterToRemove.value = null
        })
    }


    fun deleteCharacter(character: AiCharacter) {
        if (!customer.isUserConnected()) {
            makeToastService(R.string.ai_conversation_presentation_error_user_not_logged_in)
            return
        }

        _tmpCharacterToRemove.value = character

        val tag = showPopUpUseCase(
            popUp = SutokoPopUp(
                title = UiText.StringResource(R.string.ai_conversation_confirm_delete_title),
                icon = PopUpIconUrl(getRemoteAssetsUrl(character.avatarUrl ?: "")),
                iconHeight = 68.dp,
                buttonText = UiText.StringResource(fr.purpletear.sutoko.shop.presentation.R.string.sutoko_continue),
                // TODO
                description = UiText.StringResource(
                    R.string.ai_conversation_confirm_delete_description,
                    character.firstName
                ),
            )
        )


        executeFlowUseCase({
            getPopUpInteractionUseCase(tag)
        }, onStream = { interaction ->
            when (interaction.event) {
                PopUpUserInteraction.Dismiss -> onDismissDialog()
                PopUpUserInteraction.Confirm -> onConfirmDialog()
                else -> {}
            }
        }, onFailure = {
            Log.d("CharactersTableViewModel", it.toString())
        })
    }

    private fun addCharacterToLoadingList(character: AiCharacter) {
        val l = _loadingUsers.value.toMutableList()
        l.add(character)
        _loadingUsers.value = l
    }

    private fun removeCharacterFromLoadingList(character: AiCharacter) {
        val l = _loadingUsers.value.toMutableList()
        l.remove(character)
        _loadingUsers.value = l
    }
}