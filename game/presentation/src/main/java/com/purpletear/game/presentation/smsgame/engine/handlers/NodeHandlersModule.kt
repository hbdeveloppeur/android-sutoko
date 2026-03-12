package com.purpletear.game.presentation.smsgame.engine.handlers

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

/**
 * Consolidated module that includes all node handler modules.
 * Each handler module is auto-generated with @Binds and @IntoMap annotations.
 */
@Module(
    includes = [
        StartNodeHandlerModule::class,
        MessageNodeHandlerModule::class,
        ChapterChangeNodeHandlerModule::class,
        ChoiceNodeHandlerModule::class,
        ConditionNodeHandlerModule::class,
        MemoryNodeHandlerModule::class,
        InfoNodeHandlerModule::class,
        TrophyNodeHandlerModule::class,
        SignalNodeHandlerModule::class,
        BackgroundNodeHandlerModule::class
    ]
)
@InstallIn(ViewModelComponent::class)
object NodeHandlersModule
