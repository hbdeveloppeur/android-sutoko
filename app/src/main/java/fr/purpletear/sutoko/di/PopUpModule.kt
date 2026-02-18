package fr.purpletear.sutoko.di

import com.purpletear.sutoko.popup.data.PopUpRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.purpletear.sutoko.popup.domain.repository.PopUpRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class PopUpModule {

    @Provides
    @Singleton
    fun providePopUpRepository(): PopUpRepository {
        return PopUpRepositoryImpl()
    }
}