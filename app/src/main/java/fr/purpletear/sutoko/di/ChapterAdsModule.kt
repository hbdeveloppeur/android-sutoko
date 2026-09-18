package fr.purpletear.sutoko.di

import com.purpletear.game.presentation.game_play.ads.ChapterAdGateway
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.purpletear.sutoko.ads.AdMobChapterAdGateway

@Module
@InstallIn(SingletonComponent::class)
abstract class ChapterAdsModule {
    @Binds
    abstract fun bindChapterAdGateway(implementation: AdMobChapterAdGateway): ChapterAdGateway
}
