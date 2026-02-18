package com.purpletear.ai_conversation.domain.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.purpletear.ai_conversation.domain.enums.Visibility

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

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + firstName.hashCode()
        result = 31 * result + (lastName?.hashCode() ?: 0)
        result = 31 * result + description.hashCode()
        result = 31 * result + (avatarUrl?.hashCode() ?: 0)
        result = 31 * result + (bannerUrl?.hashCode() ?: 0)
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + visibility.hashCode()
        result = 31 * result + (statusDescription?.hashCode() ?: 0)
        result = 31 * result + (code?.hashCode() ?: 0)
        return result
    }
}



