package fr.purpletear.sutoko.di

import android.app.Application
import android.content.Context
import android.os.Trace
import androidx.room.Room
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.FirebaseFirestore
import com.purpletear.core.presentation.services.MakeToastService
import com.purpletear.sutoko.core.domain.helper.AppVersionProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import purpletear.fr.purpleteartools.DelayHandler
import purpletear.fr.purpleteartools.TableOfPlayersV2
import purpletear.fr.purpleteartools.TableOfSymbols
import purpletear.fr.purpleteartools.symbols.SymbolsRoomStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import purpletear.fr.purpleteartools.symbols.SymbolsStorage
import purpletear.fr.purpleteartools.symbols.data.SymbolsDatabase
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Singleton
    @Provides
    fun provideToastService(@ApplicationContext context: Context): MakeToastService {
        return MakeToastService(context)
    }

    @Provides
    @Singleton
    fun provideFirebaseAnalytics(app: Application): FirebaseAnalytics {
        return FirebaseAnalytics.getInstance(app)
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideDelayHandler(): DelayHandler {
        return DelayHandler()
    }

    @Provides
    @Singleton
    fun provideSoundPlayer(
        @ApplicationContext appContext: Context,
        delayHandler: DelayHandler
    ): TableOfPlayersV2 {
        return TableOfPlayersV2(
            context = appContext,
            delayHandler = delayHandler
        )
    }

    @Provides
    @Singleton
    fun provideSymbolsDatabase(@ApplicationContext context: Context): SymbolsDatabase {
        return Room.databaseBuilder(context, SymbolsDatabase::class.java, "symbols.db")
            .build()
    }

    @Provides
    @Singleton
    fun provideSymbolsStorage(
        database: SymbolsDatabase,
        @ApplicationContext context: Context
    ): SymbolsStorage {
        val legacyFile = File(context.filesDir, "symbols/symbols.json")
        val storage = SymbolsRoomStorage(database, legacyFile)
        TableOfSymbols.storage = storage
        return storage
    }

    @Provides
    @Singleton
    fun provideTableOfSymbols(storage: SymbolsStorage): TableOfSymbols {
        Trace.beginSection("AppModule.loadTableOfSymbols")
        val table = runBlocking(Dispatchers.IO) {
            storage.load()
        } ?: TableOfSymbols(-1)
        Trace.endSection()
        return table
    }

    @Provides
    @Singleton
    fun provideAppVersionProvider(): AppVersionProvider {
        return AppVersionProviderImpl()
    }
}
