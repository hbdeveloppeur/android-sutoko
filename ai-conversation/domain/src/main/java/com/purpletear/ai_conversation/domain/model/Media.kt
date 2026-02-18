package com.purpletear.ai_conversation.domain.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medias")
@Keep
data class Media(
    @PrimaryKey var id: Int = 0,
    var url: String = "",
    var typeCode: String = "",
    var imageGenerationRequestSerialId: String? = null,
)