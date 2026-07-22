package com.purpletear.aiconversation.domain.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.purpletear.aiconversation.domain.enums.Visibility

@Keep
@Entity("ai_characters")
data class AiCharacter(
    @PrimaryKey val id: Int,
    val firstName: String,
    val lastName: String?,
    val description: String,
    val avatarUrl: String?,
    val bannerUrl: String?,
    val createdAt: Long,
    val visibility: Visibility,
    val statusDescription: String?,
    val code: String?,
) {
    override fun equals(other: Any?): Boolean {
        return other is AiCharacter && id == other.id
    }

    // equals compares id only: hashCode must do the same (equal objects, equal hash codes).
    override fun hashCode(): Int = id
}



