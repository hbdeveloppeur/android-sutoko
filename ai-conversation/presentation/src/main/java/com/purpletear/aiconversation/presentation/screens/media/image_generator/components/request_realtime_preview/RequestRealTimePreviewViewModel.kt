package com.purpletear.aiconversation.presentation.screens.media.image_generator.components.request_realtime_preview


import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.purpletear.aiconversation.domain.enums.Direction
import com.purpletear.aiconversation.domain.model.Document
import com.purpletear.aiconversation.domain.usecase.DeleteSelectedImageGenerationRequestUseCase
import com.purpletear.aiconversation.domain.usecase.MoveDocumentCursorUseCase
import com.purpletear.aiconversation.domain.usecase.ObserveSelectedDocumentUseCase
import com.purpletear.aiconversation.presentation.R
import com.purpletear.aiconversation.presentation.common.utils.getDocumentImageUrl
import com.purpletear.core.image_downloader.ImageDownloader
import com.purpletear.core.presentation.extensions.executeFlowResultUseCase
import com.purpletear.core.presentation.extensions.executeFlowUseCase
import com.purpletear.core.presentation.services.MakeToastService
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.purpletear.sutoko.shop.coinsLogic.Customer
import javax.inject.Inject

@HiltViewModel
class RequestRealTimePreviewViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val customer: Customer,
    private val imageDownloader: ImageDownloader,
    private val makeToastService: MakeToastService,
    private val moveDocumentCursorUseCase: MoveDocumentCursorUseCase,
    private val observeSelectedDocumentUse: ObserveSelectedDocumentUseCase,
    private val deleteSelectedImageGenerationRequestUseCase: DeleteSelectedImageGenerationRequestUseCase,
) : ViewModel() {

    private val _selectedDocument: MutableState<Document?> = mutableStateOf(null)
    val selectedDocument: MutableState<Document?> get() = _selectedDocument

    private val _isLoading: MutableState<Boolean> = mutableStateOf(false)
    val isLoading: MutableState<Boolean> get() = _isLoading

    init {
        observeSelectedDocument()
    }

    private fun observeSelectedDocument() {
        executeFlowUseCase({
            observeSelectedDocumentUse()
        }, onStream = { document ->
            _selectedDocument.value = document
        })
    }

    fun onClickNext() {
        moveDocumentCursorUseCase(Direction.Forward)
    }

    fun onClickPrevious() {
        moveDocumentCursorUseCase(Direction.Backward)
    }

    fun onClickDownloadImage() {
        if (_selectedDocument.value == null) {
            return
        }

        val url = getDocumentImageUrl(_selectedDocument.value!!)

        url?.let {
            executeFlowResultUseCase({
                imageDownloader.download(it)
            }, onSuccess = {
                makeToastService(R.string.ai_conversation_image_saved_to_image_gallery)
            })
        }
    }

    fun onClickDeleteImage() {
        if (!customer.isUserConnected()) {
            makeToastService(R.string.ai_conversation_you_are_not_connected)
            return
        }
        _isLoading.value = true
        executeFlowResultUseCase({
            deleteSelectedImageGenerationRequestUseCase(
                userId = customer.getUserId(),
                userToken = customer.getUserToken(),
            )
        }, finally = {
            _isLoading.value = false
        })
    }
}