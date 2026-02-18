package com.purpletear.aiconversation.presentation.screens.character.add_character.viewmodels

import androidx.annotation.StringRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharedelements.utils.UiText
import com.purpletear.aiconversation.domain.enums.Gender
import com.purpletear.aiconversation.domain.enums.MediaType
import com.purpletear.aiconversation.domain.enums.ProcessStatus
import com.purpletear.aiconversation.domain.enums.Visibility
import com.purpletear.aiconversation.domain.model.AvatarBannerPair
import com.purpletear.aiconversation.domain.model.Style
import com.purpletear.aiconversation.domain.usecase.AddCharacterUseCase
import com.purpletear.aiconversation.domain.usecase.GetAllStylesUseCase
import com.purpletear.aiconversation.domain.usecase.GetAvatarAndBannerPairUseCase
import com.purpletear.aiconversation.domain.usecase.GetRandomAvatarAndBannerPairUseCase
import com.purpletear.aiconversation.domain.usecase.LoadCharactersUseCase
import com.purpletear.aiconversation.domain.usecase.ObserveCharactersUseCase
import com.purpletear.aiconversation.domain.usecase.SaveBitmapUseCase
import com.purpletear.aiconversation.domain.usecase.UploadMediaUseCase
import com.purpletear.aiconversation.presentation.R
import com.purpletear.aiconversation.presentation.screens.media.image_generator.state.ImageGeneratorState
import com.purpletear.aiconversation.presentation.sealed.NavigationEvent
import com.purpletear.core.presentation.extensions.executeFlowResultUseCase
import com.purpletear.core.presentation.extensions.executeFlowUseCase
import com.purpletear.core.presentation.services.MakeToastService
import com.purpletear.sutoko.user.usecase.IsUserConnectedUseCase
import com.purpletear.sutoko.user.usecase.OpenSignInPageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.purpletear.sutoko.popup.domain.PopUpIconDrawable
import fr.purpletear.sutoko.popup.domain.PopUpUserInteraction
import fr.purpletear.sutoko.popup.domain.SutokoPopUp
import fr.purpletear.sutoko.popup.domain.usecase.GetPopUpInteractionUseCase
import fr.purpletear.sutoko.popup.domain.usecase.ShowPopUpUseCase
import fr.purpletear.sutoko.shop.coinsLogic.Customer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AddCharacterViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getAllStylesUseCase: GetAllStylesUseCase,
    private val getRandomAvatarAndBannerPairUseCase: GetRandomAvatarAndBannerPairUseCase,
    private val getAvatarAndBannerPairUseCase: GetAvatarAndBannerPairUseCase,
    private val uploadMediaUseCase: UploadMediaUseCase,
    private val saveBitmapUseCase: SaveBitmapUseCase,
    private val addCharacterUseCase: AddCharacterUseCase,
    private val isUserConnectedUseCase: IsUserConnectedUseCase,
    private val openSignInPageUseCase: OpenSignInPageUseCase,
    private val makeToastService: MakeToastService,
    private val showPopUpUseCase: ShowPopUpUseCase,
    private val loadCharactersUseCase: LoadCharactersUseCase,
    private val observeCharactersUseCase: ObserveCharactersUseCase,
    private val observeInteractionUseCase: GetPopUpInteractionUseCase,
    private val customer: Customer,
) : ViewModel() {

    private var _characterDescription = mutableStateOf("")
    val characterDescription: State<String>
        get() = _characterDescription


    private var _isLoadingAvatarAndBannerPair: MutableState<Boolean> = mutableStateOf(false)
    val isLoadingAvatarAndBannerPair: State<Boolean>
        get() = _isLoadingAvatarAndBannerPair

    private var _characterGender: MutableState<Int> = mutableIntStateOf(1)
    val characterGender: State<Int>
        get() = _characterGender


    private var _avatarBannerPair: MutableState<AvatarBannerPair?> = mutableStateOf(null)
    val avatarBannerPair: State<AvatarBannerPair?>
        get() = _avatarBannerPair

    private var _importedAvatarBitmap: MutableState<ImageBitmap?> = mutableStateOf(null)
    val importedAvatarBitmap: State<ImageBitmap?>
        get() = _importedAvatarBitmap

    private var _selectedImageRequestSerialId: MutableState<String?> = mutableStateOf(null)

    val alert: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    private var _submitCharacterLoadingState: MutableState<ProcessStatus> =
        mutableStateOf(ProcessStatus.INITIAL)
    val submitCharacterLoadingState: State<ProcessStatus>
        get() = _submitCharacterLoadingState


    private var _isUserConnected: MutableState<Boolean> = mutableStateOf(false)
    val isUserConnected: Boolean get() = _isUserConnected.value


    private var _state: MutableState<ImageGeneratorState> =
        mutableStateOf(ImageGeneratorState.Loading)
    val state: ImageGeneratorState get() = _state.value

    private var _charactersStyles: MutableState<List<Style>> = mutableStateOf(emptyList())
    val charactersStyles: State<List<Style>> get() = _charactersStyles

    private var _selectedCharacterStyle = mutableStateOf<Style?>(null)
    val selectedCharacterStyle: State<Style?>
        get() = _selectedCharacterStyle

    private val _navigationEvents = MutableSharedFlow<NavigationEvent>(replay = 1)
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents


    init {
        observeIsUserConnected()
        getCharactersStyles()
    }

    fun onResume() {
        loadCharacters()
    }


    private fun loadCharacters() {
        if (!customer.isUserConnected()) {
            return
        }
        executeFlowResultUseCase({
            loadCharactersUseCase(customer.getUserId(), customer.getUserToken())
        })
        executeFlowUseCase({
            observeCharactersUseCase()
        }, onStream = {
            // TODO
            if (it.filter { l -> l.visibility == Visibility.Private }.size >= 10) {
                onLimitCharactersReached()
            }
        })
    }

    private fun onLimitCharactersReached() {
        val popUp = SutokoPopUp(
            title = UiText.StringResource(R.string.ai_conversation_pop_up_characters_limit_reached_title),
            description = UiText.StringResource(R.string.ai_conversation_pop_up_characters_limit_reached_description),
            icon = PopUpIconDrawable(fr.purpletear.sutoko.shop.presentation.R.drawable.account_creation_character),
            buttonText = UiText.StringResource(R.string.ai_conversation_continue)
        )
        val tag = showPopUpUseCase(popUp)

        executeFlowUseCase({
            observeInteractionUseCase(tag)
        }, onStream = { interaction ->
            when (interaction.event) {
                PopUpUserInteraction.Dismiss, PopUpUserInteraction.Confirm -> {
                    _navigationEvents.tryEmit(NavigationEvent.NavigateBack)
                }

                else -> {}
            }
        })
    }

    private fun getCharactersStyles() {
        executeFlowResultUseCase({
            getAllStylesUseCase()
        }, onSuccess = {
            _charactersStyles.value = it
            if (_selectedCharacterStyle.value == null) {
                _selectedCharacterStyle.value = it.firstOrNull()
            }
        })
    }

    fun toast(@StringRes message: Int) {
        makeToastService(message)
    }

    private fun observeIsUserConnected() {
        executeFlowUseCase({
            isUserConnectedUseCase()
        }, onStream = ::loadAccountState, onFailure = {
            reloadState()
            it.printStackTrace()
        })
    }

    private fun loadAccountState(isUserConnected: Boolean) {
        _isUserConnected.value = isUserConnected
        reloadState()
    }


    private fun reloadState() {
        if (!_isUserConnected.value) {
            _state.value = ImageGeneratorState.NotConnected
            return
        }

        _state.value = ImageGeneratorState.Success
    }

    fun bindNavigationChanges(savedStateHandle: SavedStateHandle?) {
        val imageRequestSerialId: String? = savedStateHandle?.get("imageRequestSerialId")
        imageRequestSerialId?.let {
            if (imageRequestSerialId != _selectedImageRequestSerialId.value && imageRequestSerialId.isNotBlank()) {
                savedStateHandle["imageRequestSerialId"] = null
                _selectedImageRequestSerialId.value = imageRequestSerialId
                _isLoadingAvatarAndBannerPair.value = true
                updateAvatarBannerPair(imageRequestSerialId)
            }
        }
    }

    private fun updateAvatarBannerPair(imageRequestSerialId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                getAvatarAndBannerPairUseCase(
                    imageRequestSerialId
                ).catch {
                    _isLoadingAvatarAndBannerPair.value = false
                }.collect { response ->
                    response.fold(
                        onSuccess = { pair ->
                            _importedAvatarBitmap.value = null
                            _avatarBannerPair.value = pair
                            _isLoadingAvatarAndBannerPair.value = false
                        },
                        onFailure = {
                            _isLoadingAvatarAndBannerPair.value = false
                        }
                    )
                }
            }
        }
    }

    fun signIn() {
        openSignInPageUseCase()
    }

    override fun onCleared() {
        super.onCleared()
        _importedAvatarBitmap.value = null
    }

    internal fun onSubmit(name: String, lastName: String) {
        if (!_isUserConnected.value) {
            signIn()
            return
        }

        if (_avatarBannerPair.value == null || _avatarBannerPair.value?.avatar == null) {
            makeToastService(R.string.ai_conversation_no_avatar_selected)
            return
        }

        _submitCharacterLoadingState.value = ProcessStatus.PROCESSING
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                addCharacterUseCase(
                    userId = customer.user.uid!!,
                    token = customer.user.token!!,
                    firstName = name,
                    lastName = lastName,
                    gender = if (_characterGender.value == Gender.Female.n) Gender.Female else Gender.Male,
                    description = characterDescription.value,
                    avatarId = _avatarBannerPair.value?.avatar?.id,
                    bannerId = _avatarBannerPair.value?.banner?.id,
                    styleId = selectedCharacterStyle.value?.id ?: 1,
                )
                    .catch {
                        _submitCharacterLoadingState.value = ProcessStatus.FAILED
                    }
                    .collect {
                        it.fold(
                            onSuccess = {

                                delay(3000)
                                _submitCharacterLoadingState.value = ProcessStatus.COMPLETED
                            },
                            onFailure = {
                                delay(3000)
                                _submitCharacterLoadingState.value = ProcessStatus.FAILED
                            }
                        )
                    }
            }
        }
    }

    private fun uploadAvatar(file: File) {
        executeFlowResultUseCase({
            uploadMediaUseCase(
                userId = customer.user.uid!!,
                userToken = customer.user.token!!,
                file = file,
                mediaType = MediaType.Avatar
            )
        }, onSuccess = { media ->
            _avatarBannerPair.value = AvatarBannerPair(
                avatar = media,
                banner = null
            )

        }, onFailure = {
            makeToastService(R.string.ai_conversation_file_rejected)
        }, finally = {
            _isLoadingAvatarAndBannerPair.value = false
            try {
                file.delete()
            } catch (_: Exception) {
            }
        })
    }

    internal fun onImageImported(image: ImageBitmap) {
        if (!_isUserConnected.value) {
            signIn()
            return
        }

        _isLoadingAvatarAndBannerPair.value = true
        _importedAvatarBitmap.value = image
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val fileResult = saveBitmapUseCase(image.asAndroidBitmap())
                fileResult.fold(
                    onSuccess = ::uploadAvatar,
                    onFailure = {
                        makeToastService(R.string.ai_conversation_file_rejected)
                    }
                )
            }
        }
    }

    internal fun onCharacterGenderChange(value: Int) {
        _characterGender.value = value
    }

    internal fun onRandomAvatarPressed() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoadingAvatarAndBannerPair.value = true
                getRandomAvatarAndBannerPairUseCase(
                    isFemale = _characterGender.value == 1
                ).catch {
                    _isLoadingAvatarAndBannerPair.value = false
                }.collect { response ->

                    response.fold(
                        onSuccess = { it ->
                            _importedAvatarBitmap.value = null
                            _avatarBannerPair.value = it
                            _isLoadingAvatarAndBannerPair.value = false
                        },
                        onFailure = {
                            // TODO
                            // ERROR MESSAGE
                            _isLoadingAvatarAndBannerPair.value = false
                        }
                    )

                }
            }
        }
    }

    internal fun onCharacterStyleSelected(style: Style) {
        _selectedCharacterStyle.value = style
    }

    internal fun onCharacterDescriptionChange(value: String) {
        _characterDescription.value = value
    }
}