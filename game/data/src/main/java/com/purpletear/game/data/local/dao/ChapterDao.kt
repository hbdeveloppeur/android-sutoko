package com.purpletear.game.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.purpletear.sutoko.game.model.Chapter
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters WHERE story = :storyId ORDER BY number ASC, alternative ASC")
    suspend fun getAllForStory(storyId: String): List<Chapter>

    @Query("SELECT * FROM chapters WHERE story = :storyId ORDER BY number ASC, alternative ASC")
    fun observeAllForStory(storyId: String): Flow<List<Chapter>>

    @Query("SELECT * FROM chapters WHERE id = :id")
    suspend fun getById(id: String): Chapter?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chapters: List<Chapter>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chapter: Chapter)

    @Query("DELETE FROM chapters WHERE story = :storyId")
    suspend fun deleteAllForStory(storyId: String)

    @Query("DELETE FROM chapters WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM chapters WHERE story = :storyId")
    suspend fun getCountForStory(storyId: String): Int

    @Query("SELECT * FROM chapters WHERE story = :storyId AND code = :code LIMIT 1")
    suspend fun getByStoryAndCode(storyId: String, code: String): Chapter?
}
