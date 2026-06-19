package com.purpletear.core.di

import com.purpletear.core.coordinator.AiConversationCoordinator
import com.purpletear.core.coordinator.AiConversationCoordinatorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiConversationCoordinatorModule {

    @Binds
    @Singleton
    abstract fun bindAiConversationCoordinator(
        impl: AiConversationCoordinatorImpl
    ): AiConversationCoordinator
}