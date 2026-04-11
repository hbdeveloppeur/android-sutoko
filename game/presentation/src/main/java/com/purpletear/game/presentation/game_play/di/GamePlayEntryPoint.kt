package com.purpletear.game.presentation.game_play.di

import com.purpletear.sutoko.game.provider.GamePathProvider
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * EntryPoint for accessing game play dependencies in composables.
 * Used when dependencies are needed outside of ViewModels.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface GamePlayEntryPoint {
    fun gamePathProvider(): GamePathProvider
}
