package com.purpletear.game.data.repository

import androidx.room.Room
import com.purpletear.game.data.database.GameDatabase
import com.purpletear.game.data.local.dao.ChapterDao
import com.purpletear.game.data.local.dao.GameInstallationDao
import com.purpletear.game.data.local.entity.ChapterEntity
import com.purpletear.game.data.local.entity.GameInstallEntity
import com.purpletear.game.data.local.entity.MemoryEntity
import com.purpletear.game.data.provider.AndroidGamePathProvider
import com.purpletear.sutoko.game.model.game.GameCatalog
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.repository.game.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class ChapterGraphRepositoryImplTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `lowercase resume keeps the chapter number and earlier decisions`() = runTest {
        val database = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(), GameDatabase::class.java,
        ).allowMainThreadQueries().build()
        try {
            database.chapterDao().insert(
                ChapterEntity(id = "chapter-2", story = "game-layout", code = "2A", number = 2),
            )
            val decision = MemoryEntity("game-layout", "trusted_friend", "true", 1)
            database.memoryDao().insert(decision)
            val repository = repositoryWithChapter(
                chapterCode = "2a", chapterDao = database.chapterDao(),
            )

            val graph = repository.loadChapterGraph("game-layout", "2a", "en").first().getOrThrow()
            val memory = GameMemory(
                MemoryRepositoryImpl(database.memoryDao()),
                UserGameProgressRepositoryImpl(database.userGameProgressDao()),
            )
            memory.load("game-layout", graph.chapterNumber)

            assertEquals(2, graph.chapterNumber)
            assertEquals("2A", graph.chapterCode)
            assertEquals("true", memory.state.value["trusted_friend"])
            assertEquals(listOf(decision), database.memoryDao().getAllForGameUpToChapter("game-layout", 2))
        } finally {
            database.close()
        }
    }

    @Test
    fun `broken install on disk is cleared when no chapter language is found`() = runTest {
        val gamesDir = temporaryFolder.newFolder("games")
        val gameId = "game1"
        val gameDir = File(gamesDir, gameId)
        File(gameDir, "scenes").mkdirs()

        val installDao = FakeGameInstallationDao().apply {
            upsert(GameInstallEntity(gameId = gameId, localVersion = 1))
        }
        val repository = ChapterGraphRepositoryImpl(
            pathProvider = FakeAndroidGamePathProvider(gamesDir),
            chapterDao = FakeChapterDao(),
            gameRepository = FakeGameRepository(),
            installDao = installDao,
        )

        val result = repository.loadChapterGraph(gameId, "1a", "en").first()

        assertTrue(result.isFailure)
        assertFalse("Broken game directory must be deleted", gameDir.exists())
        assertNull("Broken install record must be deleted", installDao.get(gameId))
    }

    @Test
    fun `install record is kept when game directory is absent`() = runTest {
        val gamesDir = temporaryFolder.newFolder("games")
        val gameId = "game2"

        val installDao = FakeGameInstallationDao().apply {
            upsert(GameInstallEntity(gameId = gameId, localVersion = 1))
        }
        val repository = ChapterGraphRepositoryImpl(
            pathProvider = FakeAndroidGamePathProvider(gamesDir),
            chapterDao = FakeChapterDao(),
            gameRepository = FakeGameRepository(),
            installDao = installDao,
        )

        val result = repository.loadChapterGraph(gameId, "1a", "en").first()

        assertTrue(result.isFailure)
        assertNotNull(
            "Install record must be kept when nothing exists on disk",
            installDao.get(gameId)
        )
    }

    @Test
    fun `layout json right side character ids are carried into the graph`() = runTest {
        val repository = repositoryWithChapter(
            layoutJson = """{"sides":{"right":[7166]}}"""
        )

        val graph = repository.loadChapterGraph("game-layout", "1a", "en")
            .first()
            .getOrThrow()

        assertEquals(listOf(7166), graph.rightSideCharacterIds)
    }

    @Test
    fun `missing layout json yields empty right side character ids`() = runTest {
        val repository = repositoryWithChapter(layoutJson = null)

        val graph = repository.loadChapterGraph("game-layout", "1a", "en")
            .first()
            .getOrThrow()

        assertTrue(graph.rightSideCharacterIds.isEmpty())
    }

    @Test
    fun `missing chapter metadata fails instead of loading with chapter one memories`() = runTest {
        val repository = repositoryWithChapter(chapterCode = "2a", chapterDao = FakeChapterDao())

        val result = repository.loadChapterGraph("game-layout", "2a", "en").first()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message.orEmpty().contains("Chapter metadata not found"))
    }

    @Test
    fun `invalid chapter number is rejected before the engine can load memories`() = runTest {
        val repository = repositoryWithChapter(
            chapterDao = FakeChapterDao(ChapterEntity(code = "1A", number = 0)),
        )

        assertTrue(repository.loadChapterGraph("game-layout", "1a", "en").first().isFailure)
    }

    private fun repositoryWithChapter(
        layoutJson: String? = null,
        chapterCode: String = "1a",
        chapterDao: ChapterDao = FakeChapterDao(ChapterEntity(code = "1A", number = 1)),
    ): ChapterGraphRepositoryImpl {
        val gamesDir = temporaryFolder.newFolder()
        val chapterDir = File(gamesDir, "game-layout/chapters/en/$chapterCode")
        check(chapterDir.mkdirs()) { "Failed to create $chapterDir" }
        File(chapterDir, "nodes.json").writeText(
            """[{"id":"start-0","type":"start","data":null}]"""
        )
        if (layoutJson != null) {
            File(chapterDir, "layout.json").writeText(layoutJson)
        }
        return ChapterGraphRepositoryImpl(
            pathProvider = FakeAndroidGamePathProvider(gamesDir),
            chapterDao = chapterDao,
            gameRepository = FakeGameRepository(),
            installDao = FakeGameInstallationDao(),
        )
    }

    private class FakeAndroidGamePathProvider(private val gamesDir: File) : AndroidGamePathProvider {
        override fun getStoriesDirectoryPath(): String = gamesDir.absolutePath
        override fun getStoryDirectoryPath(storyId: String, legacyId: Int?): String =
            File(gamesDir, legacyId?.toString() ?: storyId).absolutePath

        override fun getGamesDirectory(): File = gamesDir
        override fun getGameDirectory(gameId: String, legacyId: Int?): File =
            File(gamesDir, legacyId?.toString() ?: gameId)
    }

    private class FakeGameRepository : GameRepository {
        override fun observeOfficialGames(): Flow<List<GameCatalog>> = flowOf(emptyList())
        override fun observeUserGames(): Flow<List<GameCatalog>> = flowOf(emptyList())
        override fun observeGame(id: String): Flow<GameCatalog?> = flowOf(null)
        override suspend fun getDownloadLink(
            gameId: String,
            userId: String?,
            userToken: String?,
            preview: Boolean,
        ): Result<String> = error("unused")

        override suspend fun syncOfficialGames(languageTag: String): Result<Unit> = error("unused")
        override suspend fun syncUserGames(languageTag: String): Result<Unit> = error("unused")
        override suspend fun loadMoreUserGames(languageTag: String): Result<Boolean> = error("unused")
        override suspend fun getGameCatalog(id: String, languageTag: String): Result<GameCatalog?> =
            error("unused")

        override suspend fun refreshGameCatalog(id: String, languageTag: String): Result<GameCatalog?> =
            error("unused")

        override suspend fun searchStories(
            query: String,
            languageTag: String,
            page: Int,
            limit: Int,
        ): Result<List<GameCatalog>> = error("unused")

        override suspend fun getOneUserGames(
            userId: String,
            page: Int,
            limit: Int,
        ): Result<List<GameCatalog>> = error("unused")
    }

    private class FakeChapterDao(private val chapter: ChapterEntity? = null) : ChapterDao {
        override suspend fun getAllForStory(storyId: String): List<ChapterEntity> = emptyList()
        override fun observeAllForStory(storyId: String): Flow<List<ChapterEntity>> =
            flowOf(emptyList())

        override suspend fun getById(id: String): ChapterEntity? = null
        override suspend fun insertAll(chapters: List<ChapterEntity>) = Unit
        override suspend fun insert(chapter: ChapterEntity) = Unit
        override suspend fun deleteAllForStory(storyId: String) = Unit
        override suspend fun deleteById(id: String) = Unit
        override suspend fun getCountForStory(storyId: String): Int = 0
        override suspend fun getByStoryAndCode(storyId: String, code: String): ChapterEntity? =
            chapter?.takeIf { it.code.equals(code, ignoreCase = true) }
        override fun observeByStoryAndCode(storyId: String, code: String): Flow<ChapterEntity?> =
            flowOf(null)

        override fun observeStoryIdsWithUpcomingChapters(nowSeconds: Long): Flow<List<String>> =
            flowOf(emptyList())
    }

    private class FakeGameInstallationDao : GameInstallationDao {
        private val installs = MutableStateFlow<Map<String, GameInstallEntity>>(emptyMap())

        fun get(gameId: String): GameInstallEntity? = installs.value[gameId]

        override fun observeByGameId(gameId: String): Flow<GameInstallEntity?> =
            installs.map { it[gameId] }

        override fun observeAll(): Flow<List<GameInstallEntity>> =
            installs.map { it.values.toList() }

        override suspend fun upsert(entity: GameInstallEntity) {
            installs.value = installs.value + (entity.gameId to entity)
        }

        override suspend fun deleteByGameId(gameId: String) {
            installs.value = installs.value - gameId
        }

        override suspend fun markDownloaded(gameId: String, version: String) = Unit
    }
}
