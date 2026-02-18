package com.purpletear.aiconversation.domain.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity("styles")
data class Style(
    val id: Int,
    @PrimaryKey val code: String,
    val label: String,
)