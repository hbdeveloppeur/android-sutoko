package com.purpletear.aiconversation.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.purpletear.aiconversation.domain.model.Media
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun persist(media: Media)

    @Query("SELECT * FROM medias WHERE imageGenerationRequestSerialId = :imageGenerationRequestSerialId")
    fun getByImageRequest(imageGenerationRequestSerialId: String): Flow<List<Media>>
}