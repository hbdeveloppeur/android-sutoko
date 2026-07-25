package com.purpletear.sutoko.game.model

/**
 * Role of the current user inside the game features.
 *
 * [PLAYER] is the default role: chapter availability rules apply normally.
 * [ADMINISTRATOR] can select and play chapters that are not released yet.
 */
enum class UserRole {
    PLAYER,
    ADMINISTRATOR,
}
