package com.purpletear.aiconversation.data.local.converter

import androidx.room.TypeConverter
import com.purpletear.aiconversation.domain.enums.Visibility

class AiCharacterConverter {
    @TypeConverter
    fun fromAiCharacterVisibility(visibility: Visibility): String {
        return visibility.name
    }

    @TypeConverter
    fun toAiCharacterVisibility(visibility: String): Visibility {
        return Visibility.fromString(visibility)
            ?: throw IllegalArgumentException("Visibility $visibility not found in enum Visibility")
    }
}