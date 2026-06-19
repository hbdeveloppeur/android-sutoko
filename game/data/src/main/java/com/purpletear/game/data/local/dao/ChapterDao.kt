package com.purpletear.game.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.purpletear.game.data.local.entity.ChapterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters WHERE story = :storyId ORDER BY number ASC, alternative ASC")
    suspend fun getAllForStory(storyId: String): List<ChapterEntity>

    @Query("SELECT * FROM chapters WHERE story = :storyId ORDER BY number ASC, alternative ASC")
    fun observeAllForStory(storyId: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE id = :id")
    suspend fun getById(id: String): ChapterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chapters: List<ChapterEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chapter: ChapterEntity)

    @Query("DELETE FROM chapters WHERE story = :storyId")
    suspend fun deleteAllForStory(storyId: String)

    @Query("DELETE FROM chapters WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM chapters WHERE story = :storyId")
    suspend fun getCountForStory(storyId: String): Int

    @Query("SELECT * FROM chapters WHERE story = :storyId AND code = :code LIMIT 1")
    suspend fun getByStoryAndCode(storyId: String, code: String): ChapterEntity?

    @Query("SELECT * FROM chapters WHERE story = :storyId AND code = :code LIMIT 1")
    fun observeByStoryAndCode(storyId: String, code: String): Flow<ChapterEntity?>
}
