package fr.purpletear.sutoko.screens.params

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharedelements.utils.UiText
import com.google.firebase.auth.FirebaseAuth
import com.purpletear.core.presentation.extensions.executeFlowUseCase
import com.purpletear.core.presentation.services.MakeToastService
import com.purpletear.sutoko.domain.repository.AccountRepository
import com.purpletear.sutoko.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.purpletear.sutoko.BuildConfig
import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.popup.domain.AlertPopUp
import fr.purpletear.sutoko.popup.domain.PopUpUserInteraction
import fr.purpletear.sutoko.popup.domain.usecase.GetPopUpInteractionUseCase
import fr.purpletear.sutoko.popup.domain.usecase.ShowPopUpUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SutokoParamsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val accountRepository: AccountRepository,
    private val showPopUpUseCase: ShowPopUpUseCase,
    private val getPopUpInteractionUseCase: GetPopUpInteractionUseCase,
    private val toastService: MakeToastService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SutokoParamsUiState(
            versionText = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        )
    )
    val uiState: StateFlow<SutokoParamsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.observeIsConnected().collect { isConnected ->
                _uiState.update { it.copy(isUserConnected = isConnected) }
            }
        }
    }

    fun onEvent(event: SutokoParamsEvent) {
        when (event) {
            is SutokoParamsEvent.OnPrivacyPressed -> emitPrivacyEffect()
            is SutokoParamsEvent.OnSharePressed -> emitEffect(SutokoParamsEffect.ShareApp)
            is SutokoParamsEvent.OnReloadPressed -> confirmReloadAccountData()
            is SutokoParamsEvent.OnDeletePressed -> confirmDeleteAccount()
            is SutokoParamsEvent.OnDisconnectPressed -> confirmDisconnect()
            is SutokoParamsEvent.OnBackPressed -> emitEffect(SutokoParamsEffect.NavigateBack)
            is SutokoParamsEvent.OnEffectConsumed -> _uiState.update { it.copy(effect = null) }
        }
    }

    fun setPrivacyPolicyUrl(url: String) {
        _uiState.update { it.copy(privacyPolicyUrl = url) }
    }

    private fun emitPrivacyEffect() {
        val url = _uiState.value.privacyPolicyUrl
        if (url.isNotBlank()) {
            emitEffect(SutokoParamsEffect.OpenPrivacyPolicy(url))
        }
    }

    private fun confirmReloadAccountData() {
        showConfirmation(
            title = UiText.StringResource(R.string.sutoko_params_activity_reload_my_account_data_confirm),
            onConfirm = {
                _uiState.update { it.copy(isReloadLoading = true) }
                // No remote API is wired for reload yet; mirror legacy behavior.
                viewModelScope.launch {
                    kotlinx.coroutines.delay(500)
                    _uiState.update { it.copy(isReloadLoading = false) }
                    toastService(R.string.sutoko_params_account_data_reloaded_success)
                }
            }
        )
    }

    private fun confirmDeleteAccount() {
        showConfirmation(
            title = UiText.StringResource(R.string.sutoko_params_activity_delete_my_account_data_confirm),
            onConfirm = ::deleteAccount
        )
    }

    private fun confirmDisconnect() {
        showConfirmation(
            title = UiText.StringResource(R.string.sutoko_disconnect),
            onConfirm = ::disconnect
        )
    }

    private fun showConfirmation(title: UiText, onConfirm: () -> Unit) {
        val tag = showPopUpUseCase(AlertPopUp(title = title))
        executeFlowUseCase(
            useCase = { getPopUpInteractionUseCase(tag) },
            onStream = { event ->
                if (event.event == PopUpUserInteraction.Confirm) {
                    onConfirm()
                }
            },
            onFailure = { /* Dialog was dismissed; no-op. */ }
        )
    }

    private fun deleteAccount() {
        viewModelScope.launch {
            val user = userRepository.observeUser().first()
            if (user == null) {
                toastService(R.string.sutoko_error_you_must_be_connected)
                return@launch
            }

            _uiState.update { it.copy(isDeleteLoading = true) }
            accountRepository.requestAccountDeletion(userId = user.id)
                .fold(
                    onSuccess = {
                        disconnect()
                        toastService(R.string.account_deleted)
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(isDeleteLoading = false) }
                        toastService(R.string.sutoko_error_unknown)
                        error.printStackTrace()
                    }
                )
        }
    }

    private fun disconnect() {
        viewModelScope.launch {
            FirebaseAuth.getInstance().signOut()
            userRepository.disconnect()
            _uiState.update { it.copy(isDeleteLoading = false) }
            toastService(R.string.sutoko_params_activity_disconnect_success)
        }
    }

    private fun emitEffect(effect: SutokoParamsEffect) {
        _uiState.update { it.copy(effect = effect) }
    }
}
