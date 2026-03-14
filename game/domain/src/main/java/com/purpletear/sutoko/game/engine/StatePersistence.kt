package com.purpletear.sutoko.game.engine

/**
 * Abstraction for persisting game engine state.
 * Implementation in presentation layer uses SavedStateHandle for process death survival.
 */
interface StatePersistence {
    fun getString(key: String): String?
    fun setString(key: String, value: String)
    fun getKeys(): Set<String>
}
