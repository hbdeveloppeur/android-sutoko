package com.purpletear.game.data.remote.testing

import com.purpletear.game.data.remote.testing.dto.JoinTestSessionRequest
import com.purpletear.game.data.remote.testing.dto.JoinTestSessionResponse
import com.purpletear.game.data.remote.testing.dto.RegisterInventoryRequest
import com.purpletear.game.data.remote.testing.dto.RegisterInventoryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface TestSessionApi {

    @POST("test-session/join")
    suspend fun join(
        @Header("Authorization") authorization: String,
        @Body request: JoinTestSessionRequest,
    ): Response<JoinTestSessionResponse>

    @POST("test-session/{sessionId}/inventory")
    suspend fun registerInventory(
        @Header("Authorization") authorization: String,
        @Path("sessionId") sessionId: String,
        @Body request: RegisterInventoryRequest,
    ): Response<RegisterInventoryResponse>
}
