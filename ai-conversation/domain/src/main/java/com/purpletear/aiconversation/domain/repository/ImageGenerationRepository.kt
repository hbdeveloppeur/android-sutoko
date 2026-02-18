package com.purpletear.aiconversation.domain.repository

import com.purpletear.aiconversation.domain.enums.Direction
import com.purpletear.aiconversation.domain.model.Document
import com.purpletear.aiconversation.domain.model.ImageGenerationRequest
import com.purpletear.aiconversation.domain.model.ImageGeneratorSettings
import com.purpletear.aiconversation.domain.model.Media
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ImageGenerationRepository {
    val documents: StateFlow<List<Document>>
    val selectedDocument: StateFlow<Document?>
    val currentImageRequest: StateFlow<ImageGenerationRequest?>

    fun selectedDocument(document: Document): Unit
    fun moveDocumentCursor(direction: Direction): Unit

    fun canCreateNewDocument(): Boolean

    fun createNewDocument(request: ImageGenerationRequest?): Document

    suspend fun loadDocuments(
        userId: String,
        userToken: String,
    ): Flow<Result<Unit>>

    suspend fun sendImageGenerationRequest(
        userId: String,
        userToken: String,
        prompt: String,
    ): Flow<Result<Unit>>

    suspend fun onGenerationSuccess(
        avatar: Media?,
        banner: Media,
        imageGenerationSerialId: String
    )

    fun onGenerationError(
        imageGenerationSerialId: String
    )

    suspend fun delete(
        userId: String,
        userToken: String,
    ): Flow<Result<Unit>>

    suspend fun getSettings(
    ): Flow<Result<ImageGeneratorSettings>>

}