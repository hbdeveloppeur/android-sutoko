package com.purpletear.game.presentation.di

import com.purpletear.game.presentation.smsgame.SavedStatePersistence
import com.purpletear.sutoko.game.engine.StatePersistence
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

/**
 * DI module for SMS game presentation layer.
 * Binds presentation-specific implementations to domain abstractions.
 * 
 * NOTE: TimingScheduler is bound in Infrastructure (GameDataModule) as it's
 * a framework concern (coroutines), not a presentation concern.
 */
@Module
@InstallIn(ViewModelComponent::class)
abstract class SmsGameModule {
    
    @Binds
    abstract fun bindStatePersistence(
        savedStatePersistence: SavedStatePersistence
    ): StatePersistence
}
