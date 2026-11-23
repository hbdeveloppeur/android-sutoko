package fr.purpletear.sutoko.screens

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.purpletear.game.data.database.GameDatabase
import com.purpletear.sutoko.domain.repository.UserRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// Debug-only access for emulator tests using the app's real repositories.
@EntryPoint
@InstallIn(SingletonComponent::class)
interface PlayerOnlyPreviewDependencies {
    fun database(): GameDatabase
    fun users(): UserRepository
    fun gamePreferences(): DataStore<Preferences>
}
