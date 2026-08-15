package com.purpletear.game.data.repository

import com.purpletear.game.data.remote.dto.GameDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameRepositoryImplOfficialGamesTest {

    @Test
    fun `syncOfficialGames fetches and persists games with api order`() = runTest {
        val recordingDao = RecordingGameDao()
        val api = object : FakeGameApi() {
            override suspend fun getOfficialGames(
                languageCode: String,
                appVersionCode: Int,
                authorization: String?,
            ): List<GameDto> {
                assertEquals("fr-FR", languageCode)
                assertEquals(STUB_VERSION_CODE, appVersionCode)
                return listOf(
                    stubGameDto("game-first").copy(official = true),
                    stubGameDto("game-second").copy(official = true),
                    stubGameDto("game-third").copy(official = true),
                )
            }
        }
        val repository = gameRepository(api, recordingDao)

        val result = repository.syncOfficialGames("fr-FR")

        assertTrue(result.isSuccess)
        assertEquals(1, recordingDao.replaceAllOfficialCalls.size)
        val persisted = recordingDao.replaceAllOfficialCalls.first()
        assertEquals(3, persisted.size)
        assertEquals("game-first", persisted[0].id)
        assertEquals(0, persisted[0].officialOrder)
        assertEquals("game-second", persisted[1].id)
        assertEquals(1, persisted[1].officialOrder)
        assertEquals("game-third", persisted[2].id)
        assertEquals(2, persisted[2].officialOrder)
    }

    @Test
    fun `syncOfficialGames returns failure on error`() = runTest {
        val recordingDao = RecordingGameDao()
        val api = object : FakeGameApi() {
            override suspend fun getOfficialGames(
                languageCode: String,
                appVersionCode: Int,
                authorization: String?,
            ): List<GameDto> = throw RuntimeException("network down")
        }
        val repository = gameRepository(api, recordingDao)

        val result = repository.syncOfficialGames("fr-FR")

        assertTrue(result.isFailure)
        assertTrue(recordingDao.replaceAllOfficialCalls.isEmpty())
    }
}
