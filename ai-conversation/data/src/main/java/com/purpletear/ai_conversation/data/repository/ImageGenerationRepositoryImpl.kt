package com.purpletear.ai_conversation.data.repository


import com.purpletear.ai_conversation.data.BuildConfig
import com.purpletear.ai_conversation.data.exception.NoResponseException
import com.purpletear.ai_conversation.data.remote.ImageGenerationApi
import com.purpletear.ai_conversation.data.remote.dto.toDomain
import com.purpletear.ai_conversation.data.remote.utils.ApiFailureResponseHandler
import com.purpletear.ai_conversation.domain.enums.Direction
import com.purpletear.ai_conversation.domain.enums.ProcessStatus
import com.purpletear.ai_conversation.domain.model.Document
import com.purpletear.ai_conversation.domain.model.ImageGenerationRequest
import com.purpletear.ai_conversation.domain.model.ImageGeneratorSettings
import com.purpletear.ai_conversation.domain.model.Media
import com.purpletear.ai_conversation.domain.model.hasNext
import com.purpletear.ai_conversation.domain.model.hasPrevious
import com.purpletear.ai_conversation.domain.repository.ImageGenerationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class ImageGenerationRepositoryImpl(
    private val api: ImageGenerationApi,
) : ImageGenerationRepository {

    private val _documents = MutableStateFlow<List<Document>>(listOf())
    override val documents: StateFlow<List<Document>>
        get() = _documents

    private val _selectedDocument = MutableStateFlow<Document?>(null)
    override val selectedDocument: StateFlow<Document?>
        get() = _selectedDocument

    private val _currentImageRequest = MutableStateFlow<ImageGenerationRequest?>(null)
    override val currentImageRequest: StateFlow<ImageGenerationRequest?>
        get() = _currentImageRequest

    private fun setCurrentImageRequest() {
        if (null == _selectedDocument.value) {
            _currentImageRequest.value = null
            return
        }
        _currentImageRequest.value =
            _selectedDocument.value!!.requests.getOrNull(_selectedDocument.value!!.cursor)
    }

    override fun selectedDocument(document: Document) {
        _selectedDocument.value =
            document.copy(cursor = (document.requests.size - 1).coerceAtLeast(0))

        setCurrentImageRequest()
    }

    override fun moveDocumentCursor(direction: Direction) {
        if (direction == Direction.Forward && _selectedDocument.value?.hasNext() == false) {
            return
        } else if (direction == Direction.Backward && _selectedDocument.value?.hasPrevious() == false) {
            return
        }
        _selectedDocument.value = when (direction) {
            Direction.Forward -> _selectedDocument.value?.copy(
                cursor = _selectedDocument.value?.cursor?.plus(
                    1
                ) ?: 0
            )

            Direction.Backward -> _selectedDocument.value?.copy(
                cursor = _selectedDocument.value?.cursor?.minus(
                    1
                )?.coerceAtLeast(0) ?: 0
            )
        }
        setCurrentImageRequest()
        updateDocuments()
    }

    private fun updateDocuments() {
        _documents.value = _documents.value.map { document ->
            if (_selectedDocument.value != null && document.serial == _selectedDocument.value?.serial) {
                _selectedDocument.value!!
            } else {
                document
            }
        }.filter {
            it.requests.isNotEmpty()
        }
    }

    override suspend fun loadDocuments(
        userId: String,
        userToken: String,
    ): Flow<Result<Unit>> = flow {
        val apiResponse = api.getAllRequests(
            userId = userId,
            token = userToken,
            appVersion = BuildConfig.VERSION_NAME,
        )

        if (apiResponse.isSuccessful) {
            apiResponse.body()?.let { response ->
                _documents.value = response.toDomain()
                _selectedDocument.value = _documents.value.firstOrNull()
                setCurrentImageRequest()
                emit(Result.success(Unit))
            } ?: run {
                emit(Result.failure(NoResponseException()))
                return@flow
            }
        } else {
            val exception = ApiFailureResponseHandler.handler(apiResponse.errorBody())
            emit(Result.failure(exception))
        }
    }.catch {
        emit(Result.failure(it))
    }

    override fun canCreateNewDocument(): Boolean {
        return _documents.value.none {
            it.requests.isEmpty()
        }
    }

    override fun createNewDocument(request: ImageGenerationRequest?): Document {
        val d = Document(requests = if (request != null) listOf(request) else listOf())
        _documents.value += d
        _documents.value.sortedByDescending { it.createdAt }
        return d
    }

    private fun insertRequest(request: ImageGenerationRequest) {
        if (_selectedDocument.value == null) {
            val document = createNewDocument(request)
            _selectedDocument.value = document
        } else {
            val s = (_selectedDocument.value?.requests?.size)?.minus(1) ?: -1
            _selectedDocument.value = _selectedDocument.value?.copy(
                requests = _selectedDocument.value?.requests?.plus(request) ?: listOf(request),
                cursor = s + 1
            )
        }
        setCurrentImageRequest()

        updateDocuments()
    }

    override suspend fun sendImageGenerationRequest(
        userId: String,
        userToken: String,
        prompt: String,
    ): Flow<Result<Unit>> = flow {
        val request = ImageGenerationRequest(status = ProcessStatus.PROCESSING.code)
        insertRequest(request = request)

        val apiResponse = api.sendImageGenerationRequest(
            userId = userId,
            token = userToken,
            prompt = prompt,

            // TODO
            modelName = "Supra HD",
            appVersion = BuildConfig.VERSION_NAME,
            imageRequestSerialId = _selectedDocument.value!!.requests[_selectedDocument.value!!.cursor].serial,
            documentSerialId = _selectedDocument.value!!.serial
        )

        if (apiResponse.isSuccessful) {
            apiResponse.body()?.let { response ->
                emit(Result.success(Unit))
            } ?: run {
                emit(Result.failure(NoResponseException()))
                return@flow
            }
        } else {
            val exception = ApiFailureResponseHandler.handler(apiResponse.errorBody())
            emit(Result.failure(exception))
        }
    }.catch {
        emit(Result.failure(it))
    }

    override suspend fun onGenerationSuccess(
        avatar: Media?,
        banner: Media,
        imageGenerationSerialId: String
    ) {
        _documents.value = _documents.value.map {
            it.copy(requests = it.requests.map { request ->
                if (request.serial == imageGenerationSerialId) {
                    request.copy(
                        status = ProcessStatus.COMPLETED.code,
                        url = banner.url
                    )
                } else {
                    request
                }
            })
        }

        _selectedDocument.value = _selectedDocument.value?.copy(
            requests = _selectedDocument.value?.requests?.map { request ->
                if (request.serial == imageGenerationSerialId) {
                    request.copy(
                        status = ProcessStatus.COMPLETED.code,
                        url = banner.url
                    )
                } else {
                    request
                }
            } ?: listOf()
        )
        setCurrentImageRequest()
    }

    override fun onGenerationError(imageGenerationSerialId: String) {
        _documents.value = _documents.value.map {
            it.copy(requests = it.requests.map { request ->
                if (request.serial == imageGenerationSerialId) {
                    request.copy(
                        status = ProcessStatus.FAILED.code,
                    )
                } else {
                    request
                }
            })
        }

        _selectedDocument.value = _selectedDocument.value?.copy(
            requests = _selectedDocument.value?.requests?.map { request ->
                if (request.serial == imageGenerationSerialId) {
                    request.copy(
                        status = ProcessStatus.FAILED.code,
                    )
                } else {
                    request
                }
            } ?: listOf()
        )
        setCurrentImageRequest()
    }

    private fun removeRequest(request: ImageGenerationRequest) {
        val newRequests = selectedDocument.value!!.requests.filter { it.serial != request.serial }
        when (true) {
            _selectedDocument.value!!.hasNext() -> {
                _selectedDocument.value = _selectedDocument.value!!.copy(
                    requests = newRequests,
                )
                updateDocuments()
            }

            _selectedDocument.value!!.hasPrevious() -> {
                _selectedDocument.value = _selectedDocument.value!!.copy(
                    cursor = _selectedDocument.value!!.cursor - 1,
                    requests = newRequests,
                )
                updateDocuments()
            }

            else -> {
                _documents.value =
                    _documents.value.filter { it.serial != selectedDocument.value?.serial }
                _selectedDocument.value = _documents.value.firstOrNull()
            }
        }

        setCurrentImageRequest()
    }

    override suspend fun delete(
        userId: String,
        userToken: String,
    ): Flow<Result<Unit>> =
        flow {
            try {
                val imageGenerationRequest: ImageGenerationRequest =
                    _selectedDocument.value?.requests?.get(_selectedDocument.value?.cursor ?: 0)
                        ?: return@flow

                val apiResponse = api.deleteImageGenerationRequest(
                    userId = userId,
                    token = userToken,
                    imageRequestSerialId = imageGenerationRequest.serial,
                    appVersion = BuildConfig.VERSION_NAME
                )

                if (apiResponse.isSuccessful) {
                    removeRequest(imageGenerationRequest)
                    emit(Result.success(Unit))
                } else {
                    val exception = ApiFailureResponseHandler.handler(apiResponse.errorBody())
                    emit(Result.failure(exception))
                }
            } catch (e: Exception) {
                emit(Result.failure(e))
            }
        }.catch {
            emit(Result.failure(it))
        }

    override suspend fun getSettings(): Flow<Result<ImageGeneratorSettings>> = flow {
        try {
            val apiResponse = api.getSettings(
                appVersion = BuildConfig.VERSION_NAME
            )
            if (apiResponse.isSuccessful) {
                apiResponse.body()?.let { response ->
                    emit(Result.success(response))
                } ?: run {
                    emit(Result.failure(NoResponseException()))
                    return@flow
                }
            } else {
                val exception = ApiFailureResponseHandler.handler(apiResponse.errorBody())
                emit(Result.failure(exception))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.catch {
        emit(Result.failure(it))
    }
}