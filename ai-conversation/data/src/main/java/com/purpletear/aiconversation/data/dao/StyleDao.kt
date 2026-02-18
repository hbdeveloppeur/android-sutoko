package com.purpletear.aiconversation.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.purpletear.aiconversation.domain.model.Style

@Dao
interface StyleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun persist(style: Style)

    @Query("SELECT * FROM styles")
    suspend fun getAll(): List<Style>

    @Query("DELETE FROM styles WHERE id = :id")
    fun delete(id: Int)
}