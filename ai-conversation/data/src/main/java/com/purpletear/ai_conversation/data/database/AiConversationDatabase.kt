package com.purpletear.ai_conversation.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.purpletear.ai_conversation.data.dao.MediaDao
import com.purpletear.ai_conversation.data.dao.StyleDao
import com.purpletear.ai_conversation.data.local.converter.AiCharacterConverter
import com.purpletear.ai_conversation.domain.model.Media
import com.purpletear.ai_conversation.domain.model.Style

@Database(
    entities = [Style::class, Media::class],
    version = 21
)
@TypeConverters(AiCharacterConverter::class)
abstract class AiConversationDatabase : RoomDatabase() {
    abstract fun styleDao(): StyleDao
    abstract fun mediaDao(): MediaDao
}