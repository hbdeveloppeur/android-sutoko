package com.purpletear.game.data.remote.testing

import com.purpletear.sutoko.domain.model.User
import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.game.repository.testing.DeviceIdProvider
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class TestEventDataSourceImplTest {

    @Test
    fun `events includes device id query parameter`() = runBlocking {
        val connectionDeferred = CompletableDeferred<HttpUrl>()
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val url = chain.request().url
                connectionDeferred.complete(url)
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("".toResponseBody("text/event-stream".toMediaType()))
                    .build()
            }
            .build()
        val dataSource = TestEventDataSourceImpl(
            client = client,
            userRepository = FakeUserRepository(),
            deviceIdProvider = FakeDeviceIdProvider("device-456"),
            baseUrl = "https://canvas.sutoko.com/api/"
        )

        val collectJob = launch {
            dataSource.events("session-1", "inventory-1").collect {}
        }

        val url = withTimeout(5000) { connectionDeferred.await() }
        collectJob.cancel()

        assertNotNull(url)
        assertEquals("device-456", url.queryParameter("deviceId"))
        assertEquals("phone", url.queryParameter("clientType"))
        assertEquals("inventory-1", url.queryParameter("assetInventoryToken"))
    }

    private class FakeUserRepository : UserRepository {
        override fun observeUser() = kotlinx.coroutines.flow.flowOf(User("user-1", "token-1"))
        override fun observeIsConnected() = kotlinx.coroutines.flow.flowOf(true)
        override fun isConnected() = Result.success(true)
        override suspend fun connect(id: String, token: String) = Result.success(Unit)
        override suspend fun disconnect() = Result.success(Unit)
    }

    private class FakeDeviceIdProvider(private val deviceId: String) : DeviceIdProvider {
        override suspend fun get(): String = deviceId
    }
}
