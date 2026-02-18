package com.purpletear.aiconversation.data.remote.dto

import androidx.annotation.Keep
import com.purpletear.aiconversation.domain.enums.ProcessStatus
import com.purpletear.aiconversation.domain.model.Document
import com.purpletear.aiconversation.domain.model.ImageGenerationRequest
import java.util.UUID

@Keep
data class GetAllDocumentsDto(
    val documents: List<DocumentDto>,
    val requests: List<ImageGenerationRequestDto>,
)

@Keep
data class DocumentDto(
    val id: Int,
    val serialId: String = UUID.randomUUID().toString(),
    val createdAt: Long = System.currentTimeMillis(),
)

fun DocumentDto.toDomain(): Document {
    return Document(
        serial = serialId,
        createdAt = createdAt,
    )
}

@Keep
data class ImageGenerationRequestDto(
    val id: Int,
    var prompt: String = "",
    val modelName: String = "",
    var status: String = ProcessStatus.INITIAL.code,
    val serial: String = ImageGenerationRequest.generateRandomSerial(),
    val url: String? = null,
    val documentSerialId: String = "",
    val avatarUrl: String? = null,
    val timeStamp: Long = System.currentTimeMillis()
)

fun ImageGenerationRequestDto.toDomain(): ImageGenerationRequest {
    return ImageGenerationRequest(
        serial = serial,
        prompt = prompt,
        modelName = modelName,
        documentSerialId = documentSerialId,
        status = status,
        url = url,
        avatarUrl = avatarUrl,
        timeStamp = timeStamp * 1000
    )
}

fun GetAllDocumentsDto.toDomain(): List<Document> {
    val d = mutableMapOf<String, Document>()
    this.documents.forEach { document ->
        val re = requests
            .filter { r -> r.documentSerialId == document.serialId }

        if (re.isNotEmpty()) {
            d[document.serialId] = document.toDomain()
            val requests = re
                .map { r -> r.toDomain() }
            d[document.serialId] = d[document.serialId]!!
                .copy(
                    requests = requests, cursor = (requests.size - 1).coerceAtLeast(0)
                )
        }
    }
    val list = d.values.toList()
    val sortedList = list.sortedByDescending { it.createdAt }
    return sortedList
}
