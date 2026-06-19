package com.purpletear.game.data.repository

import com.purpletear.game.data.remote.GameApi
import com.purpletear.ntfy.Ntfy
import com.purpletear.sutoko.game.model.game.GameInstall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GameCatalogEntityRepositoryImplTest {

    private val api: GameApi = Retrofit.Builder()
        .baseUrl("https://sutoko.com/")
        .client(OkHttpClient.Builder().cache(null).build())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GameApi::class.java)

    private val stubNtfy = object : Ntfy {
        override fun startAction(description: String) {}
        override fun send(message: String, data: Map<String, Any?>?) {}
        override fun send(message: String, channelId: String, data: Map<String, Any?>?) {}
        override fun exception(throwable: Throwable, data: Map<String, Any?>?) {}
        override fun urgent(throwable: Throwable, data: Map<String, Any?>?) {}
        override fun urgent(message: String, data: Map<String, Any?>?) {}
    }

    private val stubGameInstallRepository = object : GameInstallationRepository {
        override suspend fun saveInstallation(gameId: String, version: String) {}
        override suspend fun getInstallation(gameId: String): GameInstall? = null
        override suspend fun getInstalledVersion(gameId: String): String? = null
        override suspend fun isInstalled(gameId: String): Boolean = false
        override fun observeInstallationStatus(gameId: String): Flow<Boolean> =
            throw NotImplementedError()

        override fun observeInstallation(gameId: String): Flow<GameInstall?> =
            throw NotImplementedError()

        override suspend fun removeInstallation(gameId: String) {}
    }

    @Test
    fun `generateDownloadLinkInternal works with real endpoint for free game KwSEkz9VfM5`() =
        runTest {
            val repository =
                GameRepositoryImpl(api, stubGameInstallRepository, stubNtfy)

            val method = GameRepositoryImpl::class.java.getDeclaredMethod(
                "generateDownloadLinkInternal",
                String::class.java,
                String::class.java,
                String::class.java,
                String::class.java
            )
            method.isAccessible = true

            val flow = method.invoke(
                repository,
                "KwSEkz9VfM5",
                null,
                null,
                "test"
            ) as Flow<Result<String>>

            val result = flow.first()

            assertTrue(
                "Expected success but got failure: ${result.exceptionOrNull()?.message}",
                result.isSuccess
            )

            val link = result.getOrNull()
            assertNotNull("Download link should not be null", link)
            println("Download link for KwSEkz9VfM5: $link")
        }
}
