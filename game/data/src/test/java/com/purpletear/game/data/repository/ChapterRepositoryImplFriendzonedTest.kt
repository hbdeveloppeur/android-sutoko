package com.purpletear.game.data.repository

import com.purpletear.game.data.local.dao.ChapterDao
import com.purpletear.game.data.local.dao.GameDao
import com.purpletear.game.data.local.dao.UserGameProgressDao
import com.purpletear.game.data.local.entity.ChapterEntity
import com.purpletear.game.data.local.entity.GameCatalogEntity
import com.purpletear.game.data.local.entity.UserGameProgressEntity
import com.purpletear.game.data.remote.ChapterApi
import com.purpletear.game.data.remote.dto.ChapterDto
import com.purpletear.sutoko.domain.model.User
import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.game.model.game.GameMetadata
import com.purpletear.sutoko.game.repository.FriendzonedProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Response

class ChapterRepositoryImplFriendzonedTest {

    private class StubChapterApi : ChapterApi {
        override suspend fun getChapters(
            storyId: String,
            langCode: String,
            authorization: String?,
        ): Response<List<ChapterDto>> =
            Response.success(emptyList())

        override suspend fun getChapter(id: Int, langCode: String): Response<ChapterDto> =
            Response.success(null)
    }

    private class FakeChapterDao(
        private val chapters: MutableStateFlow<List<ChapterEntity>> = MutableStateFlow(emptyList()),
    ) : ChapterDao {
        fun setChapters(value: List<ChapterEntity>) {
            chapters.value = value
        }

        override suspend fun getAllForStory(storyId: String): List<ChapterEntity> =
            chapters.value.filter { it.story == storyId }

        override fun observeAllForStory(storyId: String): Flow<List<ChapterEntity>> =
            chapters.map { list -> list.filter { it.story == storyId } }

        override suspend fun getById(id: String): ChapterEntity? =
            chapters.value.firstOrNull { it.id == id }

        override suspend fun insertAll(chapters: List<ChapterEntity>) = Unit

        override suspend fun insert(chapter: ChapterEntity) = Unit

        override suspend fun deleteAllForStory(storyId: String) = Unit

        override suspend fun deleteById(id: String) = Unit

        override suspend fun getCountForStory(storyId: String): Int =
            chapters.value.count { it.story == storyId }

        override suspend fun getByStoryAndCode(storyId: String, code: String): ChapterEntity? =
            chapters.value.firstOrNull { it.story == storyId && it.code == code }

        override fun observeByStoryAndCode(storyId: String, code: String): Flow<ChapterEntity?> =
            chapters.map { list -> list.firstOrNull { it.story == storyId && it.code == code } }

        override fun observeStoryIdsWithUpcomingChapters(nowSeconds: Long): Flow<List<String>> =
            kotlinx.coroutines.flow.flowOf(emptyList())
    }

    private class FakeUserGameProgressDao(
        private val progress: MutableStateFlow<UserGameProgressEntity?> = MutableStateFlow(null),
    ) : UserGameProgressDao {
        fun setProgress(value: UserGameProgressEntity?) {
            progress.value = value
        }

        override fun observe(gameId: String): Flow<UserGameProgressEntity?> = progress

        override suspend fun get(gameId: String): UserGameProgressEntity? = progress.value

        override suspend fun save(progress: UserGameProgressEntity) {
            this.progress.value = progress
        }

        override suspend fun delete(gameId: String) {
            progress.value = null
        }
    }

    private class FakeGameDao(
        private val game: MutableStateFlow<GameCatalogEntity?> = MutableStateFlow(null),
    ) : GameDao {
        fun setGame(value: GameCatalogEntity?) {
            game.value = value
        }

        override fun observeOfficialGames(): Flow<List<GameCatalogEntity>> =
            kotlinx.coroutines.flow.flowOf(emptyList())

        override fun observeUserGames(): Flow<List<GameCatalogEntity>> =
            kotlinx.coroutines.flow.flowOf(emptyList())

        override fun observeGame(id: String): Flow<GameCatalogEntity?> = game

        override suspend fun getByIds(ids: List<String>): List<GameCatalogEntity> =
            game.value?.takeIf { it.id in ids }?.let { listOf(it) } ?: emptyList()

        override suspend fun deleteAllOfficial() = Unit

        override suspend fun deleteAllUserGames() = Unit

        override suspend fun upsertAll(entities: List<GameCatalogEntity>) = Unit
    }

    private class FakeFriendzonedProgressRepository : FriendzonedProgressRepository {
        var chapterCode: String = "1a"
        var getChapterCodeCalls = 0

        override suspend fun getChapterCode(legacyId: Int): String {
            getChapterCodeCalls++
            return chapterCode
        }

        override suspend fun reset(legacyId: Int) = Unit

        override suspend fun setFirstName(legacyId: Int, name: String) = Unit
    }

    private val chapterDao = FakeChapterDao()
    private val userGameProgressDao = FakeUserGameProgressDao()
    private val gameDao = FakeGameDao()
    private val friendzonedProgressRepository = FakeFriendzonedProgressRepository()
    private val stubUserRepository = object : UserRepository {
        override fun observeUser(): Flow<User?> = MutableStateFlow(null)
        override fun observeIsConnected(): Flow<Boolean> = MutableStateFlow(false)
        override fun isConnected(): Result<Boolean> = Result.success(false)
        override suspend fun connect(id: String, token: String): Result<Unit> = Result.success(Unit)
        override suspend fun disconnect(): Result<Unit> = Result.success(Unit)
    }
    private val repository = ChapterRepositoryImpl(
        api = StubChapterApi(),
        chapterDao = chapterDao,
        userGameProgressDao = userGameProgressDao,
        gameDao = gameDao,
        friendzonedProgressRepository = friendzonedProgressRepository,
        userRepository = stubUserRepository,
    )

    private fun chapter(code: String, number: Int): ChapterEntity = ChapterEntity(
        id = "ch-$code",
        number = number,
        story = GAME_ID,
        code = code,
    )

    private fun catalog(legacyId: Int?): GameCatalogEntity = GameCatalogEntity(
        id = GAME_ID,
        metadata = GameMetadata(title = "Test", description = ""),
        legacyId = legacyId,
    )

    @Test
    fun `standard game reads current chapter from user progress`() = runTest {
        gameDao.setGame(catalog(legacyId = 42))
        chapterDao.setChapters(listOf(chapter("1A", 1), chapter("2A", 2)))
        userGameProgressDao.setProgress(UserGameProgressEntity(gameId = GAME_ID, currentChapterCode = "2A"))

        val current = repository.observeCurrentChapter(GAME_ID).first()

        assertEquals("2A", current?.code)
        assertEquals(0, friendzonedProgressRepository.getChapterCodeCalls)
    }

    @Test
    fun `friendzoned game reads current chapter from its own progress store`() = runTest {
        gameDao.setGame(catalog(legacyId = 162))
        chapterDao.setChapters(listOf(chapter("1A", 1), chapter("2A", 2), chapter("3A", 3)))
        // The Room progress row is stale for Friendzoned games and must be ignored.
        userGameProgressDao.setProgress(UserGameProgressEntity(gameId = GAME_ID, currentChapterCode = "1A"))
        friendzonedProgressRepository.chapterCode = "3a"

        val current = repository.observeCurrentChapter(GAME_ID).first()

        assertEquals("3A", current?.code)
        assertEquals(1, friendzonedProgressRepository.getChapterCodeCalls)
    }

    @Test
    fun `friendzoned code without exact match falls back to the same chapter number`() = runTest {
        gameDao.setGame(catalog(legacyId = 159))
        chapterDao.setChapters(listOf(chapter("1A", 1), chapter("7A", 7)))
        friendzonedProgressRepository.chapterCode = "7b"

        val current = repository.observeCurrentChapter(GAME_ID).first()

        assertEquals("7A", current?.code)
    }

    @Test
    fun `friendzoned unknown code falls back to the first chapter`() = runTest {
        gameDao.setGame(catalog(legacyId = 163))
        chapterDao.setChapters(listOf(chapter("1A", 1), chapter("2A", 2)))
        friendzonedProgressRepository.chapterCode = "12a"

        val current = repository.observeCurrentChapter(GAME_ID).first()

        assertEquals("1A", current?.code)
    }

    private companion object {
        const val GAME_ID = "game-1"
    }
}
