package com.purpletear.game.data.repository

import androidx.room.Room
import com.purpletear.game.data.database.GameDatabase
import com.purpletear.game.data.local.entity.ChapterEntity
import com.purpletear.game.data.local.entity.MemoryEntity
import com.purpletear.game.data.local.entity.UserGameProgressEntity
import com.purpletear.game.data.remote.ChapterApi
import com.purpletear.game.data.remote.dto.ChapterDto
import com.purpletear.game.data.remote.dto.ChapterMetasDto
import com.purpletear.sutoko.domain.model.User
import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.game.repository.FriendzonedProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import retrofit2.Response
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class ChapterRepositoryRefreshTest {
    private lateinit var database: GameDatabase
    private lateinit var repository: ChapterRepositoryImpl
    private var response: Response<List<ChapterDto>> = Response.success(emptyList())
    private var networkFailure: IOException? = null
    private val progress = UserGameProgressEntity(
        gameId = STORY_ID, currentChapterCode = "2a", normalizedChapterCode = "2a", heroName = "Alex",
    )
    private val decision = MemoryEntity(STORY_ID, "trusted_friend", "true", 1)

    @Before
    fun setUp() = runBlocking {
        database = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(), GameDatabase::class.java,
        ).allowMainThreadQueries().build()
        repository = ChapterRepositoryImpl(
            api = object : ChapterApi {
                override suspend fun getChapters(
                    storyId: String, langCode: String, authorization: String?,
                ): Response<List<ChapterDto>> {
                    networkFailure?.let { throw it }
                    return response
                }

                override suspend fun getChapter(id: Int, langCode: String): Response<ChapterDto> =
                    error("unused")
            },
            chapterDao = database.chapterDao(),
            userGameProgressDao = database.userGameProgressDao(),
            gameDao = database.gameDao(),
            friendzonedProgressRepository = object : FriendzonedProgressRepository {
                override suspend fun getChapterCode(legacyId: Int): String = error("unused")
                override suspend fun reset(legacyId: Int) = error("unused")
                override suspend fun setFirstName(legacyId: Int, name: String) = error("unused")
            },
            userRepository = object : UserRepository {
                override fun observeUser(): Flow<User?> = flowOf(null)
                override fun observeIsConnected(): Flow<Boolean> = flowOf(false)
                override fun isConnected(): Result<Boolean> = Result.success(false)
                override suspend fun connect(id: String, token: String): Result<Unit> = error("unused")
                override suspend fun disconnect(): Result<Unit> = error("unused")
            },
        )
        database.chapterDao().insertAll(listOf(chapter("1A", 1), chapter("2A", 2)))
        database.userGameProgressDao().save(progress)
        database.memoryDao().insert(decision)
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun `authoritative refresh removes absent chapters without rewriting player progress`() = runBlocking {
        val otherStoryChapter = chapter("1A", 1).copy(id = "other-chapter", story = "other-story")
        database.chapterDao().insert(otherStoryChapter)
        response = Response.success(listOf(dto("1A", 1)))

        val result = repository.getChapters(STORY_ID).toList().last().getOrThrow()

        assertEquals(listOf("1A"), result.map { it.code })
        assertEquals(listOf(otherStoryChapter), database.chapterDao().getAllForStory("other-story"))
        assertNull(repository.observeCurrentChapter(STORY_ID).first())
        assertPlayerStateUnchanged()
    }

    @Test
    fun `successful empty list removes every cached chapter`() = runBlocking {
        val result = repository.getChapters(STORY_ID).toList().last().getOrThrow()

        assertTrue(result.isEmpty())
        assertTrue(database.chapterDao().getAllForStory(STORY_ID).isEmpty())
        assertPlayerStateUnchanged()
    }

    @Test
    fun `network and HTTP failures retain the cached chapters`() = runBlocking {
        networkFailure = IOException("offline")
        repository.getChapters(STORY_ID).toList()
        assertEquals(2, database.chapterDao().getCountForStory(STORY_ID))

        networkFailure = null
        response = Response.error(503, "Unavailable".toResponseBody())
        repository.getChapters(STORY_ID).toList()

        assertEquals(2, database.chapterDao().getCountForStory(STORY_ID))
        assertPlayerStateUnchanged()
    }

    @Test
    fun `missing response body does not erase cached chapters`() = runBlocking {
        response = Response.success(null)

        repository.getChapters(STORY_ID).toList()

        assertEquals(2, database.chapterDao().getCountForStory(STORY_ID))
    }

    @Test
    fun `duplicate chapter ids do not replace cached chapters`() = runBlocking {
        response = Response.success(listOf(dto("3A", 3), dto("3B", 3).copy(id = "chapter-3A")))

        repository.getChapters(STORY_ID).toList()

        assertEquals(listOf("1A", "2A"), database.chapterDao().getAllForStory(STORY_ID).map { it.code })
        assertPlayerStateUnchanged()
    }

    @Test
    fun `duplicate canonical chapter codes do not replace cached chapters`() = runBlocking {
        response = Response.success(listOf(dto("3A", 3), dto("3a", 4)))

        repository.getChapters(STORY_ID).toList()

        assertEquals(listOf("1A", "2A"), database.chapterDao().getAllForStory(STORY_ID).map { it.code })
        assertPlayerStateUnchanged()
    }

    @Test
    fun `distinct alternatives with the same chapter number remain available`() = runBlocking {
        response = Response.success(listOf(dto("3A", 3), dto("3B", 3).copy(alternative = "B")))

        val result = repository.getChapters(STORY_ID).toList().last().getOrThrow()

        assertEquals(listOf("3A", "3B"), result.map { it.code })
    }

    @Test
    fun `failed replacement rolls back deleted chapters`() = runBlocking {
        database.openHelper.writableDatabase.execSQL(
            "CREATE TRIGGER fail_insert BEFORE INSERT ON chapters " +
                "BEGIN SELECT RAISE(ABORT, 'injected failure'); END",
        )
        response = Response.success(listOf(dto("3A", 3)))

        repository.getChapters(STORY_ID).toList()

        assertEquals(listOf("1A", "2A"), database.chapterDao().getAllForStory(STORY_ID).map { it.code })
        assertPlayerStateUnchanged()
    }

    @Test
    fun `current chapter resolves lowercase progress against uppercase catalog rows`() = runBlocking {
        assertEquals("2A", repository.observeCurrentChapter(STORY_ID).first()?.code)
        assertEquals("2A", repository.getCurrentChapter(STORY_ID, false).first().getOrThrow()?.code)
        assertEquals("2A", database.chapterDao().getByStoryAndCode(STORY_ID, "2a")?.code)
    }

    @Test
    fun `missing initial chapter leaves explicit selection to the player`() = runBlocking {
        database.userGameProgressDao().delete(STORY_ID)
        database.chapterDao().replaceAllForStory(
            STORY_ID,
            listOf(chapter("INTRO", 1).copy(available = false), chapter("START", 2)),
        )

        assertNull(repository.observeCurrentChapter(STORY_ID).first())
        assertNull(database.userGameProgressDao().get(STORY_ID))
    }

    private suspend fun assertPlayerStateUnchanged() {
        assertEquals(progress, database.userGameProgressDao().get(STORY_ID))
        assertEquals(listOf(decision), database.memoryDao().getAllForGameUpToChapter(STORY_ID, 2))
    }

    private fun chapter(code: String, number: Int) = ChapterEntity(
        id = "chapter-$code", story = STORY_ID, code = code, number = number, available = true,
    )

    private fun dto(code: String, number: Int) = ChapterDto(
        id = "chapter-$code", number = number, alternative = "A", releaseDate = 0,
        createdAt = 0, story = STORY_ID,
        metas = ChapterMetasDto(1, "en", "Chapter $number", ""),
        canvasAppVersion = 1, code = code, available = true,
    )

    private companion object {
        const val STORY_ID = "story"
    }
}
