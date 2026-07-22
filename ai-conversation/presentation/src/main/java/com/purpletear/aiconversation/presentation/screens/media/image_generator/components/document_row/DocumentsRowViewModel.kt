package com.purpletear.aiconversation.presentation.screens.media.image_generator.components.document_row


import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.aiconversation.domain.model.Document
import com.purpletear.aiconversation.domain.usecase.CreateDocumentUseCase
import com.purpletear.aiconversation.domain.usecase.LoadImageGenerationRequestsRepository
import com.purpletear.aiconversation.domain.usecase.ObserveImageGenerationDocuments
import com.purpletear.aiconversation.domain.usecase.ObserveSelectedDocumentUseCase
import com.purpletear.aiconversation.domain.usecase.SelectDocumentUseCase
import com.purpletear.core.presentation.extensions.executeFlowResultUseCase
import com.purpletear.core.presentation.extensions.executeFlowUseCase
import com.purpletear.sutoko.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DocumentsRowViewModel @Inject constructor(
    private val loadImageGenerationRequestsRepository: LoadImageGenerationRequestsRepository,
    private val observeImageGenerationDocuments: ObserveImageGenerationDocuments,
    private val observeSelectedDocument: ObserveSelectedDocumentUseCase,
    private val selectDocumentUseCase: SelectDocumentUseCase,
    private val createDocumentUseCase: CreateDocumentUseCase,
    private val userRepository: UserRepository,
) : ViewModel() {

    private var _documents: MutableState<List<Document>> = mutableStateOf(emptyList())
    val documents: MutableState<List<Document>> get() = _documents

    private var _selectedDocumentId: MutableState<String?> = mutableStateOf(null)
    val selectedDocumentId: MutableState<String?> get() = _selectedDocumentId

    init {
        viewModelScope.launch { observeConnection() }
        viewModelScope.launch { observeDocuments() }
    }

    private suspend fun observeConnection() {
        userRepository.observeIsConnected().collect { isConnected ->
            if (isConnected) {
                loadDocuments()
            }
        }
    }

    internal fun onClickDocument(document: Document) {
        _selectedDocumentId.value = document.serial
        selectDocumentUseCase(document)
    }

    internal fun onClickNewDocument() {
        createDocumentUseCase()
    }

    private fun onUserNotConnected() {
        // TODO
    }

    private suspend fun loadDocuments() {
        val user = userRepository.observeUser().first()
        if (null == user) {
            onUserNotConnected()
            return
        }

        executeFlowResultUseCase({
            loadImageGenerationRequestsRepository(
                userId = user.id,
                userToken = user.token,
            )
        }, onSuccess = {

        })
    }


    private fun observeDocuments() {
        executeFlowUseCase({
            observeImageGenerationDocuments()
        }, onStream = { documents ->
            if (selectedDocumentId.value == null && documents.isNotEmpty()) {
                selectedDocumentId.value = documents[0].serial
            }
            _documents.value = documents
        }, onFailure = {
            Log.e("DocumentsRowViewModel", it.toString())
        })

        executeFlowUseCase({
            observeSelectedDocument()
        }, onStream = { document ->
            _selectedDocumentId.value = document?.serial
        })
    }
}