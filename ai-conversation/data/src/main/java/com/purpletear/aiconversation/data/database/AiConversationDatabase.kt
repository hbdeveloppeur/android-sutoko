package com.purpletear.aiconversation.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.purpletear.aiconversation.data.dao.MediaDao
import com.purpletear.aiconversation.data.dao.StyleDao
import com.purpletear.aiconversation.data.local.converter.AiCharacterConverter
import com.purpletear.aiconversation.domain.model.Media
import com.purpletear.aiconversation.domain.model.Style

@Database(
    entities = [Style::class, Media::class],
    version = 21
)
@TypeConverters(AiCharacterConverter::class)
abstract class AiConversationDatabase : RoomDatabase() {
    abstract fun styleDao(): StyleDao
    abstract fun mediaDao(): MediaDao
}