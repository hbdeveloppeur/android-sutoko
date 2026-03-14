package com.purpletear.game.presentation.smsgame

import androidx.lifecycle.SavedStateHandle
import com.purpletear.sutoko.game.engine.StatePersistence
import javax.inject.Inject

/**
 * Implementation of StatePersistence using Android's SavedStateHandle.
 * Survives process death for configuration changes and system-initiated kills.
 */
class SavedStatePersistence @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : StatePersistence {
    
    override fun getString(key: String): String? {
        return savedStateHandle[key]
    }
    
    override fun setString(key: String, value: String) {
        savedStateHandle[key] = value
    }
    
    override fun getKeys(): Set<String> {
        return savedStateHandle.keys()
    }
}
