package com.purpletear.game.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.annotation.Keep

/**
 * Entity for Author table - only needed if we want to:
 * - Search/filter games by author
 * - Show author profiles with aggregated stats
 * - Query authors independently
 */
@Entity(tableName = "authors")
@Keep
data class AuthorEntity(
    @PrimaryKey
    val id: String,  // Would need unique author ID from server
    val displayName: String,
    val avatarUrl: String?,
    val isCertified: Boolean
)
